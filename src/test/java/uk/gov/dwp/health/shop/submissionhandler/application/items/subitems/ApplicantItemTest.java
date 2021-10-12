package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@SuppressWarnings("squid:S1192") // allow string literals
public class ApplicantItemTest {
    private static final String VALID_REPRESENTATIVE_CONTENTS = "{\"full_name\": \"john andrew smith\", \"relationship\" : \"father\", \"email\" : \"email@server.com\", \"tel\" : \"1234\"}";
    private static final String VALID_RESIDENCE_ADDRESS = "{\"lines\":[\"line 1\", \"line 2\"], \"premises\": \"at home\", \"postcode\": \"ls6 4pt\"}";
    private static final String VALID_CONTACT_DETAILS = "{\"method\": \"email\", \"data\" : \"an.email@address.co.uk\", \"preferred\" : true}";
    private List<ContactOptionItem> validContactOptionList;
    private RepresentativeItem validRepresentativeItem;
    private ResidenceAddressItem validAddressItem;
    private static final Logger LOG = LoggerFactory.getLogger(ApplicantItemTest.class.getName());

    @Before
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        validContactOptionList = Collections.singletonList(mapper.readValue(VALID_CONTACT_DETAILS, ContactOptionItem.class));
        validRepresentativeItem = mapper.readValue(VALID_REPRESENTATIVE_CONTENTS, RepresentativeItem.class);
        validAddressItem = mapper.readValue(VALID_RESIDENCE_ADDRESS, ResidenceAddressItem.class);
    }

    @Test
    public void invalidWithNullData() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue("{}", ApplicantItem.class);
        assertThat("empty json should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithNullForenames() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson(null, "jones", "2000-02-29", validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("null 'forenames' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithEmptyForenames() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("", "jones", "2000-02-29", validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("empty 'forenames' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullSurname() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", null, "2000-02-29", validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("null 'surname' should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithEmptySurname() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "", "2000-02-29", validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("empty 'surname' should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullDob() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", null, validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("null 'Dob' should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyDob() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "", validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("empty 'Dob' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithInvalidDob() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2017-02-30", validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("bad 'Dob' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullResidence() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", null, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("null 'Residence' should be valid", instance.isContentValid(), is(equalTo(true)));
        assertFalse("empty 'Residence' should be missing", new ObjectMapper().writeValueAsString(instance).contains("residence"));
    }

    @Test
    public void validWithEmptyResidence() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", new ResidenceAddressItem(), validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("empty 'Residence' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullContactAndNullRepresentative() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", validAddressItem, null, null), ApplicantItem.class);
        assertThat("null 'Contact' and null 'Rep' should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyContact() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", validAddressItem, Collections.singletonList(new ContactOptionItem()), validRepresentativeItem), ApplicantItem.class);
        assertThat("empty 'Contact' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNullRepresentativeAndContact() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", validAddressItem, validContactOptionList, null), ApplicantItem.class);
        assertThat("null 'Representative' should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullContactAndRepresentative() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", validAddressItem, null, validRepresentativeItem), ApplicantItem.class);
        assertThat("null 'Representative' should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithEmptyRepresentativeAndNullContact() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", validAddressItem, null, new RepresentativeItem()), ApplicantItem.class);
        assertThat("empty 'Representative' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithCorrectItems() throws IOException {
        ApplicantItem instance = new ObjectMapper().readValue(formatJson("chris", "jones", "2000-02-29", validAddressItem, validContactOptionList, validRepresentativeItem), ApplicantItem.class);
        assertThat("All items present should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    private String formatJson(String forename, String surname, String dob, ResidenceAddressItem addressItem, List<ContactOptionItem> contactOptionItem, RepresentativeItem representativeItem) throws JsonProcessingException {
        String output = String.format("{\"forenames\": %s, \"surname\" : %s, \"dob\" : %s, \"residence_address\" : %s, \"contact_options\" : %s, \"representative\" : %s}",
                forename != null ? String.format("\"%s\"", forename) : null,
                surname != null ? String.format("\"%s\"", surname) : null,
                  dob != null ? String.format("\"%s\"", dob) : null,
                addressItem != null ? new ObjectMapper().writeValueAsString(addressItem) : null,
                contactOptionItem != null ? new ObjectMapper().writeValueAsString(contactOptionItem) : null,
                representativeItem != null ? new ObjectMapper().writeValueAsString(representativeItem) : null);

        LOG.info(output);
        return output;
    }
}
