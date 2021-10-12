package uk.gov.dwp.health.shop.submissionhandler.application.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dwp.health.esao.shared.models.DataCapture;
import uk.gov.dwp.health.shop.submissionhandler.application.items.subitems.ApplicantItem;

import java.io.File;
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
public class EsaSubmissionItemTest {
    private static final String VALID_APPLICANT = "{\"forenames\":\"aa\",\"surname\":\"bb\",\"dob\":\"2000-02-29\",\"residence_address\":{\"lines\":[\"line 1\",\"line 2\"],\"premises\":\"at home\",\"postcode\":\"ls6 4pt\"},\"contact_options\":[{\"method\":\"email\",\"data\":\"an.email@address.co.uk\",\"preferred\":true}],\"representative\":{\"full_name\":\"john andrew smith\",\"relationship\":\"father\",\"email\":\"email@server.com\",\"tel\":\"1234\"}}";
    private static final ObjectMapper mapper = new ObjectMapper();
    private ApplicantItem validApplicatItem;
    private DataCapture dataCapture;
    private List<Object> validTags;

    @Before
    public void init() throws IOException {
        dataCapture = mapper.readValue(new File("src/test/resources/esa-datacapture-record.json"), DataCapture.class);
        validApplicatItem = mapper.readValue(VALID_APPLICANT, ApplicantItem.class);
        validTags = Collections.singletonList(new LinkedHashMap<>().put("key1", "value1"));
    }

    @Test
    public void invalidWithNullData() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue("{}", EsaSubmissionItem.class);
        assertThat("empty json should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithNullRef() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson(null, "2017-02-19T12:46:31Z", validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("null 'ref' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithEmptyRef() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("", "2017-02-19T12:46:31Z", validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("empty 'ref' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullDateSubmitted() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("asdf123", null, validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("null 'DateSubmitted' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyDateSubmitted() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("we234", "", validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("empty 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithInvalidDateSubmitted() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("1234wer", "2017-02-30T12:46:31Z", validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("invalid 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithFutureDateSubmittedMoreThan5DaysForward() throws IOException {
        String formattedDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now().plusSeconds(30).plus(Duration.ofDays(5)));

        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("56erty", formattedDate, validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("future 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithFutureDateSubmittedWith2DaysForward() throws IOException {
        String formattedDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now().plusSeconds(30).plus(Duration.ofDays(2)));

        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("56erty", formattedDate, validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("future 'DateSubmitted' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithFutureDateSubmittedLessThan2DaysForward() throws IOException {
        String formattedDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now().plusSeconds(30).plus(Duration.ofDays(1)));

        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("56erty", formattedDate, validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("future 'DateSubmitted' with in 2 days should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullApplicant() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("567rer", "2017-02-19T12:46:31Z", null, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("null 'Applicant' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyApplicant() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("dfgh456", "2017-02-19T12:46:31Z", new ApplicantItem(), dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("empty 'Applicant' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullDataCapture() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("sdfgh45", "2017-02-19T12:46:31Z", validApplicatItem, null, "I declare ok", validTags), EsaSubmissionItem.class);
        assertThat("null 'DataCapture' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithKnownFailureData() throws JsonProcessingException {
        dataCapture.getEmployments().get(0).setFrequency("");

        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("123qwer", "2017-02-19T12:46:31Z", validApplicatItem, dataCapture, "I declare ok", null), EsaSubmissionItem.class);
        assertThat("expecting bad DataCapture validation", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullDeclaration() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("1234qwer", "2017-02-19T12:46:31Z", validApplicatItem, dataCapture, null, validTags), EsaSubmissionItem.class);
        assertThat("null 'Declaration' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithEmptyDeclaration() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("ert456", "2017-02-19T12:46:31Z", validApplicatItem, dataCapture, "", validTags), EsaSubmissionItem.class);
        assertThat("empty 'Declaration' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullTags() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("123qwer", "2017-02-19T12:46:31Z", validApplicatItem, dataCapture, "I declare ok", null), EsaSubmissionItem.class);
        assertThat("null 'tags' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validSubmissionItems() throws IOException {
        EsaSubmissionItem instance = new ObjectMapper().readValue(formatJson("123qwe", "2017-02-19T12:46:31Z", validApplicatItem, dataCapture, "I declare ok", validTags), EsaSubmissionItem.class);
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
