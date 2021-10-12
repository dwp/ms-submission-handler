package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.BaseItem;
import org.slf4j.Logger;
import uk.gov.dwp.health.shop.submissionhandler.application.utils.ValidationLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicantItem implements BaseItem {
  private static final Logger LOG = LoggerFactory.getLogger(ApplicantItem.class.getName());

  @JsonProperty("forenames")
  private String forenames;

  @JsonProperty("surname")
  private String surname;

  @JsonProperty("dob")
  private String dateOfBirth;

  @JsonProperty("residence_address")
  private ResidenceAddressItem residenceAddress;

  @JsonProperty("contact_options")
  private ContactOptionItem[] contactOptionsItem;

  @JsonProperty("representative")
  private RepresentativeItem representativeItem;

  public String getForenames() {
    return forenames;
  }

  public String getSurname() {
    return surname;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public ResidenceAddressItem getResidenceAddress() {
    return residenceAddress;
  }

  public ContactOptionItem[] getContactOptionsItem() {
    return contactOptionsItem != null
        ? Arrays.copyOf(contactOptionsItem, contactOptionsItem.length)
        : null;
  }

  public RepresentativeItem getRepresentativeItem() {
    return representativeItem;
  }

  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = !(null == getForenames() || getForenames().trim().isEmpty());
    ValidationLogger.logOutput(LOG, "forenames", isValid);

    if (isValid && null != getDateOfBirth()) {
      isValid = validDateOfBirth();
      ValidationLogger.logOutput(LOG, "dob", isValid);
    }

    if (isValid && null != getResidenceAddress()) {
      isValid = getResidenceAddress().isContentValid();
      ValidationLogger.logOutput(LOG, "residence_address", isValid);
    }

    if (isValid && null != getContactOptionsItem() && getContactOptionsItem().length > 0) {
      isValid = validateContactContents();
      ValidationLogger.logOutput(LOG, "contact_options", isValid);
    }

    return isValid;
  }

  @JsonIgnore
  private boolean validDateOfBirth() {
    boolean dateValid;

    try {
      SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
      dtFormat.setLenient(false);

      dateValid = new Date().after(dtFormat.parse(getDateOfBirth()));

    } catch (ParseException e) {
      LOG.debug("dob is invalid :: {}", e.getMessage());
      dateValid = false;
    }

    return dateValid;
  }

  @JsonIgnore
  private boolean validateContactContents() {
    boolean contentsValid = false;

    if (getContactOptionsItem() != null) {
      for (ContactOptionItem item : getContactOptionsItem()) {
        contentsValid = item.isContentValid();
        if (!contentsValid) {
          break;
        }
      }
    }

    return contentsValid;
  }
}
