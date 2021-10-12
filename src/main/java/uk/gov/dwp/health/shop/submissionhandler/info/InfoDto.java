package uk.gov.dwp.health.shop.submissionhandler.info;

public class InfoDto {

  private String name;
  private String version;
  private String build;
  private String buildTime;

  private InfoDto() {
    // prevent instantiation
  }

  public static InfoDto create(String name, String version, String build, String buildTime) {
    InfoDto dto = new InfoDto();
    dto.name = name;
    dto.version = version;
    dto.build = build;
    dto.buildTime = buildTime;
    return dto;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getBuild() {
    return build;
  }

  public String getBuildTime() {
    return buildTime;
  }
}
