package learn.videogameletterbox.data;

import learn.videogameletterbox.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByDisplayUsernameIgnoreCase(String displayUsername);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDisplayUsernameIgnoreCase(String displayUsername);
}

