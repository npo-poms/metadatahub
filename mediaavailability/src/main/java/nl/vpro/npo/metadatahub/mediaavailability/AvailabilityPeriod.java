package nl.vpro.npo.metadatahub.mediaavailability;

import java.time.Instant;

public record AvailabilityPeriod(

    String availabilityPeriodId,
    String geoIpRestriction,
    boolean drm,
    boolean downloadAllowed,
    Instant start,
    Instant stop,
    boolean revoked,
    Instant restrictedTime
) {
}
