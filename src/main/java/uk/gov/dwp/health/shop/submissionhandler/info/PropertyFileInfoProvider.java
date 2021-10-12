package uk.gov.dwp.health.shop.submissionhandler.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyFileInfoProvider implements InfoProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFileInfoProvider.class);
  private final String resourcePath;

  public PropertyFileInfoProvider(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  public Properties getInfo() {
    Properties properties = new Properties();
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      properties.load(inputStream);

    } catch (IOException | IllegalArgumentException | NullPointerException e) {
      LOGGER.warn("Failed to load info resource at : {}", resourcePath);
      LOGGER.debug("Failed to load resource", e);
    }
    return properties;
  }
}
