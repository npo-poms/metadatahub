package nl.vpro.npo.metadatahub.mediaavailability;


import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.api.MediaAvailabilityApi;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Log4j2
public class ClientTest {

    static final Path CONFIG = Path.of(System.getProperty("user.home"), "conf", "metadatahub.properties");

    static final Properties properties = new Properties();

    @BeforeAll
    static void readConfig() throws IOException {
        assumeTrue(Files.isReadable(CONFIG), CONFIG + " not found");
        try (Reader reader = Files.newBufferedReader(CONFIG)) {
            properties.load(reader);
        }
    }

    static String property(String key) {
        String value = properties.getProperty(key);
        assumeTrue(value != null && !value.isBlank(), key + " not set in " + CONFIG);
        return value.trim();
    }

    ApiClient client() {
        ApiClient client = new ApiClient();
        // only scheme and host; the generated api adds the /v1/... paths itself
        client.updateBaseUri(URI.create(property("availability_endpoint_baseUrl")).resolve("/").toString().replaceAll("/$", ""));
        client.setObjectMapper(MediaAvailabilityModule.createObjectMapper());

        client.setRequestInterceptor(new HttpCredentials(
            URI.create(property("availability_sso").replaceAll("/$", "") + "/oauth2/token"),
            property("availability_username"),
            property("availability_password")
        ));
        return client;
    }
    MediaAvailabilityApi api = new MediaAvailabilityApi(client());


    @Test
    void getMediaAvailability() throws ApiException {

        log.info(api.getMediaAvailability("NPO_FVOD", "POW_05986176"));
    }


    @Test
    void listMediaAvailabilityProductRestrictions() throws ApiException {

        log.info(api.listMediaAvailabilityProductRestrictions("POW_05986176"));
    }

}
