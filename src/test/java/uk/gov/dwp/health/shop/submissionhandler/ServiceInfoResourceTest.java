package uk.gov.dwp.health.shop.submissionhandler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.dwp.health.shop.submissionhandler.info.InfoDto;

@RunWith(MockitoJUnitRunner.class)
public class ServiceInfoResourceTest {

    private static final String APP = "app";
    private static final String UNKNOWN = "Unknown";
    private static final String APP_NAME = "app name";
    private static final String APP_VERSION = "app version";
    private static final int OK = HttpStatus.SC_OK;
    private static final String BUILD = "1";
    private static final String APPLICATION_NAME_KEY = "app_name";
    private static final String VERSION_KEY = "app_version";
    private static final String BUILD_KEY = "build";

    @Test
    public void validServiceInfoGivesOkResponseAndServiceDetails() {

        Properties properties = new Properties();
        properties.setProperty(APPLICATION_NAME_KEY, APP_NAME);
        properties.setProperty(VERSION_KEY, APP_VERSION);
        properties.setProperty(BUILD_KEY, BUILD);
        ServiceInfoResource resource = new ServiceInfoResource(() -> properties);

        Response response = resource.serviceInfo();

        final Map<String, InfoDto> entity = (Map<String, InfoDto>) response.getEntity();

        assertThat(response.getStatus(), is(OK));
        assertThat(entity.get(APP).getName(), is(APP_NAME));
        assertThat(entity.get(APP).getVersion(), is(APP_VERSION));
        assertThat(entity.get(APP).getBuild(), is(BUILD));

    }

    @Test
    public void noServiceInfoGivesOkResponseAndUnknownDetails() {

        Properties properties = new Properties();
        ServiceInfoResource resource = new ServiceInfoResource(() -> properties);

        Response response = resource.serviceInfo();

        final Map<String, InfoDto> entity = (Map<String, InfoDto>) response.getEntity();

        assertThat(response.getStatus(), is(OK));
        assertThat(entity.get(APP).getName(), is(UNKNOWN));
        assertThat(entity.get(APP).getVersion(), is(UNKNOWN));
        assertThat(entity.get(APP).getBuild(), is(UNKNOWN));

    }
}
