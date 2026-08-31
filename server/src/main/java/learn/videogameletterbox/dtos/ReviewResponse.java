package learn.videogameletterbox.dtos;

import java.time.LocalDateTime;

public class ReviewResponse {

    private final Long reviewId;
    private final String username;
    private final String body;
    private final Byte personalRating;
    private final Boolean recommended;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ReviewResponse(Long reviewId,
                          String username,
                          String body,
                          Byte personalRating,
                          Boolean recommended,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.username = username;
        this.body = body;
        this.personalRating = personalRating;
        this.recommended = recommended;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() {
        return body;
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
