package uk.gov.dwp.health.shop.submissionhandler.application.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("squid:S1192") // allow string literals
public class ContactMethodEnumTest {
    private static final Logger LOG = LoggerFactory.getLogger(ContactMethodEnumTest.class.getName());

    @Test
    public void invalidEnumItem() {
        try {
            ContactMethodEnum.valueOf("invalid");
            fail("should be 'invalid'");

        } catch (IllegalArgumentException e) {
            assertThat("should throw invalid enum", e.getMessage(), startsWith("No enum constant"));
        }
    }

    @Test
    public void validEnumItemEMAIL() {
        LOG.info("Enum value {}", ContactMethodEnum.valueOf("email"));
    }

    @Test
    public void validEnumItemTEL() {
        LOG.info("Enum value {}", ContactMethodEnum.valueOf("tel"));
    }

    @Test
    public void validenumitemMobile() {
        LOG.info("Enum value {}", ContactMethodEnum.valueOf("telmobile"));
    }

    @Test
    public void validenumitemPostal() {
        LOG.info("Enum value {}", ContactMethodEnum.valueOf("postal"));
    }

    @Test
    public void validenumitemPostalUppercase() {
        try {
            ContactMethodEnum.valueOf("POSTAL");
            fail("should be 'invalid'");

        } catch (IllegalArgumentException e) {
            assertThat("should throw invalid enum", e.getMessage(), startsWith("No enum constant"));
        }
    }
}