package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.AddReviewRequest;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.dtos.UpdateReviewRequest;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.GameStatus;
import learn.videogameletterbox.models.Review;
import learn.videogameletterbox.models.UserGame;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    ReviewService service;

    @MockitoBean
    ReviewRepository reviewRepository;

    @MockitoBean
    UserGameRepository userGameRepository;

    @Test
    void shouldAddReviewForOwnedLibraryEntry() {
        AddReviewRequest request = makeAddRequest("Excellent game.");
        UserGame userGame = makeUserGame(GameStatus.COMPLETED);
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.of(userGame));
        when(reviewRepository.findByUserGame_UserGameId(7L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setReviewId(4L);
            review.setTimestamps();
            return review;
        });

        Result<ReviewResponse> result = service.add(1L, request);

        assertTrue(result.isSuccess());
        assertEquals(4L, result.getPayload().getReviewId());
        assertEquals("playerOne", result.getPayload().getUsername());
        assertEquals("Excellent game.", result.getPayload().getBody());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldRejectBlankReview() {
        Result<ReviewResponse> result = service.add(1L, makeAddRequest(" "));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains("Review is required."));
        verify(userGameRepository, never()).findByUserGameIdAndUser_UserId(any(), any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldReturnNotFoundForAnotherUsersLibraryEntry() {
        AddReviewRequest request = makeAddRequest("Excellent game.");
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.empty());

        Result<ReviewResponse> result = service.add(1L, request);

        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("Library entry does not exist."));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldRejectReviewForWantToPlayGame() {
        AddReviewRequest request = makeAddRequest("Excellent game.");
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.of(makeUserGame(GameStatus.WANT_TO_PLAY)));

        Result<ReviewResponse> result = service.add(1L, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains(
                "Change the game status before writing a review."));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldRejectSecondReviewForSameGame() {
        AddReviewRequest request = makeAddRequest("Another review.");
        UserGame userGame = makeUserGame(GameStatus.COMPLETED);
        Review existing = makeReview(userGame);
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.of(userGame));
        when(reviewRepository.findByUserGame_UserGameId(7L))
                .thenReturn(Optional.of(existing));

        Result<ReviewResponse> result = service.add(1L, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains("Game already has a review."));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOwnedReview() {
        Review review = makeReview(makeUserGame(GameStatus.COMPLETED));
        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setBody("Updated review.");
        when(reviewRepository.findByReviewIdAndUserGame_User_UserId(4L, 1L))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        Result<ReviewResponse> result = service.update(1L, 4L, request);

        assertTrue(result.isSuccess());
        assertEquals("Updated review.", result.getPayload().getBody());
        verify(reviewRepository).save(review);
    }

    @Test
    void shouldNotUpdateAnotherUsersReview() {
        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setBody("Updated review.");
        when(reviewRepository.findByReviewIdAndUserGame_User_UserId(4L, 1L))
                .thenReturn(Optional.empty());

        Result<ReviewResponse> result = service.update(1L, 4L, request);

        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("Review does not exist."));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldDeleteOwnedReview() {
        Review review = makeReview(makeUserGame(GameStatus.COMPLETED));
        when(reviewRepository.findByReviewIdAndUserGame_User_UserId(4L, 1L))
                .thenReturn(Optional.of(review));

        assertTrue(service.deleteById(1L, 4L));
        verify(reviewRepository).delete(review);
    }

    @Test
    void shouldNotDeleteAnotherUsersReview() {
        when(reviewRepository.findByReviewIdAndUserGame_User_UserId(4L, 1L))
                .thenReturn(Optional.empty());

        assertFalse(service.deleteById(1L, 4L));
        verify(reviewRepository, never()).delete(any());
    }

    private AddReviewRequest makeAddRequest(String body) {
        AddReviewRequest request = new AddReviewRequest();
        request.setUserGameId(7L);
        request.setBody(body);
        return request;
    }

    private UserGame makeUserGame(GameStatus status) {
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
        userGame.setStatus(status);
        userGame.setPersonalRating((byte) 10);
        userGame.setRecommended(true);
        userGame.setTimestamps();
        return userGame;
    }

    private Review makeReview(UserGame userGame) {
        Review review = new Review();
        review.setReviewId(4L);
        review.setUserGame(userGame);
        review.setBody("Excellent game.");
        review.setTimestamps();
        return review;
    }
}
