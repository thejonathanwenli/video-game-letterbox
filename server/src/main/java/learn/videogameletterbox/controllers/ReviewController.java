package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.Result;
import learn.videogameletterbox.domain.ResultType;
import learn.videogameletterbox.domain.ReviewService;
import learn.videogameletterbox.dtos.AddReviewRequest;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.dtos.UpdateReviewRequest;
import learn.videogameletterbox.models.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> add(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody AddReviewRequest request) {
        Result<ReviewResponse> result = reviewService.add(appUser.getUserId(), request);
        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), getErrorStatus(result));
        }
        return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> update(@AuthenticationPrincipal AppUser appUser,
                                    @PathVariable Long reviewId,
                                    @RequestBody UpdateReviewRequest request) {
        Result<ReviewResponse> result = reviewService.update(
                appUser.getUserId(), reviewId, request);
        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), getErrorStatus(result));
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteById(@AuthenticationPrincipal AppUser appUser,
                                           @PathVariable Long reviewId) {
        if (reviewService.deleteById(appUser.getUserId(), reviewId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private HttpStatus getErrorStatus(Result<?> result) {
        return result.getType() == ResultType.NOT_FOUND
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
    }
}
