package nl.vpro.npo.metadatahub.mediaavailability;

import java.io.IOException;
import java.io.Serial;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import nl.vpro.npo.metadatahub.mediaavailability.domain.MediaAvailabilityPeriod.GeoIpRestrictionEnum;

/**
 * Jackson module to work around deviations in the actual messages from the openapi spec.
 * <p>
 * E.g. {@code geoIpRestriction} is sometimes sent as {@code WERELD}, where {@code WR} is meant.
 * <p>
 * Registered via {@code META-INF/services}, so {@link ObjectMapper#findAndRegisterModules()} picks it up too.
 */
public class MediaAvailabilityModule extends SimpleModule {

    static final Map<String, GeoIpRestrictionEnum> GEO_IP_RESTRICTION_ALIASES = Map.of(
        "WERELD", GeoIpRestrictionEnum.WR
    );
    @Serial
    private static final long serialVersionUID = 4496088846591320188L;

    public MediaAvailabilityModule() {
        super(MediaAvailabilityModule.class.getSimpleName());
        addDeserializer(GeoIpRestrictionEnum.class, new GeoIpRestrictionDeserializer());
    }

    /**
     * The default object mapper of the generated {@link ApiClient}, with this module registered.
     */
    public static ObjectMapper createObjectMapper() {
        return ApiClient.createDefaultObjectMapper().registerModule(new MediaAvailabilityModule());
    }

    static class GeoIpRestrictionDeserializer extends StdScalarDeserializer<GeoIpRestrictionEnum> {

        @Serial
        private static final long serialVersionUID = 2887263710160458438L;

        GeoIpRestrictionDeserializer() {
            super(GeoIpRestrictionEnum.class);
        }

        @Override
        public GeoIpRestrictionEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.isBlank()) {
                return null;
            }
            value = value.trim();
            GeoIpRestrictionEnum alias = GEO_IP_RESTRICTION_ALIASES.get(value.toUpperCase());
            if (alias != null) {
                return alias;
            }
            try {
                return GeoIpRestrictionEnum.fromValue(value);
            } catch (IllegalArgumentException e) {
                return (GeoIpRestrictionEnum) ctxt.handleWeirdStringValue(GeoIpRestrictionEnum.class, value, e.getMessage());
            }
        }
    }
}
