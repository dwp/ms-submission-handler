package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepresentativeItem {

  @JsonProperty("full_name")
  private String fullName;

  @JsonProperty("relationship")
  private String relationship;

  @JsonProperty("email")
  private String email;

  @JsonProperty("tel")
  private String telephoneNumber;

  public String getFullName() {
    return fullName;
  }

  public String getRelationship() {
    return relationship;
  }

  public String getEmail() {
    return email;
  }

  public String getTelephoneNumber() {
    return telephoneNumber;
  }
}
