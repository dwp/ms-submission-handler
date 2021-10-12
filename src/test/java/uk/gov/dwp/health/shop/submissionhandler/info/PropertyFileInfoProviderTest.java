package uk.gov.dwp.health.shop.submissionhandler.info;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import org.junit.Test;

public class PropertyFileInfoProviderTest {
    private static final String TEST_PROPERTIES_FILE = "testinfo.properties";
    private static final String NON_EXISTENT_PROPERTIES_FILE = "doesnotexist.properties";
    private static final String APPLICATION_NAME_KEY = "app_name";
    private static final String VERSION_KEY = "app_version";
    private static final String BUILD_KEY = "build";
    private static final String APP_NAME = "my_test_app";
    private static final String APP_VERSION = "test_version";
    private static final String APP_BUILD = "1";

    @Test
    public void propertiesFileProvidesExpectedProperties() {
        PropertyFileInfoProvider info = new PropertyFileInfoProvider(TEST_PROPERTIES_FILE);
        final Properties properties = info.getInfo();

        assertThat(properties.getProperty(APPLICATION_NAME_KEY), is(APP_NAME));
        assertThat(properties.getProperty(VERSION_KEY), is(APP_VERSION));
        assertThat(properties.getProperty(BUILD_KEY), is(APP_BUILD));
    }

    @Test
    public void invalidPropertiesFileProvidesEmptyProperties() {
        PropertyFileInfoProvider info = new PropertyFileInfoProvider(NON_EXISTENT_PROPERTIES_FILE);
        final Properties properties = info.getInfo();

        assertTrue(properties.isEmpty());
    }

}
