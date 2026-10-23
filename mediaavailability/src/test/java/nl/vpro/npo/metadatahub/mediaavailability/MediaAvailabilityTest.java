package nl.vpro.npo.metadatahub.mediaavailability;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.npo.metadatahub.mediaavailability.domain.MediaAvailabilityInfo;

@Log4j2
class MediaAvailabilityTest {


    @Test
    void testMediaAvailability() throws JsonProcessingException {
        String example = """
            {
            "prid": "NPO_1",
            "platform": "npo-fvod",
            "created": "2026-10-23T10:00:00Z",
            "lastUpdated": "2026-10-23T10:00:00Z",
            "isStreamable": true,
            "isDownloadable": false,
            "ageRestriction": 6,
            "broadcasters": ["VPRO", "EO"],
            "availabilityPeriods": [{
             "availabilityPeriodId": "1"
            }

            ],
            "source": "test"
           }""";

        Jackson2Mapper wrapper = Jackson2Mapper.getStrictInstance();

        MediaAvailabilityInfo av = wrapper.readerFor(MediaAvailabilityInfo.class).readValue(example);

        log.info(av.toString());
    }

}
