package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.Result;
import learn.videogameletterbox.domain.ReviewService;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.GameStatus;
import learn.videogameletterbox.models.Review;
import learn.videogameletterbox.models.UserGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ReviewService reviewService;

    Authentication authentication;
    ReviewResponse response;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setDisplayUsername("playerOne");

        Game game = new Game();
        game.setGameId(3L);
        game.setIgdbId(1942);
        game.setTitle("Portal 2");

        UserGame userGame = new UserGame();
        userGame.setUserGameId(7L);
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setStatus(GameStatus.COMPLETED);
        userGame.setPersonalRating((byte) 10);
        userGame.setRecommended(true);
        userGame.setTimestamps();

        Review review = new Review();
        review.setReviewId(4L);
        review.setUserGame(userGame);
        review.setBody("Excellent game.");
        review.setTimestamps();
        response = new ReviewResponse(review);

        authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("USER")));
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userGameId": 7,
                                  "body": "Excellent game."
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAddReview() throws Exception {
        Result<ReviewResponse> result = new Result<>();
        result.setPayload(response);
        when(reviewService.add(eq(1L), any())).thenReturn(result);

        mvc.perform(post("/api/reviews")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userGameId": 7,
                                  "body": "Excellent game."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(4))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.body").value("Excellent game."));
    }

    @Test
    void shouldReturnBadRequestForInvalidReview() throws Exception {
        Result<ReviewResponse> result = new Result<>();
        result.addErrorMessage("Review is required.");
        when(reviewService.add(eq(1L), any())).thenReturn(result);

        mvc.perform(post("/api/reviews")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userGameId": 7,
                                  "body": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0]").value("Review is required."));
    }

    @Test
    void shouldUpdateReview() throws Exception {
        Result<ReviewResponse> result = new Result<>();
        result.setPayload(response);
        when(reviewService.update(eq(1L), eq(4L), any())).thenReturn(result);

        mvc.perform(put("/api/reviews/4")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "body": "Updated review."
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingReview() throws Exception {
        when(reviewService.deleteById(1L, 999L)).thenReturn(false);

        mvc.perform(delete("/api/reviews/999").with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }
}
