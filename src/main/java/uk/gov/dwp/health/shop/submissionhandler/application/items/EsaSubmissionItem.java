package uk.gov.dwp.health.shop.submissionhandler.application.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.esao.shared.models.DataCapture;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.Payload;
import uk.gov.dwp.health.shop.submissionhandler.application.items.subitems.ApplicantItem;
import uk.gov.dwp.health.shop.submissionhandler.application.items.subitems.MandatoryMsgItems;
import uk.gov.dwp.health.shop.submissionhandler.application.utils.ValidationLogger;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EsaSubmissionItem extends MandatoryMsgItems implements Payload {
  private static final Logger LOG = LoggerFactory.getLogger(EsaSubmissionItem.class.getName());
  private static final int OFFSET_DAYS = 2;

  @JsonProperty("ref")
  private String ref;

  @JsonProperty("date_submitted")
  private String dateSubmitted;

  @JsonProperty("applicant")
  private ApplicantItem applicantItem;

  @JsonProperty("data_capture")
  private DataCapture dataCapture;

  @JsonProperty("declaration")
  private String declaration;

  @JsonProperty("tags")
  private List<Object> tags;

  public String getDateSubmitted() {
    return dateSubmitted;
  }

  public ApplicantItem getApplicantItem() {
    return applicantItem;
  }

  public DataCapture getDataCapture() {
    return dataCapture;
  }

  public String getDeclaration() {
    return declaration;
  }

  public List<Object> getTags() {
    return tags;
  }

  public String getRef() {
    return ref;
  }

  @JsonIgnore
  public boolean isContentValid() {
    boolean isValid = !(null == getRef() || getRef().isEmpty());
    ValidationLogger.logOutput(LOG, "ref", isValid);

    if (isValid && null != getDateSubmitted()) {
      isValid = isSubmittedDateValid();
      ValidationLogger.logOutput(LOG, "date_submitted", isValid);
    }

    if (isValid && null != getApplicantItem()) {
      isValid = getApplicantItem().isContentValid();
      ValidationLogger.logOutput(LOG, "applicant", isValid);
    }

    if (isValid && null != getDataCapture()) {
      isValid = getDataCapture().isContentValid();
      ValidationLogger.logOutput(LOG, "data_capture", isValid);
    }

    return isValid;
  }

  @JsonIgnore
  private boolean isSubmittedDateValid() {
    boolean dateValid;

    try {
      Instant dtSubmitted = getDateSubmittedInstant();
      Duration offsetDays = Duration.ofDays(OFFSET_DAYS);

      dateValid = dtSubmitted != null && dtSubmitted.isBefore(Instant.now().plus(offsetDays));

      if (!dateValid) {
        LOG.error("{} is too far in the future, it can be only 2 days in future", dtSubmitted);
      }

    } catch (DateTimeParseException e) {
      LOG.debug("invalid date :: {}", e.getMessage());
      dateValid = false;
    }

    return dateValid;
  }

  @JsonIgnore
  public Instant getDateSubmittedInstant() {
    DateTimeFormatter dtFormat = DateTimeFormatter.ISO_INSTANT;
    return Instant.from(dtFormat.parse(getDateSubmitted()));
  }
}
