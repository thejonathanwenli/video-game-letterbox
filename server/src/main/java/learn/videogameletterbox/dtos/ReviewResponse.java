package learn.videogameletterbox.dtos;

import learn.videogameletterbox.models.Review;

import java.time.LocalDateTime;

public class ReviewResponse {

    private final Long reviewId;
    private final Long userId;
    private final Long userGameId;
    private final String username;
    private final Integer igdbId;
    private final String gameTitle;
    private final String body;
    private final Byte personalRating;
    private final Boolean recommended;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ReviewResponse(Review review) {
        this.reviewId = review.getReviewId();
        this.userId = review.getUserGame().getUser().getUserId();
        this.userGameId = review.getUserGame().getUserGameId();
        this.username = review.getUserGame().getUser().getDisplayUsername();
        this.igdbId = review.getUserGame().getGame().getIgdbId();
        this.gameTitle = review.getUserGame().getGame().getTitle();
        this.body = review.getBody();
        this.personalRating = review.getUserGame().getPersonalRating();
        this.recommended = review.getUserGame().getRecommended();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getUserGameId() {
        return userGameId;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() {
        return body;
    }

    public Integer getIgdbId() {
        return igdbId;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public Byte getPersonalRating() {
        return personalRating;
    }

    public Boolean getRecommended() {
        return recommended;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
