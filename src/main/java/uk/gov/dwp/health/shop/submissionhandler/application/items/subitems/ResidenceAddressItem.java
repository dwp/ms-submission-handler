package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.BaseItem;
import org.slf4j.Logger;
import uk.gov.dwp.health.shop.submissionhandler.application.utils.ValidationLogger;
import uk.gov.dwp.regex.PostCodeValidator;

import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResidenceAddressItem implements BaseItem {
  private static final Logger LOG = LoggerFactory.getLogger(ResidenceAddressItem.class.getName());

  @JsonProperty("lines")
  private String[] addressLines;

  @JsonProperty("premises")
  private String premises;

  @JsonProperty("postcode")
  private String postcode;

  public String[] getAddressLines() {
    return addressLines != null ? Arrays.copyOf(addressLines, addressLines.length) : null;
  }

  public String getPremises() {
    return premises;
  }

  public String getPostcode() {
    return postcode;
  }

  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = true;

    if (null != getPostcode()) {
      isValid = PostCodeValidator.validateInput(getPostcode());
      ValidationLogger.logOutput(LOG, "postcode", isValid);
    }

    return isValid;
  }
}
