package nl.vpro.npo.metadatahub.mediaavailability;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a {@link HttpRequest} as a curl command, e.g. for logging. Request bodies are not included (they can't be read back from a {@link HttpRequest}).
 * <p>
 * Use e.g. as {@link ApiClient#setResponseInterceptor(Consumer)}: {@code client.setResponseInterceptor(response -> log.info("{} -> {}", Curl.of(response.request()), response.statusCode()))}
 */
public class Curl {

    private Curl() {
    }

    /**
     * As {@link #of(HttpRequest, boolean)}, with the value of the {@code Authorization} header masked.
     */
    public static String of(HttpRequest request) {
        return of(request, false);
    }

    /**
     * @param showAuthorization whether to show the value of the {@code Authorization} header. It contains a (bearer) token, so only use this for debugging.
     */
    public static String of(HttpRequest request, boolean showAuthorization) {
        StringBuilder curl = new StringBuilder("curl");
        if (!"GET".equals(request.method())) {
            curl.append(" -X ").append(request.method());
        }
        for (Map.Entry<String, List<String>> header : request.headers().map().entrySet()) {
            boolean authorization = "authorization".equalsIgnoreCase(header.getKey());
            for (String value : header.getValue()) {
                if (authorization && !showAuthorization) {
                    int space = value.indexOf(' ');
                    value = (space > 0 ? value.substring(0, space + 1) : "") + "***";
                }
                curl.append(" -H ").append(quote(header.getKey() + ": " + value));
            }
        }
        curl.append(' ').append(quote(request.uri().toString()));
        return curl.toString();
    }

    /**
     * A response interceptor for {@link ApiClient#setResponseInterceptor(Consumer)} that offers the request as curl command and the status code to the given consumer.
     */
    public static Consumer<HttpResponse<?>> interceptor(boolean showAuthorization, Consumer<String> logger) {
        return response -> logger.accept(of(response.request(), showAuthorization) + " -> " + response.statusCode());
    }

    static String quote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}
