package learn.videogameletterbox.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GameSummaryResponse {

    private final Integer igdbId;
    private final String title;
    private final String imageUrl;
    private final LocalDate releaseDate;
    private final BigDecimal igdbRating;
    private final List<String> genres;
    private final Double communityRating;
    private final Integer recommendationPercentage;

    public GameSummaryResponse(Integer igdbId,
                               String title,
                               String imageUrl,
                               LocalDate releaseDate,
                               BigDecimal igdbRating,
                               List<String> genres,
                               Double communityRating,
                               Integer recommendationPercentage) {
        this.igdbId = igdbId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.releaseDate = releaseDate;
        this.igdbRating = igdbRating;
        this.genres = genres;
        this.communityRating = communityRating;
        this.recommendationPercentage = recommendationPercentage;
    }

    public Integer getIgdbId() {
        return igdbId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public BigDecimal getIgdbRating() {
        return igdbRating;
    }

    public List<String> getGenres() {
        return genres;
    }

    public Double getCommunityRating() {
        return communityRating;
    }

    public Integer getRecommendationPercentage() {
        return recommendationPercentage;
    }
}
