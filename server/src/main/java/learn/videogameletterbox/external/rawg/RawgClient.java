package learn.videogameletterbox.external.rawg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

@Component
public class RawgClient {

    private final RestClient restClient;
    private final String apiKey;

    public RawgClient(RestClient.Builder builder,
                      @Value("${rawg.api.base-url}") String baseUrl,
                      @Value("${rawg.api.key}") String apiKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public List<RawgGame> search(String title) {
        requireApiKey();

        try {
            RawgSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games")
                            .queryParam("key", apiKey)
                            .queryParam("search", title)
                            .queryParam("search_precise", true)
                            .queryParam("page_size", 12)
                            .build())
                    .retrieve()
                    .body(RawgSearchResponse.class);

            return response == null || response.getResults() == null
                    ? List.of()
                    : response.getResults();
        } catch (RestClientException ex) {
            throw new RawgApiException("Unable to search RAWG right now.");
        }
    }

    public Optional<RawgGame> findById(Integer rawgId) {
        requireApiKey();

        try {
            RawgGame game = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games/{rawgId}")
                            .queryParam("key", apiKey)
                            .build(rawgId))
                    .retrieve()
                    .body(RawgGame.class);

            return Optional.ofNullable(game);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            throw new RawgApiException("Unable to load that game from RAWG right now.");
        } catch (RestClientException ex) {
            throw new RawgApiException("Unable to load that game from RAWG right now.");
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RawgApiException("RAWG_API_KEY is not configured.");
        }
    }
}
