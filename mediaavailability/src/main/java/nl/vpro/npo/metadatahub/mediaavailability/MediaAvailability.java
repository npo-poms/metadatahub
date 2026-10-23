package nl.vpro.npo.metadatahub.mediaavailability;

import java.time.Instant;
import java.util.List;

public record MediaAvailability(
    String prid,
    String platform,
    Instant created,
    Instant lastUpdated, //  // ISO 8601 datetime string
    boolean isStreamable,
    boolean isDownloadable,
    int ageRestriction,
    List<String> broadcasters,
    List<AvailabilityPeriod>  availabilityPeriods,
    String source
) {
}
