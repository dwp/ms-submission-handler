package uk.gov.dwp.health.shop.submissionhandler;

import com.amazonaws.util.Base64;
import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.bson.BsonDocument;
import org.everit.json.schema.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.crypto.exceptions.EventsMessageException;
import uk.gov.dwp.health.messageq.amazon.sns.MessagePublisher;
import uk.gov.dwp.health.messageq.items.event.EventMessage;
import uk.gov.dwp.health.shop.submissionhandler.application.SubmissionHandlerConfiguration;
import uk.gov.dwp.health.shop.submissionhandler.application.handlers.MongoDbOperations;
import uk.gov.dwp.health.shop.submissionhandler.application.items.AtwSubmissionItem;
import uk.gov.dwp.health.shop.submissionhandler.application.items.config.SubmissionConfigurationItem;
import uk.gov.dwp.health.utilities.JsonObjectSchemaValidation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"squid:S1192"}) // allow string literals
public class SubmissionHandlerResourceTest {
    private static final String INVALID_JSON_CONTENTS = "{\"msg_id\":\"Test-Atw\",\"ref\":\"123qwe\",\"date_submitted\":\"2017-02-19T12:46:31Z\",\"applicant\":{\"forenames\":\"aa\",\"surname\":\"bb\",\"dob\":\"2000-02-29\",\"residence_address\":{\"lines\":[\"line 1\",\"line 2\"],\"premises\":\"at home\",\"postcode\":\"ls6 4pt\"},\"contact_options\":[{\"method\":\"snailmail\",\"data\":\"an.email@address.co.uk\",\"preferred\":true}],\"representative\":{\"full_name\":\"john andrew smith\",\"relationship\":\"father\",\"email\":\"email@server.com\",\"tel\":\"1234\"}},\"data_capture\":{\"someTextItem\":\"this is text\",\"someIntValue\":99},\"declaration\":\"I declare ok\",\"tags\":[null]}";
    private static final String UNKNOWN_MSG_ID = "{\"msg_id\":\"Unknown\",\"ref\":\"123qwe\",\"date_submitted\":\"2017-02-19T12:46:31Z\",\"applicant\":{\"forenames\":\"aa\",\"surname\":\"bb\",\"dob\":\"2000-02-29\",\"residence_address\":{\"lines\":[\"line 1\",\"line 2\"],\"premises\":\"at home\",\"postcode\":\"ls6 4pt\"},\"contact_options\":[{\"method\":\"email\",\"data\":\"an.email@address.co.uk\",\"preferred\":true}],\"representative\":{\"full_name\":\"john andrew smith\",\"relationship\":\"father\",\"email\":\"email@server.com\",\"tel\":\"1234\"}},\"data_capture\":{\"someTextItem\":\"this is text\",\"someIntValue\":99},\"declaration\":\"I declare ok\",\"tags\":[null]}";
    private static final String VALID_JSON = "{\"msg_id\":\"Test-Atw\",\"ref\":\"123qwe\",\"date_submitted\":\"2017-02-19T12:46:31Z\",\"applicant\":{\"forenames\":\"aa\",\"surname\":\"bb\",\"dob\":\"2000-02-29\",\"residence_address\":{\"lines\":[\"line 1\",\"line 2\"],\"premises\":\"at home\",\"postcode\":\"ls6 4pt\"},\"contact_options\":[{\"method\":\"email\",\"data\":\"an.email@address.co.uk\",\"preferred\":true}],\"representative\":{\"full_name\":\"john andrew smith\",\"relationship\":\"father\",\"email\":\"email@server.com\",\"tel\":\"1234\"}},\"data_capture\":{\"someTextItem\":\"this is text\",\"someIntValue\":99},\"declaration\":\"I declare ok\",\"tags\":[null]}";
    private static final String PAYLOAD_ERROR_RESPONSE = "Payload contains invalid items";
    private static final String STANDARD_500_RESPONSE = "Unable to process request";
    private static final String SCHEMA_SERIALISATION_ENTRY = "submissionAtw";
    private static final String EVENT_ROUTING_KEY = "mock.routing.key";
    private static final String MSG_SUBJECT = "submission-handler";
    private static final String MSG_TOPIC = "sub-handler-topic";

