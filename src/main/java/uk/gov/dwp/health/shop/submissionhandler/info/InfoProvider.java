package uk.gov.dwp.health.shop.submissionhandler.info;

import java.util.Properties;

@FunctionalInterface
public interface InfoProvider {

  Properties getInfo();
}
