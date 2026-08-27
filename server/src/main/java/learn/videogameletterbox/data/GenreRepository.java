package learn.videogameletterbox.data;

import learn.videogameletterbox.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByRawgId(Integer rawgId);

    Optional<Genre> findByNameIgnoreCase(String name);
}

