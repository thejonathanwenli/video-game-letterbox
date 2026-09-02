package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.GameService;
import learn.videogameletterbox.dtos.GameDetailsResponse;
import learn.videogameletterbox.dtos.GameSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    GameService gameService;

    @Test
    void shouldAllowPublicGameSearch() throws Exception {
        when(gameService.search("Portal")).thenReturn(List.of(makeSummary()));

        mvc.perform(get("/api/games/search").param("title", "Portal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].igdbId").value(1942))
                .andExpect(jsonPath("$[0].title").value("Portal 2"));
    }

    @Test
    void shouldRejectBlankSearchTitle() throws Exception {
        mvc.perform(get("/api/games/search").param("title", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0]").value("Search title is required."));
    }

    @Test
    void shouldFindGameDetails() throws Exception {
        GameSummaryResponse summary = makeSummary();
        GameDetailsResponse details = new GameDetailsResponse(
                summary.getIgdbId(),
                summary.getTitle(),
                "A puzzle game.",
                summary.getImageUrl(),
                summary.getReleaseDate(),
                summary.getIgdbRating(),
                summary.getGenres(),
                summary.getCommunityRating(),
                summary.getRecommendationPercentage(),
                List.of());
        when(gameService.findByIgdbId(1942)).thenReturn(Optional.of(details));

        mvc.perform(get("/api/games/1942"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Portal 2"))
                .andExpect(jsonPath("$.description").value("A puzzle game."));
    }

    @Test
    void shouldReturn404ForMissingGame() throws Exception {
        when(gameService.findByIgdbId(999999)).thenReturn(Optional.empty());

        mvc.perform(get("/api/games/999999"))
                .andExpect(status().isNotFound());
    }

    private GameSummaryResponse makeSummary() {
        return new GameSummaryResponse(
                1942,
                "Portal 2",
                "https://example.com/portal-2.jpg",
                LocalDate.of(2011, 4, 18),
                new BigDecimal("91.50"),
                List.of("Puzzle"),
                null,
                null);
    }
}
