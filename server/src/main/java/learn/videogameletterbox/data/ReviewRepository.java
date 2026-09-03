package learn.videogameletterbox.data;

import learn.videogameletterbox.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserGame_UserGameId(Long userGameId);

    Optional<Review> findByReviewIdAndUserGame_User_UserId(Long reviewId, Long userId);

    List<Review> findByUserGame_Game_GameIdOrderByCreatedAtDesc(Long gameId);

    List<Review> findByUserGame_User_UserIdOrderByCreatedAtDesc(Long userId);
}

