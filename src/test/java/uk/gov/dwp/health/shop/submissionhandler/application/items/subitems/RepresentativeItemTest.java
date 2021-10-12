package uk.gov.dwp.health.shop.submissionhandler.application.items.subitems;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@SuppressWarnings({"squid:S1192"}) // allow string literals
public class RepresentativeItemTest {
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(RepresentativeItemTest.class.getName());

    @Test
    public void emptyWithNullDataOk() throws IOException {
        String serialisationOutput = "{}";

        RepresentativeItem instance = mapper.readValue("{}", RepresentativeItem.class);
        assertThat("should be empty", mapper.writeValueAsString(instance), is(equalTo(serialisationOutput)));
    }

    @Test
    public void validWithGoodMethod() throws IOException {
        String serialisationOutput = formatJson("john smith", "father", "an.email@server.com", "1234");

        RepresentativeItem instance = mapper.readValue(serialisationOutput, RepresentativeItem.class);
        assertThat("populated object should be valid", mapper.writeValueAsString(instance), is(equalTo(serialisationOutput)));
    }

    @Test
    public void missingWithNullFullnameMethod() throws IOException {
        RepresentativeItem instance = new ObjectMapper().readValue(formatJson(null, "father", "an.email@server.com", "1234"), RepresentativeItem.class);
        assertFalse("null 'full_name' should be missing from serialisation", mapper.writeValueAsString(instance).contains("full_name"));
    }

    @Test
    public void emptyWithEmptyFullnameMethod() throws IOException {
        RepresentativeItem instance = new ObjectMapper().readValue(formatJson("", "father", "an.email@server.com", "1234"), RepresentativeItem.class);
        assertThat("empty 'full_name' should still be empty", instance.getFullName(), is(equalTo("")));
    }

    @Test
    public void missingWithNullRelationshipMethod() throws IOException {
        RepresentativeItem instance = new ObjectMapper().readValue(formatJson("john smith", null, "an.email@server.com", "1234"), RepresentativeItem.class);
        assertFalse("null 'relationship' should be missing from serialisation", mapper.writeValueAsString(instance).contains("relationship"));
    }

    @Test
    public void emptyWithEmptyRelationshipMethod() throws IOException {
        RepresentativeItem instance = new ObjectMapper().readValue(formatJson("john smith", "", "an.email@server.com", "1234"), RepresentativeItem.class);
        assertThat("empty 'relationship' should still be empty", instance.getRelationship(), is(equalTo("")));
    }

    @Test
    public void missingWithNullTelephoneMethod() throws IOException {
        RepresentativeItem instance = new ObjectMapper().readValue(formatJson("john smith", "father", "an.email@server.com", null), RepresentativeItem.class);
        assertFalse("null 'tel' should be missing", mapper.writeValueAsString(instance).contains("tel"));
    }

    @Test
    public void returnedWithEmptyTelephoneMethod() throws IOException {
        RepresentativeItem instance = new ObjectMapper().readValue(formatJson("john smith", "father", "an.email@server.com", ""), RepresentativeItem.class);
        assertThat("empty 'tel' should still be empty", instance.getTelephoneNumber(), is(equalTo("")));
    }

    private String formatJson(String fullname, String relationship, String email, String tel) {
        String output = String.format("{\"full_name\":%s,\"relationship\":%s,\"email\":%s,\"tel\":%s}",
                fullname != null ? String.format("\"%s\"", fullname) : null,
                relationship != null ? String.format("\"%s\"", relationship) : null,
                email != null ? String.format("\"%s\"", email) : null,
                tel != null ? String.format("\"%s\"", tel) : null);

        LOG.info(output);
        return output;
    }
}
