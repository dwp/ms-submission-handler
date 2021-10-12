package uk.gov.dwp.health.shop.submissionhandler.application.utils;

import org.slf4j.Logger;

public class ValidationLogger {

  public static void logOutput(Logger log, String item, boolean state) {
    if (state) {
      log.debug("checked item '{}' with validated status {}", item, state);
    } else {
      log.warn("checked item '{}' with validated status {}", item, state);
    }
  }
}
