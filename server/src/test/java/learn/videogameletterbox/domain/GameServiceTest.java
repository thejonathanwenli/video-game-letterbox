package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.GameRepository;
import learn.videogameletterbox.data.GenreRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.GameSummaryResponse;
import learn.videogameletterbox.dtos.GameDetailsResponse;
import learn.videogameletterbox.external.igdb.IgdbApiException;
import learn.videogameletterbox.external.igdb.IgdbClient;
import learn.videogameletterbox.external.igdb.IgdbCover;
import learn.videogameletterbox.external.igdb.IgdbGame;
import learn.videogameletterbox.external.igdb.IgdbGenre;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class GameServiceTest {

    @Autowired
    GameService service;

    @MockitoBean
    IgdbClient igdbClient;

    @MockitoBean
    GameRepository gameRepository;

    @MockitoBean
    GenreRepository genreRepository;

    @MockitoBean
    UserGameRepository userGameRepository;

    @MockitoBean
    ReviewRepository reviewRepository;

    @Test
    void shouldSearchIgdbByTitle() {
        IgdbGame igdbGame = makeIgdbGame();
        when(igdbClient.search("Portal")).thenReturn(List.of(igdbGame));
        when(gameRepository.findByIgdbId(1942)).thenReturn(Optional.empty());

        List<GameSummaryResponse> result = service.search("Portal");

        assertEquals(1, result.size());
        assertEquals(1942, result.getFirst().getIgdbId());
        assertEquals("Portal 2", result.getFirst().getTitle());
        assertEquals(List.of("Puzzle"), result.getFirst().getGenres());
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/portal-cover.jpg",
                result.getFirst().getImageUrl());
    }

    @Test
    void shouldReturnEmptyWhenIgdbGameDoesNotExist() {
        when(igdbClient.findById(999999)).thenReturn(Optional.empty());

        assertTrue(service.findByIgdbId(999999).isEmpty());
    }

    @Test
    void shouldStillPreferIgdbWhenSavedGameExists() {
        Game savedGame = new Game();
        savedGame.setGameId(3L);
        savedGame.setTitle("Older title");
        when(gameRepository.findByIgdbId(1942)).thenReturn(Optional.of(savedGame));
        when(igdbClient.findById(1942)).thenReturn(Optional.of(makeIgdbGame()));

        GameDetailsResponse result = service.findByIgdbId(1942).orElseThrow();

        assertEquals("Portal 2", result.getTitle());
        assertEquals("A puzzle game.", result.getDescription());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void shouldKeepProviderErrorWhenGameHasNotBeenSaved() {
        IgdbApiException failure = new IgdbApiException("Unable to load that game from IGDB right now.");
        when(igdbClient.findById(1942)).thenThrow(failure);
        when(gameRepository.findByIgdbId(1942)).thenReturn(Optional.empty());

        assertSame(failure, assertThrows(IgdbApiException.class, () -> service.findByIgdbId(1942)));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void shouldNotHideUnrelatedProgrammingErrorsWithFallback() {
        when(igdbClient.findById(1942)).thenThrow(new IllegalStateException("Unexpected error"));

        assertThrows(IllegalStateException.class, () -> service.findByIgdbId(1942));
        verify(gameRepository, never()).findByIgdbId(1942);
    }

    @Test
    void shouldImportAGameAndItsGenres() {
        IgdbGame igdbGame = makeIgdbGame();
        when(gameRepository.findByIgdbId(1942)).thenReturn(Optional.empty());
        when(igdbClient.findById(1942)).thenReturn(Optional.of(igdbGame));
        when(genreRepository.findByIgdbId(9)).thenReturn(Optional.empty());
        when(genreRepository.findByNameIgnoreCase("Puzzle")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game result = service.findOrImport(1942);

        assertEquals(1942, result.getIgdbId());
        assertEquals("Portal 2", result.getTitle());
        assertEquals(1, result.getGenres().size());
        verify(genreRepository).save(any(Genre.class));
        verify(gameRepository).save(result);
    }

    @Test
    void shouldNotImportAGameThatAlreadyExists() {
        Game existing = new Game();
        existing.setGameId(3L);
        existing.setIgdbId(1942);
        existing.setTitle("Portal 2");
        when(gameRepository.findByIgdbId(1942)).thenReturn(Optional.of(existing));

        Game result = service.findOrImport(1942);

        assertSame(existing, result);
        verify(igdbClient, never()).findById(1942);
        verify(gameRepository, never()).save(any());
    }

    private IgdbGame makeIgdbGame() {
        IgdbGenre genre = new IgdbGenre();
        genre.setId(9);
        genre.setName("Puzzle");

        IgdbCover cover = new IgdbCover();
        cover.setImageId("portal-cover");

        IgdbGame game = new IgdbGame();
        game.setId(1942);
        game.setName("Portal 2");
        game.setSummary("A puzzle game.");
        game.setFirstReleaseDate(1303084800L);
        game.setRating(new BigDecimal("91.50"));
        game.setCover(cover);
        game.setGenres(List.of(genre));
        return game;
    }
}
