package uk.gov.dwp.health.shop.submissionhandler.info;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InfoDtoTest {

    private static final String APPLICATION_NAME = "application name";
    private static final String APPLICATION_VERSION = "application version";
    private static final String BUILD = "123F3";
    private static final String BUILD_TIME = "2018-01-01T12:00:00Z";

    @Test
    public void validDtoCreated() {
        InfoDto dto = InfoDto.create(APPLICATION_NAME, APPLICATION_VERSION, BUILD, BUILD_TIME);

        assertThat(dto.getName(), is(APPLICATION_NAME));
        assertThat(dto.getVersion(), is(APPLICATION_VERSION));
        assertThat(dto.getBuild(), is(BUILD));
        assertThat(dto.getBuildTime(), is(BUILD_TIME));
    }

}
