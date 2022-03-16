package uk.gov.dwp.health.shop.submissionhandler;

import static com.mongodb.ErrorCategory.DUPLICATE_KEY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.crypto.exceptions.EventsMessageException;
import uk.gov.dwp.health.messageq.amazon.sns.MessagePublisher;
import uk.gov.dwp.health.messageq.items.event.EventMessage;
import uk.gov.dwp.health.messageq.items.event.MetaData;
import uk.gov.dwp.health.shop.submissionhandler.application.SubmissionHandlerConfiguration;
import uk.gov.dwp.health.shop.submissionhandler.application.handlers.MongoDbOperations;
import uk.gov.dwp.health.shop.submissionhandler.application.items.config.SubmissionConfigurationItem;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.Payload;
import uk.gov.dwp.health.shop.submissionhandler.application.items.subitems.MandatoryMsgItems;
import uk.gov.dwp.health.utilities.JsonObjectSchemaValidation;

@Path("/submission")
public class SubmissionHandlerResource {

  private static final Logger LOG =
      LoggerFactory.getLogger(SubmissionHandlerResource.class.getName());
  private static final String STANDARD_JSON_ERROR = "Payload contains invalid items";
  private static final String UNABLE_TO_PROCESS = "Unable to process request";

  private JsonObjectSchemaValidation jsonObjectSchemaValidation;
  private SubmissionHandlerConfiguration configuration;
  private MongoDbOperations mongoDbOperations;
  private MessagePublisher snsPublisher;

  @Inject
  public SubmissionHandlerResource(
      SubmissionHandlerConfiguration configuration,
      JsonObjectSchemaValidation schemaValidation,
      MongoDbOperations dbOperations,
      MessagePublisher messagePublisher) {
    this.jsonObjectSchemaValidation = schemaValidation;
    this.mongoDbOperations = dbOperations;
    this.snsPublisher = messagePublisher;
    this.configuration = configuration;
  }

  private String resolveMessageId(String payload) throws IOException {
    JsonNode jsonNode =
        new ObjectMapper()
            .readTree(payload.getBytes(StandardCharsets.UTF_8))
            .findValue(MandatoryMsgItems.MSG_ID);
    if (jsonNode == null) {
      throw new IOException(String.format("'%s' is null or invalid", MandatoryMsgItems.MSG_ID));
    }

    return jsonNode.asText();
  }

  @POST
  public Response mainApplicationPOST(String payload) {
    Response response;
    try {
      String messageId = resolveMessageId(payload);
      LOG.info("received submission from '{}'", messageId);

      SubmissionConfigurationItem messageConfiguration =
          configuration.getSubmissionServices().get(messageId);
      if (messageConfiguration == null) {
        throw new IOException(String.format("'%s' is an unknown msg_id, rejecting", messageId));
      }

      jsonObjectSchemaValidation.validateJsonDocumentWithFile(
          payload,
          messageConfiguration.getSchemaDocReference(),
          messageConfiguration.getJsonSchemaValidationDoc());

      Payload incomingStructure =
          new ObjectMapper().readValue(payload, messageConfiguration.getSerialisationClass());
      LOG.info(
          "payload will be serialised to class '{}'",
          messageConfiguration.getSerialisationClass().getName());

      if (incomingStructure.isContentValid()) {
        LOG.info("incoming structure successfully validated");

        mongoDbOperations.insertNewSubmissionRecord(incomingStructure, payload);
        LOG.info("object successfully persisted in mongodb instance");

        LOG.debug("created event message object");
        String correlationId = UUID.randomUUID().toString();

        MetaData metaData = new MetaData(Collections.singletonList(configuration.getSnsSubject()));
        metaData.setRoutingKey(messageConfiguration.getSnsEventRoutingKey());
        metaData.setCorrelationId(correlationId);

        LOG.debug("populated event message object");
        EventMessage eventToPublish = new EventMessage();
        eventToPublish.setBodyContents(incomingStructure);
        eventToPublish.setMetaData(metaData);

        LOG.debug(
            "Publish message to sns topic exchange '{}' "
                + ""
                + "with routing key '{}' and correlation id {}",
            configuration.getSnsTopicName(),
            messageConfiguration.getSnsEventRoutingKey(),
            correlationId);
        snsPublisher.publishMessageToSnsTopic(
            configuration.isSnsEncryptMessages(),
            configuration.getSnsTopicName(),
            configuration.getSnsSubject(),
            eventToPublish,
            null);

        response = Response.status(HttpStatus.SC_OK).entity(correlationId).build();

      } else {
        LOG.error("incoming structure is not valid");
        response = Response.status(HttpStatus.SC_BAD_REQUEST).entity(STANDARD_JSON_ERROR).build();
      }

    } catch (ValidationException e) {
      response = Response.status(HttpStatus.SC_BAD_REQUEST).entity(STANDARD_JSON_ERROR).build();
      LOG.error(
          "The schema validator has failed with description '{}' :: {}",
          e.getViolatedSchema() != null ? e.getViolatedSchema().getDescription() : "<none>",
          e.getMessage());
      LOG.debug(e.getClass().getName(), e);

      for (ValidationException ex : e.getCausingExceptions()) {
        LOG.error("The schema validator has failed with '{}'", ex.getMessage());
        LOG.debug(ex.getClass().getName(), ex);
      }

    } catch (IOException e) {
      response =
          Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(UNABLE_TO_PROCESS).build();
      LOG.error("Invalid payload or MessageQ exception :: {}", e.getMessage());
      LOG.debug(e.getClass().getName(), e);

    } catch (CryptoException e) {
      response =
          Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(UNABLE_TO_PROCESS).build();
      LOG.error("Crypto exception :: {}", e.getMessage());
      LOG.debug(e.getClass().getName(), e);

    } catch (MongoException e) {
      if (ErrorCategory.fromErrorCode(e.getCode()) == DUPLICATE_KEY) {
        response = Response.status(HttpStatus.SC_CONFLICT).entity(UNABLE_TO_PROCESS).build();
      } else {
        response =
            Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(UNABLE_TO_PROCESS).build();
      }

      LOG.error("Mongo exception :: {}", e.getMessage());
      LOG.debug(e.getClass().getName(), e);

    } catch (InstantiationException
        | InvocationTargetException
        | NoSuchMethodException
        | IllegalAccessException
        | EventsMessageException e) {
      response =
          Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(UNABLE_TO_PROCESS).build();
      LOG.error("Events publishing error :: {}", e.getMessage());
      LOG.debug(e.getClass().getName(), e);
    }

    return response;
  }
}
