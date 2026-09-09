package learn.videogameletterbox.controllers;

import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.GameRepository;
import learn.videogameletterbox.data.GenreRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.external.igdb.IgdbApiException;
import learn.videogameletterbox.external.igdb.IgdbClient;
import learn.videogameletterbox.models.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GameFallbackIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AppUserRepository users;
    @Autowired GameRepository games;
    @Autowired GenreRepository genres;
    @Autowired UserGameRepository library;
    @Autowired ReviewRepository reviews;
    @MockitoBean IgdbClient igdbClient;

    @Test
    void shouldReturnSavedGameAndCurrentCommunityDataWhenIgdbFails() throws Exception {
        // These fixtures must never be written to the production or demo database.
        assertEquals("video_game_letterbox_test", jdbc.queryForObject("select database()", String.class));
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        AppUser user = new AppUser();
        user.setDisplayUsername("fallback_" + suffix);
        user.setEmail("fallback_" + suffix + "@example.com");
        user.setPasswordHash("test-only-password-hash");
        user = users.save(user);

        Genre genre = new Genre();
        genre.setIgdbId(1900000001);
        genre.setName("Fallback Genre " + suffix);
        genre = genres.save(genre);
        Game game = new Game();
        game.setIgdbId(1900000001);
        game.setTitle("Saved game");
        game.setDescription("Saved description");
        game.setImageUrl("https://example.com/saved-cover.jpg");
        game.setReleaseDate(LocalDate.of(2020, 1, 2));
        game.setIgdbRating(new BigDecimal("86.50"));
        game.getGenres().add(genre);
        game = games.save(game);

        UserGame entry = new UserGame();
        entry.setUser(user);
        entry.setGame(game);
        entry.setStatus(GameStatus.PLAYING);
        entry.setPersonalRating((byte) 9);
        entry.setRecommended(true);
        entry = library.save(entry);
        Review review = new Review();
        review.setUserGame(entry);
        review.setBody("This review should still be visible.");
        reviews.saveAndFlush(review);
        when(igdbClient.findById(1900000001)).thenThrow(new IgdbApiException("IGDB is unavailable."));

        mvc.perform(get("/api/games/1900000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.igdbId").value(1900000001))
                .andExpect(jsonPath("$.title").value("Saved game"))
                .andExpect(jsonPath("$.description").value("Saved description"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/saved-cover.jpg"))
                .andExpect(jsonPath("$.releaseDate").value("2020-01-02"))
                .andExpect(jsonPath("$.igdbRating").value(86.5))
                .andExpect(jsonPath("$.genres[0]").value(genre.getName()))
                .andExpect(jsonPath("$.communityRating").value(9.0))
                .andExpect(jsonPath("$.recommendationPercentage").value(100))
                .andExpect(jsonPath("$.reviews[0].body").value(review.getBody()))
                .andExpect(jsonPath("$.reviews[0].username").value(user.getDisplayUsername()));
    }
}
