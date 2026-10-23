package nl.vpro.npo.metadatahub.mediaavailability;

import java.time.Instant;

public record MediaAvailability(
    String prid,
    String platform,
    Instant created,
    Instant lastUpdated, //  // ISO 8601 datetime string
    boolean isStreamable,
    boolean isDownloadable,
    int ageRestriction,
    String[] broadcasters,
    AvailabilityPeriod[]  availabilityPeriods,
    String source
) {
}
