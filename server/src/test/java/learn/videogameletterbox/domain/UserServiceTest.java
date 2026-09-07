package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserFollowRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.UpdateProfileRequest;
import learn.videogameletterbox.models.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService service;

    @MockitoBean
    AppUserRepository appUserRepository;

    @MockitoBean
    UserGameRepository userGameRepository;

    @MockitoBean
    ReviewRepository reviewRepository;

    @MockitoBean
    UserFollowRepository userFollowRepository;

    AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setUserId(2L);
        user.setDisplayUsername("playerTwo");
        user.setBio("I enjoy puzzle games.");
    }

    @Test
    void shouldReadPublicProfileWithoutLogin() {
        when(appUserRepository.findByDisplayUsernameIgnoreCase("playerTwo"))
                .thenReturn(Optional.of(user));

        var profile = service.findByUsername("playerTwo", null).orElseThrow();

        assertEquals("playerTwo", profile.getUsername());
        assertEquals("I enjoy puzzle games.", profile.getBio());
        assertFalse(profile.isFollowing());
        assertTrue(profile.getLibrary().isEmpty());
        assertTrue(profile.getReviews().isEmpty());
        verifyNoInteractions(userFollowRepository);
    }

    @Test
    void shouldIncludeCurrentViewersFollowState() {
        when(appUserRepository.findByDisplayUsernameIgnoreCase("playerTwo"))
                .thenReturn(Optional.of(user));
        when(userFollowRepository.existsByFollower_UserIdAndFollowed_UserId(1L, 2L))
                .thenReturn(true);

        assertTrue(service.findByUsername("playerTwo", 1L).orElseThrow().isFollowing());
    }

    @Test
    void shouldNotReturnMissingOrDisabledProfile() {
        assertTrue(service.findByUsername("missing", null).isEmpty());
        user.setEnabled(false);
        when(appUserRepository.findByDisplayUsernameIgnoreCase("playerTwo"))
                .thenReturn(Optional.of(user));

        assertTrue(service.findByUsername("playerTwo", null).isEmpty());
        verifyNoInteractions(userGameRepository, reviewRepository);
    }

    @Test
    void shouldUpdateOnlyBiography() {
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(user));
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("  New biography.  ");

        assertTrue(service.updateBio(2L, request).isSuccess());
        assertEquals("New biography.", user.getBio());
        assertEquals("playerTwo", user.getDisplayUsername());
        verify(appUserRepository).save(user);
    }

    @Test
    void shouldAllowClearingBiography() {
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(user));
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("   ");

        assertTrue(service.updateBio(2L, request).isSuccess());
        assertNull(user.getBio());
        assertTrue(service.updateBio(2L, new UpdateProfileRequest()).isSuccess());
    }

    @Test
    void shouldRejectLongBiographyWithoutSaving() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("x".repeat(501));

        var result = service.updateBio(2L, request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains("Biography must be 500 characters or fewer."));
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void shouldRejectNullProfileWithoutSaving() {
        assertFalse(service.updateBio(2L, null).isSuccess());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void shouldReturnNotFoundForMissingOwner() {
        assertEquals(ResultType.NOT_FOUND, service.updateBio(999L, new UpdateProfileRequest()).getType());
        verify(appUserRepository, never()).save(any());
    }
}
