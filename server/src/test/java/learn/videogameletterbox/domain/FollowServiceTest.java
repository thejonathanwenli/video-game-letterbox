package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserFollowRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.UserFollow;
import learn.videogameletterbox.models.UserFollowId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class FollowServiceTest {

    @Autowired
    FollowService service;

    @MockitoBean
    UserFollowRepository userFollowRepository;

    @MockitoBean
    AppUserRepository appUserRepository;

    @MockitoBean
    UserGameRepository userGameRepository;

    @MockitoBean
    ReviewRepository reviewRepository;

    @Test
    void shouldFollowAnotherUser() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(makeUser(2L)));

        assertTrue(service.follow(1L, 2L).isSuccess());

        ArgumentCaptor<UserFollow> captor = ArgumentCaptor.forClass(UserFollow.class);
        verify(userFollowRepository).save(captor.capture());
        assertEquals(new UserFollowId(1L, 2L), captor.getValue().getId());
        assertEquals(1L, captor.getValue().getFollower().getUserId());
        assertEquals(2L, captor.getValue().getFollowed().getUserId());
    }

    @Test
    void shouldNotSaveDuplicateFollow() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(makeUser(2L)));
        when(userFollowRepository.existsById(new UserFollowId(1L, 2L))).thenReturn(true);

        assertTrue(service.follow(1L, 2L).isSuccess());
        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void shouldRejectSelfFollow() {
        var result = service.follow(1L, 1L);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains("You cannot follow yourself."));
        verifyNoInteractions(userFollowRepository, appUserRepository);
    }

    @Test
    void shouldRejectMissingUser() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));

        assertEquals(ResultType.NOT_FOUND, service.follow(1L, 999L).getType());
        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void shouldRejectDisabledUser() {
        AppUser disabled = makeUser(2L);
        disabled.setEnabled(false);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(disabled));

        assertEquals(ResultType.NOT_FOUND, service.follow(1L, 2L).getType());
        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void shouldRemoveOnlyCurrentUsersFollow() {
        UserFollow follow = makeFollow(1L, 2L);
        when(userFollowRepository.findById(new UserFollowId(1L, 2L)))
                .thenReturn(Optional.of(follow));

        service.unfollow(1L, 2L);

        verify(userFollowRepository).delete(follow);
        verify(userFollowRepository).findById(new UserFollowId(1L, 2L));
    }

    @Test
    void shouldAllowRepeatedUnfollow() {
        service.unfollow(1L, 2L);
        verify(userFollowRepository, never()).delete(any());
    }

    @Test
    void shouldReturnEmptyFeedWithoutFollowingAnyone() {
        var response = service.findFollowing(1L);

        assertTrue(response.getRecommendations().isEmpty());
        assertTrue(response.getReviews().isEmpty());
        verifyNoInteractions(userGameRepository, reviewRepository);
    }

    @Test
    void shouldQueryOnlyEnabledFollowedUsers() {
        UserFollow enabled = makeFollow(1L, 2L);
        UserFollow disabled = makeFollow(1L, 3L);
        disabled.getFollowed().setEnabled(false);
        when(userFollowRepository.findByFollower_UserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(enabled, disabled));

        service.findFollowing(1L);

        verify(userGameRepository).findByUser_UserIdInAndRecommendedTrueOrderByUpdatedAtDesc(List.of(2L));
        verify(reviewRepository).findByUserGame_User_UserIdInOrderByCreatedAtDesc(List.of(2L));
    }

    private AppUser makeUser(Long id) {
        AppUser user = new AppUser();
        user.setUserId(id);
        user.setDisplayUsername("player" + id);
        return user;
    }

    private UserFollow makeFollow(Long followerId, Long followedId) {
        UserFollow follow = new UserFollow();
        follow.setId(new UserFollowId(followerId, followedId));
        follow.setFollower(makeUser(followerId));
        follow.setFollowed(makeUser(followedId));
        return follow;
    }
}
