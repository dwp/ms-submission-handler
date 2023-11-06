package uk.gov.dwp.health.shop.submissionhandler.application.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.application.SubmissionHandlerConfiguration;
import uk.gov.dwp.health.shop.submissionhandler.application.items.AtwSubmissionItem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings({"squid:S1192", "squid:S1075"}) // allow string literals and hard-coded URI's
@RunWith(MockitoJUnitRunner.class)
public class MongoDbOperationsTest {
    private static final String VALID_SUBMISSION = "{\"ref\":\"aa123\",\"date_submitted\":\"2017-02-19T12:46:31Z\",\"applicant\":{\"forenames\":\"aa\",\"surname\":\"bb\",\"dob\":\"2000-02-29\",\"residence_address\":{\"lines\":[\"line 1\",\"line 2\"],\"premises\":\"at home\",\"postcode\":\"ls6 4pt\"},\"contact_options\":[{\"method\":\"email\",\"data\":\"an.email@address.co.uk\",\"preferred\":true}],\"representative\":{\"full_name\":\"john andrew smith\",\"relationship\":\"father\",\"email\":\"email@server.com\",\"tel\":\"1234\"}},\"data_capture\":{\"pojo\":\"object\"},\"declaration\":\"I confirm\",\"tags\":[]}";
    private static final Logger LOG = LoggerFactory.getLogger(MongoDbOperationsTest.class.getName());
    private static final String TRUSTSTORE_PASS_PROPERTY = "javax.net.ssl.trustStorePassword";
    private static final String KEYSTORE_PASS_PROPERTY = "javax.net.ssl.keyStorePassword";
    private static final String TRUSTSTORE_PROPERTY = "javax.net.ssl.trustStore";
    private static final String KEYSTORE_PROPERTY = "javax.net.ssl.keyStore";
    private static AtwSubmissionItem inputItem;
    private static URI mongoDbUri;

    @Mock
    private SubmissionHandlerConfiguration configuration;

    private MongodExecutable mongodbExe;
    private MongoDbOperations instance;

    @BeforeClass
    public static void init() throws IOException, URISyntaxException {
        inputItem = new ObjectMapper().readValue(VALID_SUBMISSION, AtwSubmissionItem.class);
        mongoDbUri = new URI("mongodb://localhost:9897");
    }

    private void clearSystemProperties() {
        System.clearProperty(TRUSTSTORE_PROPERTY);
        System.clearProperty(TRUSTSTORE_PASS_PROPERTY);
        System.clearProperty(KEYSTORE_PROPERTY);
        System.clearProperty(KEYSTORE_PASS_PROPERTY);
    }

    @Before
    public void setup() throws IOException {
        clearSystemProperties();

        when(configuration.getMongoCollectionName()).thenReturn("submissions");
        when(configuration.getMongoDatabase()).thenReturn("incomingData");
        when(configuration.getMongoDbUri()).thenReturn(mongoDbUri);

        instance = new MongoDbOperations(configuration);

        LOG.info("starting embedded mondodb instance");
        MongodStarter embeddedMongo = MongodStarter.getDefaultInstance();
        MongodConfig mongodConfig = MongodConfig.builder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(mongoDbUri.getHost(), mongoDbUri.getPort(), Network.localhostIsIPv6()))
            .build();

