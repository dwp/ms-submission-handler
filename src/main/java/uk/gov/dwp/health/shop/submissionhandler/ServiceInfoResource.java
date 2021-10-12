package uk.gov.dwp.health.shop.submissionhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.info.InfoDto;
import uk.gov.dwp.health.shop.submissionhandler.info.InfoProvider;

@Path("/version-info")
public class ServiceInfoResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInfoResource.class);
  private static final String APPLICATION_NAME_KEY = "app_name";
  private static final String VERSION_KEY = "app_version";
  private static final String BUILD_KEY = "build";
  private static final String BUILD_TIME_KEY = "build_time";
  private static final String APP = "app";
  private static final String UNKNOWN = "Unknown";

  private final InfoProvider infoProvider;

  public ServiceInfoResource(InfoProvider infoProvider) {
    this.infoProvider = infoProvider;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response serviceInfo() {

    LOGGER.debug("Service version info request received");

    Properties properties = infoProvider.getInfo();

    InfoDto infoDto =
        InfoDto.create(
            properties.getProperty(APPLICATION_NAME_KEY, UNKNOWN),
            properties.getProperty(VERSION_KEY, UNKNOWN),
            properties.getProperty(BUILD_KEY, UNKNOWN),
            properties.getProperty(BUILD_TIME_KEY, UNKNOWN));

    Map<String, InfoDto> propertiesMap = new HashMap<>();
    propertiesMap.put(APP, infoDto);

    LOGGER.debug(
        "Service version properties provided : {} {} {} {}",
        infoDto.getName(),
        infoDto.getVersion(),
        infoDto.getBuild(),
        infoDto.getBuildTime());

    return Response.status(HttpStatus.SC_OK).entity(propertiesMap).build();
  }
}
