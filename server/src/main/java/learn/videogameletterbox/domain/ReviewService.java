package learn.videogameletterbox.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.AddReviewRequest;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.dtos.UpdateReviewRequest;
import learn.videogameletterbox.models.GameStatus;
import learn.videogameletterbox.models.Review;
import learn.videogameletterbox.models.UserGame;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserGameRepository userGameRepository;
    private final Validator validator;

    public ReviewService(ReviewRepository reviewRepository,
                         UserGameRepository userGameRepository,
                         Validator validator) {
        this.reviewRepository = reviewRepository;
        this.userGameRepository = userGameRepository;
        this.validator = validator;
    }

    @Transactional
    public Result<ReviewResponse> add(Long userId, AddReviewRequest request) {
        Result<ReviewResponse> result = validate(request);
        if (!result.isSuccess()) {
            return result;
        }

        UserGame userGame = userGameRepository
                .findByUserGameIdAndUser_UserId(request.getUserGameId(), userId)
                .orElse(null);
        if (userGame == null) {
            result.addNotFoundMessage("Library entry does not exist.");
            return result;
        }

        if (userGame.getStatus() == GameStatus.WANT_TO_PLAY) {
            result.addErrorMessage("Change the game status before writing a review.");
            return result;
        }

        if (reviewRepository.findByUserGame_UserGameId(request.getUserGameId()).isPresent()) {
            result.addErrorMessage("Game already has a review.");
            return result;
        }

        Review review = new Review();
        review.setUserGame(userGame);
        review.setBody(request.getBody().trim());
        result.setPayload(new ReviewResponse(reviewRepository.save(review)));
        return result;
    }

    @Transactional
    public Result<ReviewResponse> update(Long userId,
                                         Long reviewId,
                                         UpdateReviewRequest request) {
        Result<ReviewResponse> result = validate(request);
        if (!result.isSuccess()) {
            return result;
        }

        Review review = reviewRepository
                .findByReviewIdAndUserGame_User_UserId(reviewId, userId)
                .orElse(null);
        if (review == null) {
            result.addNotFoundMessage("Review does not exist.");
            return result;
        }

        review.setBody(request.getBody().trim());
        result.setPayload(new ReviewResponse(reviewRepository.save(review)));
        return result;
    }

    @Transactional
    public boolean deleteById(Long userId, Long reviewId) {
        Optional<Review> review = reviewRepository
                .findByReviewIdAndUserGame_User_UserId(reviewId, userId);
        if (review.isEmpty()) {
            return false;
        }
        reviewRepository.delete(review.get());
        return true;
    }

    private <T> Result<ReviewResponse> validate(T request) {
        Result<ReviewResponse> result = new Result<>();
        if (request == null) {
            result.addErrorMessage("Review is required.");
            return result;
        }

        Set<ConstraintViolation<T>> violations = validator.validate(request);
        for (ConstraintViolation<T> violation : violations) {
            result.addErrorMessage(violation.getMessage());
        }
        return result;
    }
}
