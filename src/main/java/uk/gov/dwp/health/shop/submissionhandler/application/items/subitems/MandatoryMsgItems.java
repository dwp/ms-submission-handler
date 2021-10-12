package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MandatoryMsgItems {
  public static final String MSG_ID = "msg_id";

  @JsonProperty(MSG_ID)
  private String msgId;

  public String getMsgId() {
    return msgId;
  }
}
