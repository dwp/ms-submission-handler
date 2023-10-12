package uk.gov.dwp.health.shop.submissionhandler.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.ServerApiVersion;
import uk.gov.dwp.health.messageq.amazon.items.AmazonConfigBase;
import uk.gov.dwp.health.shop.submissionhandler.application.items.config.SubmissionConfigurationItem;
import io.dropwizard.Configuration;
import uk.gov.dwp.crypto.SecureStrings;
import uk.gov.dwp.health.crypto.CryptoConfig;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class SubmissionHandlerConfiguration extends Configuration {
  private final SecureStrings cipher;

  @NotNull
  @JsonProperty("mongoDbUri")
  private URI mongoDbUri;

  @NotNull
  @JsonProperty("mongoDatabase")
  private String mongoDatabase;

  @NotNull
  @JsonProperty("mongoCollectionName")
  private String mongoCollectionName;

  @NotNull
  @JsonProperty("mongoSslInvalidHostNameAllowed")
  private boolean mongoSslInvalidHostNameAllowed;

  @JsonProperty("mongoDbSslTruststoreFilename")
  private String mongoDbSslTruststoreFilename;

  @JsonProperty("mongoDbSslKeystoreFilename")
  private String mongoDbSslKeystoreFilename;

  @JsonProperty("mongoDbSslTruststorePassword")
  private SealedObject mongoDbSslTruststorePassword;

  @JsonProperty("mongoDbSslKeystorePassword")
  private SealedObject mongoDbSslKeystorePassword;

  @JsonProperty("mongoStableApiVersion")
  private String mongoStableApiVersion;

  @JsonProperty("mongoStableApiStrict")
  private boolean mongoStableApiStrict = true;

  @NotNull
  @JsonProperty("mqCryptoConfiguration")
  private CryptoConfig mqCryptoConfig;

  @NotNull
  @JsonProperty("snsTopicName")
  private String snsTopicName;

  @NotNull
  @JsonProperty("snsSubject")
  private String snsSubject;

  @JsonProperty("snsEncryptMessages")
  private boolean snsEncryptMessages = true;

  @NotNull
  @JsonProperty("snsConfiguration")
  private AmazonConfigBase snsConfiguration;

  @JsonProperty("applicationInfoEnabled")
  private boolean applicationInfoEnabled;

  @NotNull
  @JsonProperty("submissionServices")
  private Map<String, SubmissionConfigurationItem> submissionServices;

  public SubmissionHandlerConfiguration() throws
      NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    cipher = new SecureStrings();
  }

  public String getMongoDbSslTruststoreFilename() {
    return mongoDbSslTruststoreFilename;
  }

  public String getMongoDbSslKeystoreFilename() {
    return mongoDbSslKeystoreFilename;
  }

  public String getMongoDbSslTruststorePassword() {
    return cipher.revealString(mongoDbSslTruststorePassword);
  }

  public void setMongoDbSslTruststorePassword(String mongoDbSslTruststorePassword) throws
      IllegalBlockSizeException, IOException {
    this.mongoDbSslTruststorePassword = cipher.sealString(mongoDbSslTruststorePassword);
  }

  public String getMongoDbSslKeystorePassword() {
    return cipher.revealString(mongoDbSslKeystorePassword);
  }

  public void setMongoDbSslKeystorePassword(String mongoDbSslKeystorePassword) throws
      IllegalBlockSizeException, IOException {
    this.mongoDbSslKeystorePassword = cipher.sealString(mongoDbSslKeystorePassword);
  }

  public String getMongoDatabase() {
    return mongoDatabase;
  }

  public String getMongoCollectionName() {
    return mongoCollectionName;
  }

  public URI getMongoDbUri() {
    return mongoDbUri;
  }

  public boolean isMongoStableApiEnabled() {
    return mongoStableApiVersion != null;
  }

  public ServerApiVersion getMongoStableApiVersion() {
    if (isMongoStableApiEnabled()) {
      return ServerApiVersion.findByValue(mongoStableApiVersion);
    } else {
      return null;
    }
  }

  public boolean isMongoStableApiStrict() {
    return mongoStableApiStrict;
  }

  public CryptoConfig getMQCryptoConfig() {
    return mqCryptoConfig;
  }

  public boolean isApplicationInfoEnabled() {
    return applicationInfoEnabled;
  }

  public Map<String, SubmissionConfigurationItem> getSubmissionServices() {
    return submissionServices;
  }

  public String getSnsTopicName() {
    return snsTopicName;
  }

  public String getSnsSubject() {
    return snsSubject;
  }

  public boolean isSnsEncryptMessages() {
    return snsEncryptMessages;
  }

  public AmazonConfigBase getSnsConfiguration() {
    return snsConfiguration;
  }

  public boolean isMongoSslInvalidHostNameAllowed() {
    return mongoSslInvalidHostNameAllowed;
  }
}
