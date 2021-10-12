package uk.gov.dwp.health.shop.submissionhandler.application.handlers;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.application.SubmissionHandlerConfiguration;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.Payload;
import org.bson.Document;
import org.slf4j.Logger;

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

    MongoClientOptions.Builder optionsBuilder =
        MongoClientOptions.builder()
            .sslEnabled(sslEnabled)
            .sslInvalidHostNameAllowed(configuration.isMongoSslInvalidHostNameAllowed());
    MongoClientURI mongoClientURI =
        new MongoClientURI(configuration.getMongoDbUri().toString(), optionsBuilder);
    return new MongoClient(mongoClientURI);
  }

  public void insertNewSubmissionRecord(
      Payload itemToSave, String encryptedObject, String encryptedKey) {

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
    document.append("encrypted_message", encryptedObject);
    document.append("hash", encryptedKey);

    LOG.debug("Inserting record to collection");
    submissionsCollection.insertOne(document);
    clientConnection.close();
  }
}
