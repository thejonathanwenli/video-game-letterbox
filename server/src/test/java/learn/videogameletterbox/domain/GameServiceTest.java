package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.GameRepository;
import learn.videogameletterbox.data.GenreRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.GameSummaryResponse;
import learn.videogameletterbox.external.rawg.RawgClient;
import learn.videogameletterbox.external.rawg.RawgGame;
import learn.videogameletterbox.external.rawg.RawgGenre;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    RawgClient rawgClient;

    @MockitoBean
    GameRepository gameRepository;

    @MockitoBean
    GenreRepository genreRepository;

    @MockitoBean
    UserGameRepository userGameRepository;

    @MockitoBean
    ReviewRepository reviewRepository;

    @Test
    void shouldSearchRawgByTitle() {
        RawgGame rawgGame = makeRawgGame();
        when(rawgClient.search("Portal")).thenReturn(List.of(rawgGame));
        when(gameRepository.findByRawgId(4200)).thenReturn(Optional.empty());

        List<GameSummaryResponse> result = service.search("Portal");

        assertEquals(1, result.size());
        assertEquals(4200, result.getFirst().getRawgId());
        assertEquals("Portal 2", result.getFirst().getTitle());
        assertEquals(List.of("Puzzle"), result.getFirst().getGenres());
    }

    @Test
    void shouldReturnEmptyWhenRawgGameDoesNotExist() {
        when(rawgClient.findById(999999)).thenReturn(Optional.empty());

        assertTrue(service.findByRawgId(999999).isEmpty());
    }

    @Test
    void shouldImportAGameAndItsGenres() {
        RawgGame rawgGame = makeRawgGame();
        when(gameRepository.findByRawgId(4200)).thenReturn(Optional.empty());
        when(rawgClient.findById(4200)).thenReturn(Optional.of(rawgGame));
        when(genreRepository.findByRawgId(7)).thenReturn(Optional.empty());
        when(genreRepository.findByNameIgnoreCase("Puzzle")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game result = service.findOrImport(4200);

        assertEquals(4200, result.getRawgId());
        assertEquals("Portal 2", result.getTitle());
        assertEquals(1, result.getGenres().size());
        verify(genreRepository).save(any(Genre.class));
        verify(gameRepository).save(result);
    }

    @Test
    void shouldNotImportAGameThatAlreadyExists() {
        Game existing = new Game();
        existing.setGameId(3L);
        existing.setRawgId(4200);
        existing.setTitle("Portal 2");
        when(gameRepository.findByRawgId(4200)).thenReturn(Optional.of(existing));

        Game result = service.findOrImport(4200);

        assertSame(existing, result);
        verify(rawgClient, never()).findById(4200);
        verify(gameRepository, never()).save(any());
    }

    private RawgGame makeRawgGame() {
        RawgGenre genre = new RawgGenre();
        genre.setId(7);
        genre.setName("Puzzle");

        RawgGame game = new RawgGame();
        game.setId(4200);
        game.setName("Portal 2");
        game.setDescription("A puzzle game.");
        game.setBackgroundImage("https://example.com/portal-2.jpg");
        game.setReleased(LocalDate.of(2011, 4, 18));
        game.setRating(new BigDecimal("4.61"));
        game.setGenres(List.of(genre));
        return game;
    }
}
