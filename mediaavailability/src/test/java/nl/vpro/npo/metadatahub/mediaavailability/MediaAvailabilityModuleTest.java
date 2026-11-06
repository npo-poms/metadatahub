package nl.vpro.npo.metadatahub.mediaavailability;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.vpro.npo.metadatahub.mediaavailability.domain.MediaAvailabilityInfo;
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
    void unknown() throws Exception {
        assertNull(read("\"MARS\""));
    }

    /**
     * Platforms not in the spec (like 'hergebruik') are mapped to null, and must not make the complete message fail.
     */
    @Test
    void unknownPlatform() throws Exception {
        MediaAvailabilityInfo info = mapper.readValue("""
            {
              "prid": "POW_03384486",
              "platform": "hergebruik",
              "availabilityPeriods": [ { "geoIpRestriction": "NL" } ]
            }
            """, MediaAvailabilityInfo.class);
        assertNull(info.getPlatform());
        assertEquals("POW_03384486", info.getPrid());
        assertEquals(NL, info.getAvailabilityPeriods().getFirst().getGeoIpRestriction());
    }

    @Test
    void knownPlatform() throws Exception {
        MediaAvailabilityInfo info = mapper.readValue("{\"prid\": \"POW_1\", \"platform\": \"npo-od\"}", MediaAvailabilityInfo.class);
        assertEquals(MediaAvailabilityInfo.PlatformEnum.NPO_OD, info.getPlatform());
    }

    @Test
    void serviceLoader() throws Exception {
        ObjectMapper m = ApiClient.createDefaultObjectMapper().findAndRegisterModules();
        assertEquals(WR, m.readValue("{\"geoIpRestriction\": \"WERELD\"}", MediaAvailabilityPeriod.class).getGeoIpRestriction());
        assertNull(m.readValue("{\"prid\": \"POW_1\", \"platform\": \"hergebruik\"}", MediaAvailabilityInfo.class).getPlatform());
    }
}