    private static final File REFERAL_SCHEMA = new File("src/main/resources/referral.SubmissionItem.JsonSchemaDoc.json");
    private static final File ATW_SCHEMA = new File("src/main/resources/atw.SubmissionItem.JsonSchemaDoc.json");

    @Mock
    private JsonObjectSchemaValidation schemaValidation;

    @Mock
    private SubmissionHandlerConfiguration configuration;

    @Mock
    private MongoDbOperations mongoDbOperations;

    @Mock
    private MessagePublisher mqPublisher;

    @Mock
    private CryptoDataManager cryptoDataManager;

    @Mock
    private SubmissionConfigurationItem submissionConfigurationItem;

    @Captor
    private ArgumentCaptor<EventMessage> messageEventCapture;

    @Before
    public void init() {
        when(configuration.getSubmissionServices()).thenReturn(ImmutableMap.of("Test-Atw", submissionConfigurationItem));

        when(configuration.getSnsTopicName()).thenReturn(MSG_TOPIC);
        when(configuration.getSnsSubject()).thenReturn(MSG_SUBJECT);

        doReturn(AtwSubmissionItem.class).when(submissionConfigurationItem).getSerialisationClass();
        when(submissionConfigurationItem.getSnsEventRoutingKey()).thenReturn(EVENT_ROUTING_KEY);
        when(submissionConfigurationItem.getJsonSchemaValidationDoc()).thenReturn(ATW_SCHEMA);
        when(submissionConfigurationItem.getSchemaDocReference()).thenReturn(SCHEMA_SERIALISATION_ENTRY);
    }

    @Test
    public void validPayloadAtwProcessedCorrectlyWithEncryptedSns200() throws CryptoException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, EventsMessageException, InvocationTargetException {
        CryptoMessage encryptedObject = new CryptoMessage();
        encryptedObject.setMessage("encrypted_version_of_the_payload");
        encryptedObject.setKey(Base64.encodeAsString("a key".getBytes()));

        when(cryptoDataManager.encrypt(VALID_JSON)).thenReturn(encryptedObject);
        when(configuration.isSnsEncryptMessages()).thenReturn(true);

        SubmissionHandlerResource instance = new SubmissionHandlerResource(
                configuration,
                schemaValidation,
                cryptoDataManager,
                mongoDbOperations,
                mqPublisher
        );
        Response response = instance.mainApplicationPOST(VALID_JSON);

        UUID correlationId = UUID.fromString(response.getEntity().toString());

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(VALID_JSON), eq(SCHEMA_SERIALISATION_ENTRY), eq(ATW_SCHEMA));
        verify(cryptoDataManager, times(1)).encrypt(eq(VALID_JSON));
        verify(mongoDbOperations, times(1)).insertNewSubmissionRecord(any(AtwSubmissionItem.class), eq(encryptedObject.getMessage()), eq(encryptedObject.getKey()));
        verify(mqPublisher, times(1)).publishMessageToSnsTopic(eq(true), eq(MSG_TOPIC), eq(MSG_SUBJECT), messageEventCapture.capture(), any(Map.class));

