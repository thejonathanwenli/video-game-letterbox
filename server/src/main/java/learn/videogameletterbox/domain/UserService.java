package learn.videogameletterbox.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserFollowRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.LibraryEntryResponse;
import learn.videogameletterbox.dtos.ProfileResponse;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.dtos.UpdateProfileRequest;
import learn.videogameletterbox.models.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final UserGameRepository userGameRepository;
    private final ReviewRepository reviewRepository;
    private final UserFollowRepository userFollowRepository;
    private final Validator validator;

    public UserService(AppUserRepository appUserRepository,
                       UserGameRepository userGameRepository,
                       ReviewRepository reviewRepository,
                       UserFollowRepository userFollowRepository,
                       Validator validator) {
        this.appUserRepository = appUserRepository;
        this.userGameRepository = userGameRepository;
        this.reviewRepository = reviewRepository;
        this.userFollowRepository = userFollowRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public Optional<ProfileResponse> findByUsername(String username, Long viewerId) {
        return appUserRepository.findByDisplayUsernameIgnoreCase(username)
                .filter(AppUser::isEnabled)
                .map(user -> new ProfileResponse(user,
                        viewerId != null && userFollowRepository
                                .existsByFollower_UserIdAndFollowed_UserId(viewerId, user.getUserId()),
                        userGameRepository.findByUser_UserIdOrderByAddedAtDesc(user.getUserId())
                                .stream().map(LibraryEntryResponse::new).toList(),
                        reviewRepository.findByUserGame_User_UserIdOrderByCreatedAtDesc(user.getUserId())
                                .stream().map(ReviewResponse::new).toList()));
    }

    @Transactional
    public Result<Void> updateBio(Long userId, UpdateProfileRequest request) {
        Result<Void> result = new Result<>();
        if (request == null) {
            result.addErrorMessage("Profile is required.");
            return result;
        }

        for (ConstraintViolation<UpdateProfileRequest> violation : validator.validate(request)) {
            result.addErrorMessage(violation.getMessage());
        }
        if (!result.isSuccess()) {
            return result;
        }

        AppUser user = appUserRepository.findById(userId).filter(AppUser::isEnabled).orElse(null);
        if (user == null) {
            result.addNotFoundMessage("User does not exist.");
            return result;
        }

        String bio = request.getBio();
        user.setBio(bio == null || bio.isBlank() ? null : bio.trim());
        appUserRepository.save(user);
        return result;
    }
}
