package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserFollowRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.FollowingResponse;
import learn.videogameletterbox.dtos.RecommendationResponse;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.UserFollow;
import learn.videogameletterbox.models.UserFollowId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowService {

    private final UserFollowRepository userFollowRepository;
    private final AppUserRepository appUserRepository;
    private final UserGameRepository userGameRepository;
    private final ReviewRepository reviewRepository;

    public FollowService(UserFollowRepository userFollowRepository,
                         AppUserRepository appUserRepository,
                         UserGameRepository userGameRepository,
                         ReviewRepository reviewRepository) {
        this.userFollowRepository = userFollowRepository;
        this.appUserRepository = appUserRepository;
        this.userGameRepository = userGameRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Result<Void> follow(Long followerId, Long followedId) {
        Result<Void> result = new Result<>();
        if (followerId.equals(followedId)) {
            result.addErrorMessage("You cannot follow yourself.");
            return result;
        }

        AppUser follower = appUserRepository.findById(followerId)
                .filter(AppUser::isEnabled).orElse(null);
        AppUser followed = appUserRepository.findById(followedId)
                .filter(AppUser::isEnabled).orElse(null);
        if (follower == null || followed == null) {
            result.addNotFoundMessage("User does not exist.");
            return result;
        }

        UserFollowId id = new UserFollowId(followerId, followedId);
        if (!userFollowRepository.existsById(id)) {
            UserFollow follow = new UserFollow();
            follow.setId(id);
            follow.setFollower(follower);
            follow.setFollowed(followed);
            userFollowRepository.save(follow);
        }
        return result;
    }

    @Transactional
    public void unfollow(Long followerId, Long followedId) {
        userFollowRepository.findById(new UserFollowId(followerId, followedId))
                .ifPresent(userFollowRepository::delete);
    }

    @Transactional(readOnly = true)
    public FollowingResponse findFollowing(Long userId) {
        List<Long> followedIds = userFollowRepository.findByFollower_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(UserFollow::getFollowed)
                .filter(AppUser::isEnabled)
                .map(AppUser::getUserId)
                .toList();

        if (followedIds.isEmpty()) {
            return new FollowingResponse(List.of(), List.of());
        }

        return new FollowingResponse(
                userGameRepository.findByUser_UserIdInAndRecommendedTrueOrderByUpdatedAtDesc(followedIds)
                        .stream().map(RecommendationResponse::new).toList(),
                reviewRepository.findByUserGame_User_UserIdInOrderByCreatedAtDesc(followedIds)
                        .stream().map(ReviewResponse::new).toList());
    }
}
