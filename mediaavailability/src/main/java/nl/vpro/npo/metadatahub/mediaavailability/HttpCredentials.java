package nl.vpro.npo.metadatahub.mediaavailability;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Request interceptor for {@link ApiClient#setRequestInterceptor(Consumer)} that obtains an access token using the OAuth2
 * client credentials grant, caches it until shortly before it expires, and adds it as a bearer token to every request.
 */
public class HttpCredentials implements Consumer<HttpRequest.Builder> {

    public static final URI SSO_ENDPOINT_ACC = URI.create("https://sso.metadatahub.bijnpo.nl/auth/realms/MDS-ACC/protocol/openid-connect/token");

    private static final Duration EXPIRY_MARGIN = Duration.ofSeconds(30);

    private final URI ssoEndpoint;
    private final String basicAuth;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    private String accessToken;
    private Instant expires = Instant.EPOCH;

    public HttpCredentials(URI ssoEndpoint, String clientId, String clientSecret) {
        this(ssoEndpoint, clientId, clientSecret, HttpClient.newHttpClient());
    }

    public HttpCredentials(URI ssoEndpoint, String clientId, String clientSecret, HttpClient http) {
        this.ssoEndpoint = ssoEndpoint;
        this.basicAuth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(UTF_8));
        this.http = http;
    }

    @Override
    public void accept(HttpRequest.Builder builder) {
        builder.header("Authorization", "Bearer " + getAccessToken());
    }

    public synchronized String getAccessToken() {
        if (accessToken == null || Instant.now().isAfter(expires.minus(EXPIRY_MARGIN))) {
            fetchToken();
        }
        return accessToken;
    }

    private void fetchToken() {
        HttpRequest request = HttpRequest.newBuilder(ssoEndpoint)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", "Basic " + basicAuth)
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Could not obtain access token from " + ssoEndpoint + ": " + response.statusCode() + " " + response.body());
            }
            JsonNode json = mapper.readTree(response.body());
            accessToken = json.get("access_token").asText();
            expires = Instant.now().plusSeconds(json.path("expires_in").asLong(60));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
