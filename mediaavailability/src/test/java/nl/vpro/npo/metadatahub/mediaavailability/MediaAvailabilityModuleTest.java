package nl.vpro.npo.metadatahub.mediaavailability;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import nl.vpro.npo.metadatahub.mediaavailability.domain.MediaAvailabilityPeriod;

import static nl.vpro.npo.metadatahub.mediaavailability.domain.MediaAvailabilityPeriod.GeoIpRestrictionEnum.*;
import static org.junit.jupiter.api.Assertions.*;

class MediaAvailabilityModuleTest {

    final ObjectMapper mapper = MediaAvailabilityModule.createObjectMapper();

    MediaAvailabilityPeriod.GeoIpRestrictionEnum read(String geo) throws Exception {
        return mapper.readValue("{\"geoIpRestriction\": " + geo + "}", MediaAvailabilityPeriod.class).getGeoIpRestriction();
    }

    @Test
    void regular() throws Exception {
        assertEquals(NL, read("\"NL\""));
        assertEquals(WR, read("\"WR\""));
    }

    @Test
    void wereld() throws Exception {
        assertEquals(WR, read("\"WERELD\""));
        assertEquals(WR, read("\"wereld\""));
    }

    @Test
    void nullOrEmpty() throws Exception {
        assertNull(read("null"));
        assertNull(read("\"\""));
    }

    @Test
    void unknown() {
        assertThrows(InvalidFormatException.class, () -> read("\"MARS\""));
    }

    @Test
    void serviceLoader() throws Exception {
        ObjectMapper m = ApiClient.createDefaultObjectMapper().findAndRegisterModules();
        assertEquals(WR, m.readValue("{\"geoIpRestriction\": \"WERELD\"}", MediaAvailabilityPeriod.class).getGeoIpRestriction());
    }
}
