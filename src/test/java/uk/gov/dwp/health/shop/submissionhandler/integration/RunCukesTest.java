package uk.gov.dwp.health.shop.submissionhandler.integration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import uk.gov.dwp.health.shop.submissionhandler.application.SubmissionHandlerApplication;
import uk.gov.dwp.health.shop.submissionhandler.application.SubmissionHandlerConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@RunWith(Cucumber.class)
@SuppressWarnings({"squid:S2187", "squid:S1118"}) // no tests needed to kick of cucumber
@CucumberOptions(plugin = "json:target/cucumber-report.json")
public class RunCukesTest {

  private static final String CONFIG_FILE = "test.yml";

  @ClassRule
  public static final DropwizardAppRule<SubmissionHandlerConfiguration> RULE =
      new DropwizardAppRule<>(SubmissionHandlerApplication.class, resourceFilePath(CONFIG_FILE));
}
