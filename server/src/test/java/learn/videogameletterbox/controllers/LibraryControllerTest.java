package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.LibraryService;
import learn.videogameletterbox.domain.Result;
import learn.videogameletterbox.dtos.LibraryEntryResponse;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.GameStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    LibraryService libraryService;

    Authentication authentication;
    LibraryEntryResponse response;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setDisplayUsername("playerOne");
        user.setEmail("player@example.com");

        Game game = new Game();
        game.setGameId(3L);
        game.setIgdbId(1942);
        game.setTitle("Portal 2");

        UserGame entry = new UserGame();
        entry.setUserGameId(7L);
        entry.setUser(user);
        entry.setGame(game);
        entry.setStatus(GameStatus.WANT_TO_PLAY);
        entry.setTimestamps();

        response = new LibraryEntryResponse(entry);
        authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("USER")));
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mvc.perform(get("/api/library"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnCurrentUsersLibrary() throws Exception {
        when(libraryService.findAll(1L, null, null)).thenReturn(List.of(response));

        mvc.perform(get("/api/library").with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userGameId").value(7))
                .andExpect(jsonPath("$[0].title").value("Portal 2"));
    }

    @Test
    void shouldAddGameToCurrentUsersLibrary() throws Exception {
        Result<LibraryEntryResponse> result = new Result<>();
        result.setPayload(response);
        when(libraryService.add(eq(1L), any())).thenReturn(result);

        mvc.perform(post("/api/library")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "igdbId": 1942,
                                  "status": "WANT_TO_PLAY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userGameId").value(7));
    }

    @Test
    void shouldDeleteOwnedLibraryEntry() throws Exception {
        when(libraryService.deleteById(1L, 7L)).thenReturn(true);

        mvc.perform(delete("/api/library/7").with(authentication(authentication)))
                .andExpect(status().isNoContent());
    }
}
