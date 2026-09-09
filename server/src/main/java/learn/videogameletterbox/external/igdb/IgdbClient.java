package learn.videogameletterbox.external.igdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class IgdbClient {

    private static final Logger logger = LoggerFactory.getLogger(IgdbClient.class);
    private static final String GAME_FIELDS =
            "fields id,name,summary,first_release_date,rating,cover.image_id,genres.id,genres.name;";

    private final RestClient apiClient;
    private final RestClient authClient;
    private final String clientId;
    private final String clientSecret;
    private String accessToken;
    private Instant tokenExpiresAt;

    public IgdbClient(RestClient.Builder builder,
                      @Value("${igdb.api.base-url}") String apiBaseUrl,
                      @Value("${igdb.auth.base-url}") String authBaseUrl,
                      @Value("${igdb.client-id}") String clientId,
                      @Value("${igdb.client-secret}") String clientSecret) {
        this.apiClient = builder.clone().baseUrl(apiBaseUrl).build();
        this.authClient = builder.clone().baseUrl(authBaseUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public List<IgdbGame> search(String title) {
        String safeTitle = title.replace("\\", "\\\\").replace("\"", "\\\"");
        String query = GAME_FIELDS
                + " search \"" + safeTitle + "\";"
                + " where version_parent = null;"
                + " limit 12;";

        return requestGames(query, "Unable to search IGDB right now.");
    }

    public Optional<IgdbGame> findById(Integer igdbId) {
        String query = GAME_FIELDS + " where id = " + igdbId + "; limit 1;";
        return requestGames(query, "Unable to load that game from IGDB right now.")
                .stream()
                .findFirst();
    }

    private List<IgdbGame> requestGames(String query, String errorMessage) {
        requireCredentials();

        try {
            IgdbGame[] games = apiClient.post()
                    .uri("/games")
                    .header("Client-ID", clientId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(query)
                    .retrieve()
                    .body(IgdbGame[].class);

            return games == null ? List.of() : Arrays.asList(games);
        } catch (RestClientException ex) {
            logFailure("IGDB games request", ex);
            throw new IgdbApiException(errorMessage);
        }
    }

    private synchronized String getAccessToken() {
        if (accessToken != null
                && tokenExpiresAt != null
                && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
            return accessToken;
        }

        try {
            IgdbTokenResponse response = authClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/token")
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("grant_type", "client_credentials")
                            .build())
                    .retrieve()
                    .body(IgdbTokenResponse.class);

            if (response == null || response.getAccessToken() == null) {
                throw new IgdbApiException("Unable to authenticate with IGDB right now.");
            }

            accessToken = response.getAccessToken();
            tokenExpiresAt = Instant.now().plusSeconds(response.getExpiresIn());
            return accessToken;
        } catch (RestClientException ex) {
            logFailure("Twitch token request", ex);
            throw new IgdbApiException("Unable to authenticate with IGDB right now.");
        }
    }

    private void logFailure(String operation, RestClientException ex) {
        // Do not log request URLs, headers, response bodies, or exception messages:
        // the token request contains credentials and its response contains a token.
        if (ex instanceof RestClientResponseException responseException) {
            logger.warn("{} failed: HTTP {} ({})", operation,
                    responseException.getStatusCode().value(), ex.getClass().getSimpleName());
        } else {
            Throwable cause = ex.getMostSpecificCause();
            logger.warn("{} failed: {} (cause: {})", operation,
                    ex.getClass().getSimpleName(), cause.getClass().getSimpleName());
        }
    }

    private void requireCredentials() {
        if (clientId == null || clientId.isBlank()
                || clientSecret == null || clientSecret.isBlank()) {
            throw new IgdbApiException("IGDB_CLIENT_ID and IGDB_CLIENT_SECRET are not configured.");
        }
    }
}
