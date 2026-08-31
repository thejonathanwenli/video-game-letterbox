package learn.videogameletterbox.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GameSummaryResponse {

    private final Integer rawgId;
    private final String title;
    private final String imageUrl;
    private final LocalDate releaseDate;
    private final BigDecimal rawgRating;
    private final List<String> genres;
    private final Double communityRating;
    private final Integer recommendationPercentage;

    public GameSummaryResponse(Integer rawgId,
                               String title,
                               String imageUrl,
                               LocalDate releaseDate,
                               BigDecimal rawgRating,
                               List<String> genres,
                               Double communityRating,
                               Integer recommendationPercentage) {
        this.rawgId = rawgId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.releaseDate = releaseDate;
        this.rawgRating = rawgRating;
        this.genres = genres;
        this.communityRating = communityRating;
        this.recommendationPercentage = recommendationPercentage;
    }

    public Integer getRawgId() {
        return rawgId;
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

    public BigDecimal getRawgRating() {
        return rawgRating;
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
