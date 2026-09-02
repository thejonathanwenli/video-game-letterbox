package learn.videogameletterbox.data;

import learn.videogameletterbox.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByIgdbId(Integer igdbId);

    List<Game> findAllByOrderByImportedAtDesc();
}

