package learn.videogameletterbox.dtos;

import learn.videogameletterbox.models.GameStatus;
import learn.videogameletterbox.models.Genre;
import learn.videogameletterbox.models.UserGame;

import java.time.LocalDateTime;
import java.util.List;

public class LibraryEntryResponse {

    private final Long userGameId;
    private final Integer igdbId;
    private final String title;
    private final String imageUrl;
    private final List<String> genres;
    private final GameStatus status;
    private final Byte personalRating;
    private final Boolean recommended;
    private final LocalDateTime addedAt;
    private final LocalDateTime updatedAt;

    public LibraryEntryResponse(UserGame userGame) {
        this.userGameId = userGame.getUserGameId();
        this.igdbId = userGame.getGame().getIgdbId();
        this.title = userGame.getGame().getTitle();
        this.imageUrl = userGame.getGame().getImageUrl();
        this.genres = userGame.getGame().getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .toList();
        this.status = userGame.getStatus();
        this.personalRating = userGame.getPersonalRating();
        this.recommended = userGame.getRecommended();
        this.addedAt = userGame.getAddedAt();
        this.updatedAt = userGame.getUpdatedAt();
    }

    public Long getUserGameId() {
        return userGameId;
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

    public List<String> getGenres() {
        return genres;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Byte getPersonalRating() {
        return personalRating;
    }

    public Boolean getRecommended() {
        return recommended;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
