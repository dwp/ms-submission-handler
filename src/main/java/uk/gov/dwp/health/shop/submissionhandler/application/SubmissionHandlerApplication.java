package uk.gov.dwp.health.shop.submissionhandler.application;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import uk.gov.dwp.health.crypto.MessageEncoder;
import uk.gov.dwp.health.messageq.amazon.sns.MessagePublisher;
import uk.gov.dwp.health.shop.submissionhandler.ServiceInfoResource;
import uk.gov.dwp.health.shop.submissionhandler.SubmissionHandlerResource;
import uk.gov.dwp.health.shop.submissionhandler.application.handlers.MongoDbOperations;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.shop.submissionhandler.info.PropertyFileInfoProvider;
import uk.gov.dwp.health.utilities.JsonObjectSchemaValidation;

public class SubmissionHandlerApplication extends Application<SubmissionHandlerConfiguration> {

  public static void main(String[] args) throws Exception {
    new SubmissionHandlerApplication().run(args);
  }

  @Override
  protected void bootstrapLogging() {
    // to prevent dropwizard using its own standard logger
  }

  @Override
  public void initialize(Bootstrap<SubmissionHandlerConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
  }

  @Override
  public void run(SubmissionHandlerConfiguration configuration, Environment environment)
      throws Exception {
    final JsonObjectSchemaValidation schemaValidation = new JsonObjectSchemaValidation();

    final CryptoDataManager mqCryptoDataManager =
        new CryptoDataManager(configuration.getMQCryptoConfig());
    final CryptoDataManager mongoCryptoDataManager =
        new CryptoDataManager(configuration.getMongoCryptoConfig());

    final MessageEncoder<MessageAttributeValue> mqMessageEncoder =
        new MessageEncoder<>(mqCryptoDataManager, MessageAttributeValue.class);
    final MessagePublisher messagePublisher =
        new MessagePublisher(mqMessageEncoder, configuration.getSnsConfiguration());

    final MongoDbOperations mongoDbOperations = new MongoDbOperations(configuration);

    final SubmissionHandlerResource instance =
        new SubmissionHandlerResource(
            configuration,
            schemaValidation,
            mongoCryptoDataManager,
            mongoDbOperations,
            messagePublisher);

    environment.jersey().register(instance);

    if (configuration.isApplicationInfoEnabled()) {
      final ServiceInfoResource infoInstance =
          new ServiceInfoResource(new PropertyFileInfoProvider("application.yml"));
      environment.jersey().register(infoInstance);
    }
  }
}
