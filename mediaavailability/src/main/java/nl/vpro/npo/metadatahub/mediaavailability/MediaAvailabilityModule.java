package nl.vpro.npo.metadatahub.mediaavailability;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serial;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import nl.vpro.npo.metadatahub.mediaavailability.domain.MediaAvailabilityPeriod.GeoIpRestrictionEnum;

/**
 * Jackson module to work around deviations in the actual messages from the openapi spec.
 * <p>
 * E.g. {@code geoIpRestriction} is sometimes sent as {@code WERELD}, where {@code WR} is meant.
 * <p>
 * Enum values which are not in the spec at all (e.g. platform {@code hergebruik}) are logged as a warning, and mapped to {@code null},
 * so they don't make the complete message fail.
 * <p>
 * Registered via {@code META-INF/services}, so {@link ObjectMapper#findAndRegisterModules()} picks it up too.
 */
@Slf4j
public class MediaAvailabilityModule extends SimpleModule {

    static final Map<String, GeoIpRestrictionEnum> GEO_IP_RESTRICTION_ALIASES = Map.of(
        "WERELD", GeoIpRestrictionEnum.WR,
        "NL ALL", GeoIpRestrictionEnum.NLALL
    );
    @Serial
    private static final long serialVersionUID = 4496088846591320188L;

    public MediaAvailabilityModule() {
        super(MediaAvailabilityModule.class.getSimpleName());
        addDeserializer(GeoIpRestrictionEnum.class, new GeoIpRestrictionDeserializer());

    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addDeserializationProblemHandler(new UnknownEnumValuesAsNull());
    }

    /**
     * The default object mapper of the generated {@link ApiClient}, with this module registered.
     */
    public static ObjectMapper createObjectMapper() {
        return ApiClient.createDefaultObjectMapper().registerModule(new MediaAvailabilityModule());
    }

    /**
     * Maps unknown enum values to {@code null}, with a warning.
     * <p>
     * The generated enums have a {@code @JsonCreator fromValue}, which throws an {@link IllegalArgumentException} for unknown values. That
     * arrives at {@link #handleInstantiationProblem}. Enums without such a creator (and {@link GeoIpRestrictionDeserializer}) arrive at
     * {@link #handleWeirdStringValue}.
     */
    static class UnknownEnumValuesAsNull extends DeserializationProblemHandler {

        @Override
        public Object handleInstantiationProblem(DeserializationContext ctxt, Class<?> instClass, Object argument, Throwable t) {
            if (instClass.isEnum()) {
                return unknown(ctxt, instClass, argument);
            }
            return NOT_HANDLED;
        }

        @Override
        public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) {
            if (targetType.isEnum()) {
                return unknown(ctxt, targetType, valueToConvert);
            }
            return NOT_HANDLED;
        }

        private static Object unknown(DeserializationContext ctxt, Class<?> enumClass, Object value) {
            final JsonParser parser = ctxt.getParser();
            log.warn("Unknown value '{}' for {} at {}. Mapped to null",
                value, enumClass.getSimpleName(), parser == null ? null : parser.getParsingContext().pathAsPointer());
            return null;
        }
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
