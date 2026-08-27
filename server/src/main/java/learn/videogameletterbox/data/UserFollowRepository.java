package learn.videogameletterbox.data;

import learn.videogameletterbox.models.UserFollow;
import learn.videogameletterbox.models.UserFollowId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFollowRepository extends JpaRepository<UserFollow, UserFollowId> {

    List<UserFollow> findByFollower_UserIdOrderByCreatedAtDesc(Long followerId);

    boolean existsByFollower_UserIdAndFollowed_UserId(Long followerId, Long followedId);
}

