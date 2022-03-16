package uk.gov.dwp.health.shop.submissionhandler.application.handlers;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.application.SubmissionHandlerConfiguration;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.Payload;

import javax.inject.Inject;

public class MongoDbOperations {
  private static final Logger LOG = LoggerFactory.getLogger(MongoDbOperations.class.getName());
  private final SubmissionHandlerConfiguration configuration;

  @Inject
  public MongoDbOperations(SubmissionHandlerConfiguration configuration) {
    this.configuration = configuration;
  }

  protected MongoClient getMongoClient() {
    boolean sslEnabled = configuration.getMongoDbSslTruststoreFilename() != null;

    LOG.debug(
        "create new mongo connection to {} :: sslEnabled = {}",
        configuration.getMongoDbUri(),
        sslEnabled);
    if (sslEnabled) {
      System.setProperty(
          "javax.net.ssl.trustStore", configuration.getMongoDbSslTruststoreFilename());
      System.setProperty(
          "javax.net.ssl.trustStorePassword", configuration.getMongoDbSslTruststorePassword());

      if (configuration.getMongoDbSslKeystoreFilename() != null) {
        System.setProperty("javax.net.ssl.keyStore", configuration.getMongoDbSslKeystoreFilename());
        System.setProperty(
            "javax.net.ssl.keyStorePassword", configuration.getMongoDbSslKeystorePassword());
      }
    }

    var mongoClientSettings = MongoClientSettings.builder()
        .applyToSslSettings(
            builder -> {
              builder.enabled(sslEnabled);
              builder.invalidHostNameAllowed(
                  configuration.isMongoSslInvalidHostNameAllowed());
            })
        .applyConnectionString(
            new ConnectionString(configuration.getMongoDbUri().toString()));

    if (configuration.isMongoStableApiEnabled()) {
      mongoClientSettings.serverApi(ServerApi.builder()
          .strict(configuration.isMongoStableApiStrict())
          .version(configuration.getMongoStableApiVersion())
          .build());
    }


    return MongoClients.create(mongoClientSettings.build());
  }

  public void insertNewSubmissionRecord(
      Payload itemToSave, String message) {

    LOG.debug(
        "create connection to {} and attach to database {}",
        configuration.getMongoDbUri(),
        configuration.getMongoDatabase());
    MongoClient clientConnection = getMongoClient();
    MongoDatabase database = clientConnection.getDatabase(configuration.getMongoDatabase());

    LOG.debug("attach to collection {}", configuration.getMongoCollectionName());
    MongoCollection<Document> submissionsCollection =
        database.getCollection(configuration.getMongoCollectionName());

    submissionsCollection.createIndex(
        Indexes.ascending("ref"), new IndexOptions().unique(true).sparse(true));

    Document document = new Document();
    document.append("date_submitted", itemToSave.getDateSubmittedInstant().toEpochMilli());
    document.append("ref", itemToSave.getRef());
    document.append("message", message);

    LOG.debug("Inserting record to collection");
    submissionsCollection.insertOne(document);
    clientConnection.close();
  }
}
