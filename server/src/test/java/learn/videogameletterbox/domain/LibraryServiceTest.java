package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.AddLibraryEntryRequest;
import learn.videogameletterbox.dtos.LibraryEntryResponse;
import learn.videogameletterbox.dtos.UpdateLibraryEntryRequest;
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
class LibraryServiceTest {

    @Autowired
    LibraryService service;

    @MockitoBean
    UserGameRepository userGameRepository;

    @MockitoBean
    AppUserRepository appUserRepository;

    @MockitoBean
    GameService gameService;

    @Test
    void shouldAddGameToLibrary() {
        AddLibraryEntryRequest request = makeAddRequest(GameStatus.WANT_TO_PLAY);
        AppUser user = makeUser();
        Game game = makeGame();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameService.findOrImport(1942)).thenReturn(game);
        when(userGameRepository.save(any(UserGame.class))).thenAnswer(invocation -> {
            UserGame entry = invocation.getArgument(0);
            entry.setUserGameId(7L);
            entry.setTimestamps();
            return entry;
        });

        Result<LibraryEntryResponse> result = service.add(1L, request);

        assertTrue(result.isSuccess());
        assertEquals(7L, result.getPayload().getUserGameId());
        assertEquals("Portal 2", result.getPayload().getTitle());
        verify(gameService).findOrImport(1942);
        verify(userGameRepository).save(any(UserGame.class));
    }

    @Test
    void shouldRejectDuplicateLibraryGame() {
        AddLibraryEntryRequest request = makeAddRequest(GameStatus.WANT_TO_PLAY);
        when(userGameRepository.existsByUser_UserIdAndGame_IgdbId(1L, 1942)).thenReturn(true);

        Result<LibraryEntryResponse> result = service.add(1L, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains("Game is already in your library."));
        verify(gameService, never()).findOrImport(any());
        verify(userGameRepository, never()).save(any());
    }

    @Test
    void shouldRejectRatingForWantToPlay() {
        AddLibraryEntryRequest request = makeAddRequest(GameStatus.WANT_TO_PLAY);
        request.setPersonalRating((byte) 8);

        Result<LibraryEntryResponse> result = service.add(1L, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains(
                "Want to Play games cannot have a rating or recommendation."));
        verify(userGameRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOwnedLibraryEntry() {
        UserGame entry = makeEntry();
        UpdateLibraryEntryRequest request = new UpdateLibraryEntryRequest();
        request.setStatus(GameStatus.COMPLETED);
        request.setPersonalRating((byte) 9);
        request.setRecommended(true);
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.of(entry));
        when(userGameRepository.save(entry)).thenReturn(entry);

        Result<LibraryEntryResponse> result = service.update(1L, 7L, request);

        assertTrue(result.isSuccess());
        assertEquals(GameStatus.COMPLETED, result.getPayload().getStatus());
        assertEquals((byte) 9, result.getPayload().getPersonalRating());
        assertTrue(result.getPayload().getRecommended());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingAnotherUsersEntry() {
        UpdateLibraryEntryRequest request = new UpdateLibraryEntryRequest();
        request.setStatus(GameStatus.PLAYING);
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.empty());

        Result<LibraryEntryResponse> result = service.update(1L, 7L, request);

        assertEquals(ResultType.NOT_FOUND, result.getType());
        assertTrue(result.getMessages().contains("Library entry does not exist."));
        verify(userGameRepository, never()).save(any());
    }

    @Test
    void shouldRejectWantToPlayWhenEntryHasReview() {
        UserGame entry = makeEntry();
        Review review = new Review();
        review.setUserGame(entry);
        review.setBody("A review.");
        entry.setReview(review);

        UpdateLibraryEntryRequest request = new UpdateLibraryEntryRequest();
        request.setStatus(GameStatus.WANT_TO_PLAY);
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.of(entry));

        Result<LibraryEntryResponse> result = service.update(1L, 7L, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains(
                "Remove your review before changing status to Want to Play."));
        verify(userGameRepository, never()).save(any());
    }

    @Test
    void shouldNotDeleteAnotherUsersEntry() {
        when(userGameRepository.findByUserGameIdAndUser_UserId(7L, 1L))
                .thenReturn(Optional.empty());

        assertFalse(service.deleteById(1L, 7L));
        verify(userGameRepository, never()).delete(any());
    }

    private AddLibraryEntryRequest makeAddRequest(GameStatus status) {
        AddLibraryEntryRequest request = new AddLibraryEntryRequest();
        request.setIgdbId(1942);
        request.setStatus(status);
        return request;
    }

    private AppUser makeUser() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setDisplayUsername("playerOne");
        user.setEmail("player@example.com");
        return user;
    }

    private Game makeGame() {
        Game game = new Game();
        game.setGameId(3L);
        game.setIgdbId(1942);
        game.setTitle("Portal 2");
        return game;
    }

    private UserGame makeEntry() {
        UserGame entry = new UserGame();
        entry.setUserGameId(7L);
        entry.setUser(makeUser());
        entry.setGame(makeGame());
        entry.setStatus(GameStatus.PLAYING);
        entry.setTimestamps();
        return entry;
    }
}
