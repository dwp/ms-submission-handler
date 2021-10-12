package uk.gov.dwp.health.shop.submissionhandler.application.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces.Payload;
import org.slf4j.Logger;
import uk.gov.dwp.health.shop.submissionhandler.application.items.subitems.ApplicantItem;
import uk.gov.dwp.health.shop.submissionhandler.application.items.subitems.MandatoryMsgItems;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AtwSubmissionItem extends MandatoryMsgItems implements Payload {
  private static final Logger LOG = LoggerFactory.getLogger(AtwSubmissionItem.class.getName());
  private static final int OFFSET_FOR_WORKING_DAYS = 5;

  @JsonProperty("ref")
  private String ref;

  @JsonProperty("date_submitted")
  private String dateSubmitted;

  @JsonProperty("applicant")
  private ApplicantItem applicantItem;

  @JsonProperty("data_capture")
  private Object dataCapture;

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

  public Object getDataCapture() {
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
    logOutput("ref", isValid);

    if (isValid && null != getDateSubmitted()) {
      isValid = isSubmittedDateValid();
      logOutput("date_submitted", isValid);
    }

    if (isValid && null != getApplicantItem()) {
      isValid = getApplicantItem().isContentValid();
      logOutput("applicant", isValid);
    }

    return isValid;
  }

  @JsonIgnore
  private boolean isSubmittedDateValid() {
    boolean dateValid;

    try {
      Instant dtSubmitted = getDateSubmittedInstant();
      Duration offsetForWorkingDays = Duration.ofDays(OFFSET_FOR_WORKING_DAYS);

      dateValid =
          dtSubmitted != null && dtSubmitted.isBefore(Instant.now().plus(offsetForWorkingDays));

      if (dtSubmitted != null
          && dtSubmitted.isAfter(Instant.now().plus(offsetForWorkingDays))) {
        LOG.debug("{} is too far in the future for next working day", dtSubmitted);
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

  @JsonIgnore
  private void logOutput(String item, boolean state) {
    LOG.debug("checking item '{}' with validated status {}", item, state);
  }
}
