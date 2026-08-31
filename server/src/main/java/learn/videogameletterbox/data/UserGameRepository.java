package learn.videogameletterbox.data;

import learn.videogameletterbox.models.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    List<UserGame> findByUser_UserIdOrderByAddedAtDesc(Long userId);

    List<UserGame> findByGame_GameId(Long gameId);

    Optional<UserGame> findByUserGameIdAndUser_UserId(Long userGameId, Long userId);

    boolean existsByUser_UserIdAndGame_GameId(Long userId, Long gameId);
}

