package uk.gov.dwp.health.shop.submissionhandler.integration.cucumber;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.crypto.CryptoConfig;
import uk.gov.dwp.health.crypto.CryptoDataManager;
import uk.gov.dwp.health.crypto.CryptoMessage;
import uk.gov.dwp.health.crypto.exception.CryptoException;
import uk.gov.dwp.health.messageq.EventConstants;
import uk.gov.dwp.health.messageq.amazon.items.AmazonConfigBase;
import uk.gov.dwp.health.messageq.amazon.items.messages.SnsMessageClassItem;
import uk.gov.dwp.health.messageq.amazon.utils.AmazonQueueUtilities;
import uk.gov.dwp.health.messageq.items.event.EventMessage;
import uk.gov.dwp.health.shop.submissionhandler.application.items.AtwSubmissionItem;
import uk.gov.dwp.tls.TLSConnectionBuilder;
import uk.gov.dwp.tls.TLSGeneralException;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class CucumberStepDefs {
    private static final Logger LOG = LoggerFactory.getLogger(CucumberStepDefs.class.getName());
    private static final String TRUSTSTORE_FILE = "src/test/resources/submissions-handler-tls-server.ts";
    private static final String KEYSTORE_FILE = "src/test/resources/cucumber-test.ks";

    private static final ConnectionString MONGO_CLIENT_URI = new ConnectionString("mongodb://mongo-db:9897");
    private static final String LOCALSTACK_CONTAINER_HOST = "http://localstack";
    private static final String MONGO_COLLECTION_NAME = "submissions";
    private static final String MONGO_DATABASE = "incomingData";
    private static final String CERT_PASS = "password";

    private AmazonQueueUtilities amazonQueueUtilities;
    private CryptoDataManager awsKmsCryptoClass;
    private SnsMessageClassItem queueMessage;
    private CloseableHttpResponse response;
    private CloseableHttpClient httpClient;
    private CryptoConfig cryptoConfig;
    private String payload;

    @Before
    public void setup() throws CryptoException, IOException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, TLSGeneralException {

        // create local properties to negate KMS & LocalStack from needing to access Metadata Service for IAM role privs
        System.setProperty("aws.accessKeyId", "this_is_my_system_property_key");
        System.setProperty("aws.secretKey", "abcd123456789");

        AmazonConfigBase snsConfig = new AmazonConfigBase();
        snsConfig.setS3EndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4572");
        snsConfig.setEndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4575");
        snsConfig.setLargePayloadSupportEnabled(false);
        snsConfig.setPathStyleAccessEnabled(true);
        snsConfig.setS3BucketName("sns-bucket");
        snsConfig.setRegion(Regions.US_EAST_1);

        AmazonConfigBase sqsConfig = new AmazonConfigBase();
        sqsConfig.setS3EndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4572");
        sqsConfig.setEndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4576");
        sqsConfig.setLargePayloadSupportEnabled(false);
        sqsConfig.setPathStyleAccessEnabled(true);
        sqsConfig.setS3BucketName("sns-bucket");
        sqsConfig.setRegion(Regions.US_EAST_1);

        amazonQueueUtilities = new AmazonQueueUtilities(sqsConfig, snsConfig);

        cryptoConfig = new CryptoConfig("alias/test_mq_request_id");
        cryptoConfig.setKmsEndpointOverride(LOCALSTACK_CONTAINER_HOST + ":4599");
        awsKmsCryptoClass = new CryptoDataManager(cryptoConfig);

        try (MongoClient mongoClient = MongoClients.create(MONGO_CLIENT_URI)) {
            var mongoDatabase = mongoClient.getDatabase(MONGO_DATABASE);
            mongoDatabase.drop();
        }

        LOG.info("configuring closable http client");
        TLSConnectionBuilder sslClient = new TLSConnectionBuilder(TRUSTSTORE_FILE, CERT_PASS, KEYSTORE_FILE, CERT_PASS);
        httpClient = sslClient.configureSSLConnection();

        LOG.info("initialise payload and queue objects");
        queueMessage = null;
        payload = null;
    }

    @When("^I hit \"([^\"]*)\" with an empty POST request$")
    public void iHitWithAGETRequest(String url) throws IOException {
        performHttpPostWithUriOf(url, "");
    }

    @When("^I hit the service url \"([^\"]*)\" with the following json body$")
    public void hitServiceUrlWithSpecifiedJson(String url, Map<String, String> jsonValues) throws IOException {
        String jsonRequestBody = buildJsonBody(jsonValues);
        performHttpPostWithUriOf(url, jsonRequestBody);
    }

    @When("^I hit the service url \"([^\"]*)\" with the following json body taken from file \"([^\"]*)\" with msg_id \"([^\"]*)\" and submission time of now plus (\\d+) days$")
    public void iHitTheServiceUrlWithTheFollowingJsonBodyTakenFromFile(String url, String fileRef, String msgId, int daysOffset) throws IOException {
        String isoDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now().plus(Duration.ofDays(daysOffset)));
        String json = String.format(FileUtils.readFileToString(new File(fileRef)), msgId, isoDate);
        performHttpPostWithUriOf(url, json);
    }

    @Then("^I should get a (\\d+) response$")
    public void iShouldGetAResponse(int status) {
        assertThat("response codes do not match", response.getStatusLine().getStatusCode(), is(equalTo(status)));
    }

    @And("^The response payload contains the following text : \"([^\"]*)\"$")
    public void theResponsePayloadIsEmptyContainsTheFollowingText(String searchText) {
        assertNotNull("cannot be null", payload);
        assertThat(String.format("payload should contain %s", searchText), payload, containsString(searchText));
    }

    @And("^The response payload is empty$")
    public void theResponsePayloadIsEmpty() {
        assertTrue("should be empty", payload.trim().isEmpty());
    }

    @When("^I hit \"([^\"]*)\" with the a JSON string of \"([^\"]*)\"$")
    public void iHitWithTheAJSONStringOf(String url, String json) throws IOException {
        performHttpPostWithUriOf(url, json);
    }

    @And("^I create an sns topic named \"([^\"]*)\"$")
    public void iSetupAStandardRabbitmqExchangeCalled(String topicName) {
        amazonQueueUtilities.createTopic(topicName);
    }

    @And("^I create a catch all subscription for queue name \"([^\"]*)\" binding to topic \"([^\"]*)\"$")
    public void iCreateACatchAllSubscriptionForQueueNameBindingToExchange(String queueName, String topicName) {
        amazonQueueUtilities.createQueue(queueName);
        amazonQueueUtilities.subscribeQueueToTopic(queueName, topicName);
    }

    @And("^I create a queue called \"([^\"]*)\" binding to topic \"([^\"]*)\" with filter policy \"([^\"]*)\"$")
    public void iInitialiseAQueueCalledBindingToExchangeWithTheFollowingBinding(String queueName, String topicName, String routingKey) {
        amazonQueueUtilities.createQueue(queueName);
        amazonQueueUtilities.subscribeQueueToTopicWithRoutingKeyPolicy(queueName, topicName, routingKey);
    }

    @And("^The received message has a routing key \"([^\"]*)\"$")
    public void whenConsumedFromQueueTheWaitingMessageHasARoutingKey(String routingKey) {
        assertThat(String.format("expecting routing key '%s'", routingKey), queueMessage.getMessageAttributes().get(EventConstants.ROUTING_KEY_MARKER).getStringValue(), is(equalTo(routingKey)));
    }

    @And("^a message is successfully removed from the queue, there were a total of (\\d+) messages on queue \"([^\"]*)\"$")
    public void thereIsPendingMessageOnQueue(int totalMessages, String queueName) throws IOException, CryptoException {
        List<Message> queueMessages = amazonQueueUtilities.receiveMessages(queueName, amazonQueueUtilities.getS3Sqs());

        assertThat("mismatched messages", queueMessages.size(), Is.is(equalTo(totalMessages)));

        assertNotNull("queue contents are null", queueMessages);
        amazonQueueUtilities.deleteMessageFromQueue(queueName, queueMessages.get(0).getReceiptHandle());
        queueMessage = decodeQueueMessage(queueMessages.get(0));
    }

    @And("^The response payload contains a valid return UUID correlationId$")
    public void theResponsePayloadContainsAValidReturnUUIDCorrelationId() {
        UUID correlationId = UUID.fromString(payload);
        assertNotNull(correlationId);
    }

    @And("^The message body content is an AtwSubmissionItem with reference \"([^\"]*)\"$")
    public void theMessageBodyContentIsAValidSubmissionObjectWithReference(String ref) throws IOException {

        AtwSubmissionItem item = new ObjectMapper().readValue(queueMessage.getMessage(), AtwSubmissionItem.class);

        assertNotNull("AtwSubmissionItem cannot be null", item);
        assertThat("the contents should be valid", item.isContentValid(), is(equalTo(true)));
        assertThat(String.format("ref should be '%s'", ref), item.getRef(), is(equalTo(ref)));
    }

    @And("^The following message body json items are (present|missing)$")
    public void theMessageBodyJsonContentsAreMissingAnd(String state, List<String> fields) {
        boolean present = ((state != null) && state.equalsIgnoreCase("PRESENT"));

        String jsonPayload = queueMessage.getMessage();
        assertNotNull(jsonPayload);

        for (String item : fields) {
            assertThat(String.format("%s contain element %s", present ? "must" : "cannot", item), jsonPayload.contains(item), is(equalTo(present)));
        }
    }

    @Then("^I read the mongo database and there are (\\d+) entries")
    public void iReadTheMongoDatabaseAndThereIsASingleEntryWithReference(long count) {
        long recordCount;

        try (MongoClient mongoClient = MongoClients.create(MONGO_CLIENT_URI)) {
            recordCount = mongoClient.getDatabase(MONGO_DATABASE).getCollection(MONGO_COLLECTION_NAME).countDocuments();
        }

        assertThat(String.format("should only be %d collections", count), recordCount, is(equalTo(count)));
    }

    @And("^There is an entry containing the reference \"([^\"]*)\" and the original submitted time$")
    public void iReadTheMongoDatabaseAndThereIsAnEntryWithReference(String ref) throws IOException {
        AtwSubmissionItem item = new ObjectMapper().readValue(queueMessage.getMessage(), AtwSubmissionItem.class);
        Document doc;

        BasicDBObject query = new BasicDBObject();
        query.put("ref", ref);

        try (MongoClient mongoClient = MongoClients.create(MONGO_CLIENT_URI)) {
            doc = mongoClient.getDatabase(MONGO_DATABASE).getCollection(MONGO_COLLECTION_NAME).find(query).first();
        }

        assertNotNull(doc);

        LOG.info("resolved :: {}", doc);
        assertThat(String.format("ref should equal %s", ref), doc.get("ref"), is(equalTo(ref)));
        assertThat(String.format("datesubmitted should equal %s", item.getDateSubmitted()), doc.get("date_submitted"), is(equalTo(item.getDateSubmittedInstant().toEpochMilli())));
    }

    @And("^The decrypted mongo entry with ref \"([^\"]*)\" has matching contents to the original submitted item$")
    public void theDecryptedMongoContentsHasMatchingContentsToTheOriginalSubmittedItem(String ref) throws IOException, CryptoException {
        AtwSubmissionItem queueItem = new ObjectMapper().readValue(queueMessage.getMessage(), AtwSubmissionItem.class);
        Document doc;

        BasicDBObject query = new BasicDBObject();
        query.put("ref", ref);

        try (MongoClient mongoClient = MongoClients.create(MONGO_CLIENT_URI)) {
            doc = mongoClient.getDatabase(MONGO_DATABASE).getCollection(MONGO_COLLECTION_NAME).find(query).first();
        }

        assertNotNull(doc);

        EventMessage mongoObject = decryptContents(doc.get("encrypted_message").toString(), doc.get("hash").toString());
        AtwSubmissionItem mongoItem = new ObjectMapper().readValue(mongoObject.serialisedBodyContentsToJson(), AtwSubmissionItem.class);

        assertTrue(queueItem.isContentValid());
        assertTrue(mongoItem.isContentValid());

        assertEquals(queueItem.getDateSubmittedInstant().toString(), mongoItem.getDateSubmittedInstant().toString());
        assertEquals(queueItem.getDeclaration(), mongoItem.getDeclaration());
        assertEquals(queueItem.getRef(), mongoItem.getRef());
    }

    @And("^There were no messages on queue \"([^\"]*)\"$")
    public void thereWereNoMessagesOnTheQueue(String queueName) throws IOException {
        List<Message> queueMessages = amazonQueueUtilities.receiveMessages(queueName, amazonQueueUtilities.getS3Sqs());
        assertNotNull(queueMessages);
        assertTrue(queueMessages.isEmpty());
    }

    private SnsMessageClassItem decodeQueueMessage(Message sqsMessage) throws IOException, CryptoException {
        assertNotNull(sqsMessage);

        SnsMessageClassItem snsMessageClass = new SnsMessageClassItem().buildMessageClassItem(sqsMessage.getBody());
        String msgContents = snsMessageClass.getMessage();

        if (snsMessageClass.getMessageAttributes().get(EventConstants.KMS_DATA_KEY_MARKER) != null) {
            CryptoMessage cryptoMessage = new CryptoMessage();
            cryptoMessage.setKey(snsMessageClass.getMessageAttributes().get(EventConstants.KMS_DATA_KEY_MARKER).getStringValue());
            cryptoMessage.setMessage(msgContents);

            snsMessageClass.setMessage(awsKmsCryptoClass.decrypt(cryptoMessage));
        }

        return snsMessageClass;
    }

    private String buildJsonBody(Map<String, String> jsonValues) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        String delimiter = "";
        for (Map.Entry<String, String> jsonKeyValue : jsonValues.entrySet()) {
            if (!jsonKeyValue.getKey().isEmpty()) {
                builder.append(delimiter);
                builder.append(String.format("\"%s\":", jsonKeyValue.getKey()));
                builder.append(jsonKeyValue.getValue());
                delimiter = ",";
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private void performHttpPostWithUriOf(String uri, String body) throws IOException {
        HttpPost httpUriRequest = new HttpPost(uri);

        if ((body != null) && (!body.isEmpty())) {
            HttpEntity entity = new StringEntity(body);
            httpUriRequest.setEntity(entity);
        }

        response = httpClient.execute(httpUriRequest);
        HttpEntity responseEntity = response.getEntity();
        payload = EntityUtils.toString(responseEntity);
    }

    private EventMessage decryptContents(String encryptedMessage, String dataKey) throws CryptoException, IOException {
        CryptoDataManager cryptoDataManager = new CryptoDataManager(cryptoConfig);
        LOG.info("Creating new CryptoMessage object for decryption");

        CryptoMessage messageToDecrypt = new CryptoMessage();
        messageToDecrypt.setMessage(encryptedMessage);
        messageToDecrypt.setKey(dataKey);

        LOG.info("Decrypting message...");
        EventMessage messageObject = new EventMessage();
        messageObject.setBodyContents(new ObjectMapper().readValue(cryptoDataManager.decrypt(messageToDecrypt), Object.class));

        return messageObject;
    }

}
