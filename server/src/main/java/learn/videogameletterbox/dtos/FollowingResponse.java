package learn.videogameletterbox.dtos;

import java.util.List;

public class FollowingResponse {

    private final List<RecommendationResponse> recommendations;
    private final List<ReviewResponse> reviews;

    public FollowingResponse(List<RecommendationResponse> recommendations,
                             List<ReviewResponse> reviews) {
        this.recommendations = recommendations;
        this.reviews = reviews;
    }

    public List<RecommendationResponse> getRecommendations() {
        return recommendations;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }
}
