package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings({"squid:S1192"}) // allow string literals
public class ResidenceAddressItemTest {

    @Test
    public void invalidWithNullData() throws IOException {
        ResidenceAddressItem instance = new ObjectMapper().readValue("{}", ResidenceAddressItem.class);
        assertThat("empty json should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithGoodContents() throws IOException {
        ResidenceAddressItem instance = new ObjectMapper().readValue(formatJson(new String[]{"line1", "line2"}, "at home", "ls6 4pt"), ResidenceAddressItem.class);
        assertThat("populated object should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithNullLines() throws IOException {
        ResidenceAddressItem instance = new ObjectMapper().readValue(formatJson(null, "at home", "ls6 4pt"), ResidenceAddressItem.class);
        assertThat("null lines should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyLines() throws IOException {
        ResidenceAddressItem instance = new ObjectMapper().readValue(formatJson(new String[]{}, "at home", "ls6 4pt"), ResidenceAddressItem.class);
        assertThat("empty lines should be invalid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void validWithNullPostcode() throws IOException {
        ResidenceAddressItem instance = new ObjectMapper().readValue(formatJson(new String[]{"line1", "line2"}, "at home", null), ResidenceAddressItem.class);
        assertThat("null postcode should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithEmptyPostcode() throws IOException {
        ResidenceAddressItem instance = new ObjectMapper().readValue(formatJson(new String[]{"line1", "line2"}, "at home", ""), ResidenceAddressItem.class);
        assertThat("empty postcode should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithBadPostcode() throws IOException {
        ResidenceAddressItem instance = new ObjectMapper().readValue(formatJson(new String[]{"line1", "line2"}, "at home", "ls6"), ResidenceAddressItem.class);
        assertThat("bad postcode should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    private String formatJson(String[] lines, String premises, String postcode) throws JsonProcessingException {
        return String.format("{\"lines\":%s, \"premises\": %s, \"postcode\": %s}",
                lines != null ? new ObjectMapper().writeValueAsString(lines) : null,
                premises != null ? String.format("\"%s\"", premises) : null,
                postcode != null ? String.format("\"%s\"", postcode) : null);
    }
}
