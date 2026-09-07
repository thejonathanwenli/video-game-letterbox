package learn.videogameletterbox.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.GameRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserFollowRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.GameStatus;
import learn.videogameletterbox.models.Review;
import learn.videogameletterbox.models.UserFollowId;
import learn.videogameletterbox.models.UserGame;
import learn.videogameletterbox.security.JwtConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileFollowingIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    AppUserRepository users;
    @Autowired
    GameRepository games;
    @Autowired
    UserGameRepository library;
    @Autowired
    ReviewRepository reviews;
    @Autowired
    UserFollowRepository follows;
    @Autowired
    JwtConverter jwt;

    AppUser alice;
    AppUser bob;
    AppUser charlie;
    String aliceToken;
    String charlieToken;

    @BeforeEach
    void setUp() {
        // Refuse to create fixtures outside the test database; each test rolls them back.
        assertEquals("video_game_letterbox_test", jdbc.queryForObject("select database()", String.class));
        alice = makeUser("alice");
        bob = makeUser("bob");
        charlie = makeUser("charlie");
        aliceToken = "Bearer " + jwt.getTokenFromUser(alice);
        charlieToken = "Bearer " + jwt.getTokenFromUser(charlie);
    }

    @Test
    void shouldReadPublicProfileWithLibraryAndReviewsWithoutLeakingPrivateFields() throws Exception {
        UserGame entry = makeEntry(bob, "A puzzle game", true, 1);
        makeReview(entry, "Wonderful puzzles.", 0);

        mvc.perform(get("/api/users/{username}", bob.getDisplayUsername().toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(bob.getUserId()))
                .andExpect(jsonPath("$.library[0].title").value("A puzzle game"))
                .andExpect(jsonPath("$.reviews[0].body").value("Wonderful puzzles."))
                .andExpect(jsonPath("$.reviews[0].gameTitle").value("A puzzle game"))
                .andExpect(jsonPath("$.following").value(false))
                .andExpect(jsonPath("$..email").isEmpty())
                .andExpect(jsonPath("$..passwordHash").isEmpty())
                .andExpect(jsonPath("$..password").isEmpty())
                .andExpect(jsonPath("$..roles").isEmpty());
    }

    @Test
    void shouldReturn404ForMissingProfile() throws Exception {
        mvc.perform(get("/api/users/missing-" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateOnlyAuthenticatedUsersBioEvenWithExtraBodyFields() throws Exception {
        mvc.perform(put("/api/users/me").header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "userId", bob.getUserId(), "bio", "  I enjoy games.  ",
                                "username", "hijacked", "enabled", false))))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/users/{username}", alice.getDisplayUsername()))
                .andExpect(jsonPath("$.bio").value("I enjoy games."));
        mvc.perform(get("/api/users/{username}", bob.getDisplayUsername()))
                .andExpect(jsonPath("$.bio").doesNotExist());
        assertTrue(users.findById(alice.getUserId()).orElseThrow().isEnabled());
    }

    @Test
    void shouldRejectOversizedBioAndAllowClearingIt() throws Exception {
        mvc.perform(put("/api/users/me").header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("bio", "x".repeat(501)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0]").value("Biography must be 500 characters or fewer."));
        mvc.perform(put("/api/users/me").header("Authorization", aliceToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"bio\":\"\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRequireLoginForAllWritesAndFollowingFeed() throws Exception {
        mvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON).content("{\"bio\":\"Test\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/follows/{id}", bob.getUserId())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/follows/{id}", bob.getUserId())).andExpect(status().isForbidden());
        mvc.perform(get("/api/following")).andExpect(status().isForbidden());
    }

    @Test
    void shouldFollowOnceAndKeepEachViewersFollowStateSeparate() throws Exception {
        follow(aliceToken, bob);
        follow(aliceToken, bob);
        assertEquals(1, jdbc.queryForObject(
                "select count(*) from user_follow where follower_id = ? and followed_id = ?",
                Integer.class, alice.getUserId(), bob.getUserId()));

        mvc.perform(get("/api/users/{username}", bob.getDisplayUsername()).header("Authorization", aliceToken))
                .andExpect(jsonPath("$.following").value(true));
        mvc.perform(get("/api/users/{username}", bob.getDisplayUsername()).header("Authorization", charlieToken))
                .andExpect(jsonPath("$.following").value(false));
    }

    @Test
    void shouldRejectSelfAndMissingFollowTargets() throws Exception {
        mvc.perform(put("/api/follows/{id}", alice.getUserId()).header("Authorization", aliceToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0]").value("You cannot follow yourself."));
        mvc.perform(put("/api/follows/-1").header("Authorization", aliceToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRemoveOnlyCurrentUsersFollowAndAllowRepeatingDelete() throws Exception {
        follow(aliceToken, bob);
        follow(charlieToken, bob);

        mvc.perform(delete("/api/follows/{id}", bob.getUserId()).header("Authorization", aliceToken))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/follows/{id}", bob.getUserId()).header("Authorization", aliceToken))
                .andExpect(status().isNoContent());

        assertFalse(follows.existsById(new UserFollowId(alice.getUserId(), bob.getUserId())));
        assertTrue(follows.existsById(new UserFollowId(charlie.getUserId(), bob.getUserId())));
    }

    @Test
    void shouldReturnOnlyFollowedUsersRecommendationsAndReviewsInOrder() throws Exception {
        UserGame older = makeEntry(bob, "Older game", true, 1);
        UserGame newer = makeEntry(bob, "Newer game", true, 2);
        makeEntry(bob, "Not recommended", false, 3);
        makeEntry(bob, "No opinion", null, 4);
        UserGame unrelated = makeEntry(charlie, "Unfollowed game", true, 5);
        UserGame own = makeEntry(alice, "My game", true, 6);
        makeReview(older, "Older review", 2);
        makeReview(newer, "Newer review", 1);
        makeReview(unrelated, "Not in feed", 0);
        makeReview(own, "My own review", 0);
        // Fix timestamps explicitly so ordering does not depend on execution speed.
        library.flush();
        jdbc.update("update user_game set updated_at = ? where user_game_id = ?",
                LocalDateTime.now().minusDays(2), older.getUserGameId());
        jdbc.update("update user_game set updated_at = ? where user_game_id = ?",
                LocalDateTime.now().minusDays(1), newer.getUserGameId());
        follow(aliceToken, bob);

        mvc.perform(get("/api/following").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations.length()").value(2))
                .andExpect(jsonPath("$.recommendations[0].entry.title").value("Newer game"))
                .andExpect(jsonPath("$.recommendations[1].entry.title").value("Older game"))
                .andExpect(jsonPath("$.recommendations[0].username").value(bob.getDisplayUsername()))
                .andExpect(jsonPath("$.reviews.length()").value(2))
                .andExpect(jsonPath("$.reviews[0].body").value("Newer review"))
                .andExpect(jsonPath("$.reviews[1].body").value("Older review"));

        mvc.perform(delete("/api/follows/{id}", bob.getUserId()).header("Authorization", aliceToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/following").header("Authorization", aliceToken))
                .andExpect(jsonPath("$.recommendations").isEmpty())
                .andExpect(jsonPath("$.reviews").isEmpty());
    }

    @Test
    void shouldReturnEmptyFeedBeforeFollowingAnyone() throws Exception {
        mvc.perform(get("/api/following").header("Authorization", aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isEmpty())
                .andExpect(jsonPath("$.reviews").isEmpty());
    }

    private void follow(String token, AppUser target) throws Exception {
        mvc.perform(put("/api/follows/{id}", target.getUserId()).header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    private AppUser makeUser(String prefix) {
        String username = prefix + UUID.randomUUID().toString().substring(0, 8);
        AppUser user = new AppUser();
        user.setDisplayUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("not-a-real-password-hash");
        return users.saveAndFlush(user);
    }

    private UserGame makeEntry(AppUser user, String title, Boolean recommended, int number) {
        Game game = new Game();
        game.setIgdbId(-900000 - number);
        game.setTitle(title);
        games.saveAndFlush(game);

        UserGame entry = new UserGame();
        entry.setUser(user);
        entry.setGame(game);
        entry.setStatus(GameStatus.COMPLETED);
        entry.setPersonalRating((byte) 9);
        entry.setRecommended(recommended);
        return library.saveAndFlush(entry);
    }

    private void makeReview(UserGame entry, String body, int daysAgo) {
        Review review = new Review();
        review.setUserGame(entry);
        review.setBody(body);
        review.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        reviews.saveAndFlush(review);
    }
}
