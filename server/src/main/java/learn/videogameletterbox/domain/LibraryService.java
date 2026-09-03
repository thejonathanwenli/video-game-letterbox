package learn.videogameletterbox.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.AddLibraryEntryRequest;
import learn.videogameletterbox.dtos.LibraryEntryResponse;
import learn.videogameletterbox.dtos.UpdateLibraryEntryRequest;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.GameStatus;
import learn.videogameletterbox.models.UserGame;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class LibraryService {

    private final UserGameRepository userGameRepository;
    private final AppUserRepository appUserRepository;
    private final GameService gameService;
    private final Validator validator;

    public LibraryService(UserGameRepository userGameRepository,
                          AppUserRepository appUserRepository,
                          GameService gameService,
                          Validator validator) {
        this.userGameRepository = userGameRepository;
        this.appUserRepository = appUserRepository;
        this.gameService = gameService;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<LibraryEntryResponse> findAll(Long userId, GameStatus status, String genre) {
        String normalizedGenre = genre == null ? null : genre.trim().toLowerCase(Locale.ROOT);

        return userGameRepository.findByUser_UserIdOrderByAddedAtDesc(userId).stream()
                .filter(entry -> status == null || entry.getStatus() == status)
                .filter(entry -> normalizedGenre == null || normalizedGenre.isBlank()
                        || entry.getGame().getGenres().stream()
                        .anyMatch(item -> item.getName().toLowerCase(Locale.ROOT).equals(normalizedGenre)))
                .map(LibraryEntryResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LibraryEntryResponse> findById(Long userId, Long userGameId) {
        return userGameRepository.findByUserGameIdAndUser_UserId(userGameId, userId)
                .map(LibraryEntryResponse::new);
    }

    @Transactional
    public Result<LibraryEntryResponse> add(Long userId, AddLibraryEntryRequest request) {
        Result<LibraryEntryResponse> result = validate(request);
        if (!result.isSuccess()) {
            return result;
        }

        if (userGameRepository.existsByUser_UserIdAndGame_IgdbId(userId, request.getIgdbId())) {
            result.addErrorMessage("Game is already in your library.");
            return result;
        }

        AppUser user = appUserRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            result.addNotFoundMessage("User does not exist.");
            return result;
        }

        Game game;
        try {
            game = gameService.findOrImport(request.getIgdbId());
        } catch (IllegalArgumentException ex) {
            result.addNotFoundMessage("Game does not exist.");
            return result;
        }

        UserGame userGame = new UserGame();
        userGame.setUser(user);
        userGame.setGame(game);
        userGame.setStatus(request.getStatus());
        userGame.setPersonalRating(request.getPersonalRating());
        userGame.setRecommended(request.getRecommended());

        result.setPayload(new LibraryEntryResponse(userGameRepository.save(userGame)));
        return result;
    }

    @Transactional
    public Result<LibraryEntryResponse> update(Long userId,
                                               Long userGameId,
                                               UpdateLibraryEntryRequest request) {
        Result<LibraryEntryResponse> result = validate(request);
        if (!result.isSuccess()) {
            return result;
        }

        UserGame userGame = userGameRepository
                .findByUserGameIdAndUser_UserId(userGameId, userId)
                .orElse(null);
        if (userGame == null) {
            result.addNotFoundMessage("Library entry does not exist.");
            return result;
        }

        if (request.getStatus() == GameStatus.WANT_TO_PLAY
                && userGame.getReview() != null) {
            result.addErrorMessage("Remove your review before changing status to Want to Play.");
            return result;
        }

        userGame.setStatus(request.getStatus());
        userGame.setPersonalRating(request.getPersonalRating());
        userGame.setRecommended(request.getRecommended());
        result.setPayload(new LibraryEntryResponse(userGameRepository.save(userGame)));
        return result;
    }

    @Transactional
    public boolean deleteById(Long userId, Long userGameId) {
        Optional<UserGame> userGame = userGameRepository
                .findByUserGameIdAndUser_UserId(userGameId, userId);
        if (userGame.isEmpty()) {
            return false;
        }
        userGameRepository.delete(userGame.get());
        return true;
    }

    private Result<LibraryEntryResponse> validate(AddLibraryEntryRequest request) {
        Result<LibraryEntryResponse> result = validateRequest(request, "Library entry is required.");
        if (request != null) {
            validateStatusFields(request.getStatus(), request.getPersonalRating(), request.getRecommended(), result);
        }
        return result;
    }

    private Result<LibraryEntryResponse> validate(UpdateLibraryEntryRequest request) {
        Result<LibraryEntryResponse> result = validateRequest(request, "Library entry is required.");
        if (request != null) {
            validateStatusFields(request.getStatus(), request.getPersonalRating(), request.getRecommended(), result);
        }
        return result;
    }

    private <T> Result<LibraryEntryResponse> validateRequest(T request, String nullMessage) {
        Result<LibraryEntryResponse> result = new Result<>();
        if (request == null) {
            result.addErrorMessage(nullMessage);
            return result;
        }

        Set<ConstraintViolation<T>> violations = validator.validate(request);
        for (ConstraintViolation<T> violation : violations) {
            result.addErrorMessage(violation.getMessage());
        }
        return result;
    }

    private void validateStatusFields(GameStatus status,
                                      Byte personalRating,
                                      Boolean recommended,
                                      Result<LibraryEntryResponse> result) {
        if (status == GameStatus.WANT_TO_PLAY
                && (personalRating != null || recommended != null)) {
            result.addErrorMessage("Want to Play games cannot have a rating or recommendation.");
        }
    }
}
