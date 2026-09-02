package learn.videogameletterbox.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GameDetailsResponse extends GameSummaryResponse {

    private final String description;
    private final List<ReviewResponse> reviews;

    public GameDetailsResponse(Integer igdbId,
                               String title,
                               String description,
                               String imageUrl,
                               LocalDate releaseDate,
                               BigDecimal igdbRating,
                               List<String> genres,
                               Double communityRating,
                               Integer recommendationPercentage,
                               List<ReviewResponse> reviews) {
        super(igdbId,
                title,
                imageUrl,
                releaseDate,
                igdbRating,
                genres,
                communityRating,
                recommendationPercentage);
        this.description = description;
        this.reviews = reviews;
    }

    public String getDescription() {
        return description;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }
}
