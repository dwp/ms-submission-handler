package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.BaseItem;
import uk.gov.dwp.health.shop.submissionhandler.application.utils.ContactMethodEnum;
import org.slf4j.Logger;
import uk.gov.dwp.health.shop.submissionhandler.application.utils.ValidationLogger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactOptionItem implements BaseItem {
  private static final Logger LOG = LoggerFactory.getLogger(ContactOptionItem.class.getName());

  @JsonProperty("method")
  private String contactMethod;

  @JsonProperty("data")
  private String contactDetails;

  @JsonProperty("preferred")
  private boolean preferred;

  public String getContactMethod() {
    return contactMethod;
  }

  public String getContactDetails() {
    return contactDetails;
  }

  public boolean isPreferred() {
    return preferred;
  }

  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = getContactMethod() != null;
    if (isValid) {
      try {
        ContactMethodEnum.valueOf(this.contactMethod);

      } catch (IllegalArgumentException e) {
        LOG.debug(e.getClass().getName(), e);
        isValid = false;
      }
    }
    ValidationLogger.logOutput(LOG, "method", isValid);

    if (isValid) {
      isValid = !(null == getContactDetails() || getContactDetails().trim().isEmpty());
      ValidationLogger.logOutput(LOG, "data", isValid);
    }

    return isValid;
  }
}
