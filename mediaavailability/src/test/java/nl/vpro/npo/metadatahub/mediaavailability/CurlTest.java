package nl.vpro.npo.metadatahub.mediaavailability;

import java.net.URI;
import java.net.http.HttpRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurlTest {

    final HttpRequest request = HttpRequest.newBuilder(URI.create("https://availability.metadata.bijnpo.nl/v1/product/POW_05986176?a=b"))
        .header("Accept", "application/json")
        .header("Authorization", "Bearer secret")
        .GET()
        .build();

    @Test
    void masked() {
        assertEquals("curl -H 'Accept: application/json' -H 'Authorization: Bearer ***' 'https://availability.metadata.bijnpo.nl/v1/product/POW_05986176?a=b'", Curl.of(request));
    }

    @Test
    void showAuthorization() {
        assertEquals("curl -H 'Accept: application/json' -H 'Authorization: Bearer secret' 'https://availability.metadata.bijnpo.nl/v1/product/POW_05986176?a=b'", Curl.of(request, true));
    }

    @Test
    void quote() {
        assertEquals("'it'\\''s'", Curl.quote("it's"));
    }
}
