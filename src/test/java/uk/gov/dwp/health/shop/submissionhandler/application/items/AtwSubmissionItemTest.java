package uk.gov.dwp.health.shop.submissionhandler.application.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.health.shop.submissionhandler.application.items.subitems.ApplicantItem;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("squid:S1192") // allow string literals
public class AtwSubmissionItemTest {
    private static final String VALID_APPLICANT = "{\"forenames\":\"aa\",\"surname\":\"bb\",\"dob\":\"2000-02-29\",\"residence_address\":{\"lines\":[\"line 1\",\"line 2\"],\"premises\":\"at home\",\"postcode\":\"ls6 4pt\"},\"contact_options\":[{\"method\":\"email\",\"data\":\"an.email@address.co.uk\",\"preferred\":true}],\"representative\":{\"full_name\":\"john andrew smith\",\"relationship\":\"father\",\"email\":\"email@server.com\",\"tel\":\"1234\"}}";
    private PojoDataCapture validDataCapture;
    private ApplicantItem validApplicatItem;
    private List<Object> validTags;

    private class PojoDataCapture {
        private String someTextItem;
        private int someIntValue;

        public PojoDataCapture(String text, int testValue) {
            this.someIntValue = testValue;
            this.someTextItem = text;
        }

        public String getSomeTextItem() {
            return someTextItem;
        }

        public int getSomeIntValue() {
            return someIntValue;
        }
    }

    @Before
    public void init() throws IOException {
        validApplicatItem = new ObjectMapper().readValue(VALID_APPLICANT, ApplicantItem.class);
        validTags = Collections.singletonList(new LinkedHashMap<>().put("key1", "value1"));
        validDataCapture = new PojoDataCapture("this is text", 99);
    }

    @Test
    public void invalidWithNullData() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue("{}", AtwSubmissionItem.class);
        assertThat("empty json should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithNullRef() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson(null, "2017-02-19T12:46:31Z", validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("null 'ref' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithEmptyRef() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("", "2017-02-19T12:46:31Z", validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("empty 'ref' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullDateSubmitted() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("asdf123", null, validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("null 'DateSubmitted' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyDateSubmitted() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("we234", "", validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("empty 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithInvalidDateSubmitted() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("1234wer", "2017-02-30T12:46:31Z", validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("invalid 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithFutureDateSubmittedMoreThan5DaysForward() throws IOException {
        String formattedDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now().plusSeconds(30).plus(Duration.ofDays(5)));

        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("56erty", formattedDate, validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("future 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithFutureDateSubmittedLessThan5DaysForward() throws IOException {
        String formattedDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now().minusSeconds(30).plus(Duration.ofDays(5)));

        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("56erty", formattedDate, validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("future 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullApplicant() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("567rer", "2017-02-19T12:46:31Z", null, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("null 'Applicant' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyApplicant() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("dfgh456", "2017-02-19T12:46:31Z", new ApplicantItem(), validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("empty 'Applicant' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullDataCapture() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("sdfgh45", "2017-02-19T12:46:31Z", validApplicatItem, null, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("null 'DataCapture' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullDeclaration() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("1234qwer", "2017-02-19T12:46:31Z", validApplicatItem, validDataCapture, null, validTags), AtwSubmissionItem.class);
        assertThat("null 'Declaration' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithEmptyDeclaration() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("ert456", "2017-02-19T12:46:31Z", validApplicatItem, validDataCapture, "", validTags), AtwSubmissionItem.class);
        assertThat("empty 'Declaration' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullTags() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("123qwer", "2017-02-19T12:46:31Z", validApplicatItem, validDataCapture, "I declare ok", null), AtwSubmissionItem.class);
        assertThat("null 'tags' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validSubmissionItems() throws IOException {
        AtwSubmissionItem instance = new ObjectMapper().readValue(formatJson("123qwe", "2017-02-19T12:46:31Z", validApplicatItem, validDataCapture, "I declare ok", validTags), AtwSubmissionItem.class);
        assertThat("submitted items should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    private String formatJson(String ref, String datesubmitted, ApplicantItem applicantItem, Object dataCapture, String declaration, List<Object> tags) throws JsonProcessingException {
        return String.format("{\"ref\": %s, \"date_submitted\": %s, \"applicant\" : %s, \"data_capture\" : %s, \"declaration\" : %s, \"tags\" : %s}",
                ref != null ? String.format("\"%s\"", ref) : null,
                datesubmitted != null ? String.format("\"%s\"", datesubmitted) : null,
                applicantItem != null ? new ObjectMapper().writeValueAsString(applicantItem) : null,
                dataCapture != null ? new ObjectMapper().writeValueAsString(dataCapture) : null,
                declaration != null ? String.format("\"%s\"", declaration) : null,
                tags != null ? new ObjectMapper().writeValueAsString(tags) : null);
    }
}