        assertThat(messageEventCapture.getValue().serialisedBodyContentsToJson(), is(equalTo(VALID_JSON)));
        assertThat(messageEventCapture.getValue().getMetaData().getRoutingKey(), is(equalTo(EVENT_ROUTING_KEY)));
        assertThat(messageEventCapture.getValue().getMetaData().getCorrelationId(), is(equalTo(correlationId.toString())));
        assertThat("response should be 200", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_OK)));
        assertNotNull("correlationId should carry a value", correlationId);
    }

    @Test
    public void unknownMessageIdThrowsError() {
        SubmissionHandlerResource instance = new SubmissionHandlerResource(
                configuration,
                schemaValidation,
                cryptoDataManager,
                mongoDbOperations,
                mqPublisher
        );
        Response response = instance.mainApplicationPOST(UNKNOWN_MSG_ID);

        verifyZeroInteractions(schemaValidation);
        verifyZeroInteractions(cryptoDataManager);
        verifyZeroInteractions(mongoDbOperations);
        verifyZeroInteractions(mqPublisher);

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }

    @Test
    public void validPayloadReferralProcessedCorrectlyWithEncryptedSns200() throws CryptoException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, EventsMessageException, InvocationTargetException {
        CryptoMessage encryptedObject = new CryptoMessage();
        encryptedObject.setMessage("encrypted_version_of_the_payload");
        encryptedObject.setKey(Base64.encodeAsString("a key".getBytes()));

        when(submissionConfigurationItem.getJsonSchemaValidationDoc()).thenReturn(REFERAL_SCHEMA);
        when(cryptoDataManager.encrypt(VALID_JSON)).thenReturn(encryptedObject);
        when(configuration.isSnsEncryptMessages()).thenReturn(true);

        SubmissionHandlerResource instance = new SubmissionHandlerResource(
                configuration,
                schemaValidation,
                cryptoDataManager,
                mongoDbOperations,
                mqPublisher
        );
        Response response = instance.mainApplicationPOST(VALID_JSON);

        UUID correlationId = UUID.fromString(response.getEntity().toString());

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(VALID_JSON), eq(SCHEMA_SERIALISATION_ENTRY), eq(REFERAL_SCHEMA));
        verify(cryptoDataManager, times(1)).encrypt(eq(VALID_JSON));
        verify(mongoDbOperations, times(1)).insertNewSubmissionRecord(any(AtwSubmissionItem.class), eq(encryptedObject.getMessage()), eq(encryptedObject.getKey()));
        verify(mqPublisher, times(1)).publishMessageToSnsTopic(eq(true), eq(MSG_TOPIC), eq(MSG_SUBJECT), messageEventCapture.capture(), any(Map.class));

        assertThat(messageEventCapture.getValue().serialisedBodyContentsToJson(), is(equalTo(VALID_JSON)));
        assertThat(messageEventCapture.getValue().getMetaData().getRoutingKey(), is(equalTo(EVENT_ROUTING_KEY)));
        assertThat(messageEventCapture.getValue().getMetaData().getCorrelationId(), is(equalTo(correlationId.toString())));
        assertThat("response should be 200", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_OK)));
        assertNotNull("correlationId should carry a value", correlationId);
    }

    @Test
    public void validPayloadProcessedCorrectlyWithPlaintextSns200() throws CryptoException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, EventsMessageException, InvocationTargetException {
        CryptoMessage encryptedObject = new CryptoMessage();
        encryptedObject.setMessage("encrypted_version_of_the_payload");
        encryptedObject.setKey(Base64.encodeAsString("a key".getBytes()));

        when(cryptoDataManager.encrypt(VALID_JSON)).thenReturn(encryptedObject);

        SubmissionHandlerResource instance = new SubmissionHandlerResource(
                configuration,
                schemaValidation,
                cryptoDataManager,
                mongoDbOperations,
                mqPublisher
        );
        Response response = instance.mainApplicationPOST(VALID_JSON);

        UUID correlationId = UUID.fromString(response.getEntity().toString());

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(VALID_JSON), eq(SCHEMA_SERIALISATION_ENTRY), eq(ATW_SCHEMA));
        verify(cryptoDataManager, times(1)).encrypt(eq(VALID_JSON));
        verify(mongoDbOperations, times(1)).insertNewSubmissionRecord(any(AtwSubmissionItem.class), eq(encryptedObject.getMessage()), eq(encryptedObject.getKey()));
        verify(mqPublisher, times(1)).publishMessageToSnsTopic(eq(false), eq(MSG_TOPIC), eq(MSG_SUBJECT), messageEventCapture.capture(), any(Map.class));

        assertThat(messageEventCapture.getValue().serialisedBodyContentsToJson(), is(equalTo(VALID_JSON)));
        assertThat(messageEventCapture.getValue().getMetaData().getRoutingKey(), is(equalTo(EVENT_ROUTING_KEY)));
        assertThat(messageEventCapture.getValue().getMetaData().getCorrelationId(), is(equalTo(correlationId.toString())));
        assertThat("response should be 200", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_OK)));
        assertNotNull("correlationId should carry a value", correlationId);
    }

    @Test
    public void validPayloadCryptoThrowsExceptionToGet500() throws CryptoException, IOException {
        when(cryptoDataManager.encrypt(VALID_JSON)).thenThrow(new CryptoException("I am an exception!"));

        SubmissionHandlerResource instance = new SubmissionHandlerResource(configuration, schemaValidation, cryptoDataManager, mongoDbOperations, mqPublisher);
        Response response = instance.mainApplicationPOST(VALID_JSON);

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(VALID_JSON), eq(SCHEMA_SERIALISATION_ENTRY), eq(ATW_SCHEMA));
        verify(cryptoDataManager, times(1)).encrypt(eq(VALID_JSON));
        verifyZeroInteractions(mongoDbOperations);
        verifyZeroInteractions(mqPublisher);

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        assertThat("response payload should be standard", response.getEntity().toString(), is(equalTo(STANDARD_500_RESPONSE)));
    }

    @Test
    public void validPayloadMongoDbThrowsExceptionToGet500() throws CryptoException, IOException {
        CryptoMessage encryptedObject = new CryptoMessage();
        encryptedObject.setMessage("encrypted_version_of_the_payload");
        encryptedObject.setKey(Base64.encodeAsString("a key".getBytes()));

        when(cryptoDataManager.encrypt(VALID_JSON)).thenReturn(encryptedObject);
        doThrow(new MongoWriteException(new WriteError(987, "error!", new BsonDocument()), new ServerAddress("mockhost:123"))).when(mongoDbOperations).insertNewSubmissionRecord(any(AtwSubmissionItem.class), eq(encryptedObject.getMessage()), eq(encryptedObject.getKey()));

        SubmissionHandlerResource instance = new SubmissionHandlerResource(configuration, schemaValidation, cryptoDataManager, mongoDbOperations, mqPublisher);
        Response response = instance.mainApplicationPOST(VALID_JSON);

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(VALID_JSON), eq(SCHEMA_SERIALISATION_ENTRY), eq(ATW_SCHEMA));
        verify(cryptoDataManager, times(1)).encrypt(eq(VALID_JSON));
        verify(mongoDbOperations, times(1)).insertNewSubmissionRecord(any(AtwSubmissionItem.class), anyString(), anyString());
        verifyZeroInteractions(mqPublisher);

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        assertThat("response payload should be standard", response.getEntity().toString(), is(equalTo(STANDARD_500_RESPONSE)));
    }

    @Test
    public void validPayloadSnsMqThrowsExceptionToGet500() throws CryptoException, IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, EventsMessageException, InvocationTargetException {
        CryptoMessage encryptedObject = new CryptoMessage();
        encryptedObject.setMessage("encrypted_version_of_the_payload");
        encryptedObject.setKey(Base64.encodeAsString("a key".getBytes()));

        when(cryptoDataManager.encrypt(VALID_JSON)).thenReturn(encryptedObject);
        doThrow(new EventsMessageException("mocked exception thrown!")).when(mqPublisher).publishMessageToSnsTopic(eq(false), eq(MSG_TOPIC), eq(MSG_SUBJECT), any(EventMessage.class), any(Map.class));

        SubmissionHandlerResource instance = new SubmissionHandlerResource(configuration, schemaValidation, cryptoDataManager, mongoDbOperations, mqPublisher);
        Response response = instance.mainApplicationPOST(VALID_JSON);

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(VALID_JSON), eq(SCHEMA_SERIALISATION_ENTRY), eq(ATW_SCHEMA));
        verify(cryptoDataManager, times(1)).encrypt(eq(VALID_JSON));
        verify(mongoDbOperations, times(1)).insertNewSubmissionRecord(any(AtwSubmissionItem.class), anyString(), anyString());
        verify(mqPublisher, times(1)).publishMessageToSnsTopic(eq(false), eq(MSG_TOPIC), eq(MSG_SUBJECT), any(EventMessage.class), any(Map.class));

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        assertThat("response payload should be standard", response.getEntity().toString(), is(equalTo(STANDARD_500_RESPONSE)));
    }

    @Test
    public void validPayloadWithInvalidContentsThrowsExceptionToGet500() throws IOException {
        SubmissionHandlerResource instance = new SubmissionHandlerResource(configuration, schemaValidation, cryptoDataManager, mongoDbOperations, mqPublisher);
        Response response = instance.mainApplicationPOST(INVALID_JSON_CONTENTS);

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(INVALID_JSON_CONTENTS), eq(SCHEMA_SERIALISATION_ENTRY), eq(ATW_SCHEMA));
        verifyZeroInteractions(cryptoDataManager);
        verifyZeroInteractions(mongoDbOperations);
        verifyZeroInteractions(mqPublisher);

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
        assertThat("response payload should be standard", response.getEntity().toString(), is(equalTo(PAYLOAD_ERROR_RESPONSE)));
    }

    @Test
    public void invalidPayloadThrowsExceptionToGet500() {
        SubmissionHandlerResource instance = new SubmissionHandlerResource(configuration, schemaValidation, cryptoDataManager, mongoDbOperations, mqPublisher);
        Response response = instance.mainApplicationPOST("{\"not_a_configured_node\" : \"this should fail\"}");

        verifyZeroInteractions(schemaValidation);
        verifyZeroInteractions(cryptoDataManager);
        verifyZeroInteractions(mongoDbOperations);
        verifyZeroInteractions(mqPublisher);

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        assertThat("response payload should be standard", response.getEntity().toString(), is(equalTo(STANDARD_500_RESPONSE)));
    }

    @Test
    public void brokenPayloadThrowsExceptionToGet500() {
        SubmissionHandlerResource instance = new SubmissionHandlerResource(configuration, schemaValidation, cryptoDataManager, mongoDbOperations, mqPublisher);
        Response response = instance.mainApplicationPOST("{\"not_a_configured_node : \"missing_quotation\"}");

        verifyZeroInteractions(schemaValidation);
        verifyZeroInteractions(cryptoDataManager);
        verifyZeroInteractions(mongoDbOperations);
        verifyZeroInteractions(mqPublisher);

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        assertThat("response payload should be standard", response.getEntity().toString(), is(equalTo(STANDARD_500_RESPONSE)));
    }

    @Test
    @SuppressWarnings("squid:CallToDeprecatedMethod") // allow deprecated exception for testing purposes
    public void invalidSchemaValidationThrowsExceptionToGet500() throws IOException {
        doThrow(new ValidationException("I know this implementation is deprecated but I'm just proving the exception")).when(schemaValidation).validateJsonDocumentWithFile(VALID_JSON, SCHEMA_SERIALISATION_ENTRY, ATW_SCHEMA);

        SubmissionHandlerResource instance = new SubmissionHandlerResource(configuration, schemaValidation, cryptoDataManager, mongoDbOperations, mqPublisher);
        Response response = instance.mainApplicationPOST(VALID_JSON);

        verify(schemaValidation, times(1)).validateJsonDocumentWithFile(eq(VALID_JSON), eq(SCHEMA_SERIALISATION_ENTRY), eq(ATW_SCHEMA));
        verifyZeroInteractions(cryptoDataManager);
        verifyZeroInteractions(mongoDbOperations);
        verifyZeroInteractions(mqPublisher);

        assertThat("response should be 500", response.getStatusInfo().getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
        assertThat("response payload should be standard", response.getEntity().toString(), is(equalTo(PAYLOAD_ERROR_RESPONSE)));
    }
}