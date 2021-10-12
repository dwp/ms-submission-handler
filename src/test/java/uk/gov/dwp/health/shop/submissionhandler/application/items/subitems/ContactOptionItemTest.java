package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("squid:S1192") // allow string literals
public class ContactOptionItemTest {

    @Test
    public void invalidWithNullData() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue("{}", ContactOptionItem.class);
        assertThat("empty json should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithEmptyContactMethod() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue(formatJson(null, "an.email@address.com", true), ContactOptionItem.class);
        assertThat("empty 'method' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithInvalidContactMethod() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue(formatJson("bad", "an.email@address.com", true), ContactOptionItem.class);
        assertThat("bad 'method' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithGoodContactMethod() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue(formatJson("email", "an.email@address.com", true), ContactOptionItem.class);
        assertThat("email 'method' should be valid", instance.isContentValid(), is(equalTo(true)));
    }

    @Test
    public void invalidWithNullContents() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue(formatJson("email", null, true), ContactOptionItem.class);
        assertThat("null 'data' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void invalidWithEmptyContents() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue(formatJson("email", "", true), ContactOptionItem.class);
        assertThat("empty 'data' should be invalid", instance.isContentValid(), is(equalTo(false)));
    }

    @Test
    public void validWithNoPreference() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue("{\"method\": \"email\", \"data\" : \"an.email@address.co.uk\"}", ContactOptionItem.class);
        assertThat("empty preferred should be valid", instance.isContentValid(), is(equalTo(true)));
        assertThat("missing preferred is false", instance.isPreferred(), is(equalTo(false)));
    }

    @Test
    public void validWithFalsePreference() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue(formatJson("email", "an.email@address.com", false), ContactOptionItem.class);
        assertThat("false preferred should be valid", instance.isContentValid(), is(equalTo(true)));
        assertThat("'false' preferred is false", instance.isPreferred(), is(equalTo(false)));
    }

    @Test
    public void validWithTruePreference() throws IOException {
        ContactOptionItem instance = new ObjectMapper().readValue(formatJson("email", "an.email@address.com", true), ContactOptionItem.class);
        assertThat("false preferred should  be false", instance.isContentValid(), is(equalTo(true)));
        assertThat("'true' preferred is true", instance.isPreferred(), is(equalTo(true)));
    }

    private String formatJson(String method, String data, Boolean preferred) {
        return String.format("{\"method\": %s, \"data\" : %s, \"preferred\" : %s}",
                method != null ? String.format("\"%s\"", method) : null,
                data != null ? String.format("\"%s\"", data) : null,
                preferred != null ? String.format("\"%s\"", preferred) : null);
    }
}