        mongodbExe = embeddedMongo.prepare(mongodConfig);
        mongodbExe.start();
    }

    @After
    public void tearDown() {
        mongodbExe.stop();
    }

    @Test
    public void testinsertobjectHttp() {
        String testMessage = "payload-message";

        instance.insertNewSubmissionRecord(inputItem, testMessage);
        MongoDatabase db = instance.getMongoClient().getDatabase(configuration.getMongoDatabase());
        MongoCollection<Document> collection = db.getCollection(configuration.getMongoCollectionName());

        assertThat("should only be one document", collection.countDocuments(), is(equalTo(1L)));

        Document doc = collection.find().first();
        LOG.info("resolved :: {}", doc);

        assertThat(String.format("ref should equal %s", inputItem.getRef()), doc.get("ref"), is(equalTo(inputItem.getRef())));
        assertThat(String.format("date_submitted should equal %s", inputItem.getDateSubmitted()), doc.get("date_submitted"), is(equalTo(inputItem.getDateSubmittedInstant().toEpochMilli())));
        assertThat(String.format("message should be equal '%s'", testMessage), doc.get("message"), is(equalTo(testMessage)));
    }

    @Test
    public void testNoTrustStoreMeansNoSSLSetting() throws URISyntaxException {
        when(configuration.getMongoDbUri()).thenReturn(new URI("mongodb://a-secure-endpoint:9897"));
        MongoClientImpl localClient =
                (MongoClientImpl) new MongoDbOperations(configuration).getMongoClient();

        assertThat(
                "should be configured without ssl",
                localClient.getSettings().getSslSettings().isEnabled(),
                is(equalTo(false)));
        assertNull("no truststore value", System.getProperty(TRUSTSTORE_PROPERTY));
        assertNull("no truststore pass", System.getProperty(TRUSTSTORE_PASS_PROPERTY));
        assertNull("no keystore value", System.getProperty(KEYSTORE_PROPERTY));
        assertNull("no keystore pass", System.getProperty(KEYSTORE_PASS_PROPERTY));
    }

    @Test
    public void testNoKeyStoreMeansSSLTrustOnlySetting() throws URISyntaxException {
        when(configuration.getMongoDbUri()).thenReturn(new URI("mongodb://a-secure-endpoint:9897"));
        when(configuration.getMongoDbSslTruststoreFilename()).thenReturn("truststore.ts");
        when(configuration.getMongoDbSslTruststorePassword()).thenReturn("password");

        MongoClientImpl localClient =
                (MongoClientImpl) new MongoDbOperations(configuration).getMongoClient();

        assertThat(
                "should be configured with ssl",
                localClient.getSettings().getSslSettings().isEnabled(),
                is(equalTo(true)));
        assertNotNull("truststore value", System.getProperty(TRUSTSTORE_PROPERTY));
        assertNotNull("truststore pass", System.getProperty(TRUSTSTORE_PASS_PROPERTY));
        assertNull("no keystore value", System.getProperty(KEYSTORE_PROPERTY));
        assertNull("no keystore pass", System.getProperty(KEYSTORE_PASS_PROPERTY));
    }

    @Test
    public void testKeyStoreMeansSSLMutualSetting() throws URISyntaxException {
        when(configuration.getMongoDbUri()).thenReturn(new URI("mongodb://a-secure-endpoint:9897"));
        when(configuration.getMongoDbSslTruststoreFilename()).thenReturn("truststore.ts");
        when(configuration.getMongoDbSslTruststorePassword()).thenReturn("password_ts");
        when(configuration.getMongoDbSslKeystoreFilename()).thenReturn("keystore.ts");
        when(configuration.getMongoDbSslKeystorePassword()).thenReturn("password_ks");

        MongoClientImpl localClient =
                (MongoClientImpl) new MongoDbOperations(configuration).getMongoClient();

        assertThat(
                "should be configured with ssl",
                localClient.getSettings().getSslSettings().isEnabled(),
                is(equalTo(true)));
        assertNotNull("no truststore value", System.getProperty(TRUSTSTORE_PROPERTY));
        assertNotNull("no truststore pass", System.getProperty(TRUSTSTORE_PASS_PROPERTY));
        assertNotNull("no keystore value", System.getProperty(KEYSTORE_PROPERTY));
        assertNotNull("no keystore pass", System.getProperty(KEYSTORE_PASS_PROPERTY));
    }

    @Test
    public void testSSLHostnameSettingIsTrue() throws URISyntaxException {
        when(configuration.getMongoDbUri()).thenReturn(new URI("mongodb://a-secure-endpoint:9897"));
        when(configuration.isMongoSslInvalidHostNameAllowed()).thenReturn(true);

        MongoClientImpl localClient =
                (MongoClientImpl) new MongoDbOperations(configuration).getMongoClient();

        assertThat(
                "should be configured with ssl",
                localClient.getSettings().getSslSettings().isEnabled(),
                is(equalTo(false)));
        assertThat(
                "should be configured with ssl",
                localClient.getSettings().getSslSettings().isInvalidHostNameAllowed(),
                is(equalTo(true)));
    }

    @Test
    public void testStableApi() {
        when(configuration.isMongoStableApiEnabled()).thenReturn(true);
        when(configuration.getMongoStableApiVersion()).thenReturn(ServerApiVersion.V1);
        when(configuration.isMongoStableApiStrict()).thenReturn(true);

        MongoClientImpl localClient =
                (MongoClientImpl) new MongoDbOperations(configuration).getMongoClient();

        assertThat(
                "should have stable api enabled",
                localClient.getSettings().getServerApi(),
                is(notNullValue()));
        assertThat(
                "should have api version set correctly",
                localClient.getSettings().getServerApi().getVersion(),
                is(equalTo(ServerApiVersion.V1)));
        assertThat(
                "should have strictness enabled",
                localClient.getSettings().getServerApi().getStrict().orElse(false),
                is(equalTo(true)));
    }
}
