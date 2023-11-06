package uk.gov.dwp.health.shop.submissionhandler.application.items.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.Payload;
import java.io.File;

public class SubmissionConfigurationItem {

  @NotNull
  @JsonProperty("serialisationClass")
  private Class<? extends Payload> serialisationClass;

  @NotNull
  @JsonProperty("schemaDocReference")
  private String schemaDocReference;

  @NotNull
  @JsonProperty("snsEventRoutingKey")
  private String snsEventRoutingKey;

  @NotNull
  @JsonProperty("jsonSchemaValidationDoc")
  private File jsonSchemaValidationDoc;

  public Class<? extends Payload> getSerialisationClass() {
    return serialisationClass;
  }

  public String getSnsEventRoutingKey() {
    return snsEventRoutingKey;
  }

  public File getJsonSchemaValidationDoc() {
    return jsonSchemaValidationDoc;
  }

  public String getSchemaDocReference() {
    return schemaDocReference;
  }
}
