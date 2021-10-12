package uk.gov.dwp.health.shop.submissionhandler.application.items.interfaces;

import java.time.Instant;

public interface Payload {
  boolean isContentValid();

  Instant getDateSubmittedInstant();

  String getRef();
}
