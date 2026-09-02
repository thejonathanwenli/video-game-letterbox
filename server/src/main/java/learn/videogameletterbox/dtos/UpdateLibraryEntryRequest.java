package learn.videogameletterbox.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import learn.videogameletterbox.models.GameStatus;

public class UpdateLibraryEntryRequest {

    @NotNull(message = "Status is required.")
    private GameStatus status;

    @Min(value = 1, message = "Personal rating must be between 1 and 10.")
    @Max(value = 10, message = "Personal rating must be between 1 and 10.")
    private Byte personalRating;

    private Boolean recommended;

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Byte getPersonalRating() {
        return personalRating;
    }

    public void setPersonalRating(Byte personalRating) {
        this.personalRating = personalRating;
    }

    public Boolean getRecommended() {
        return recommended;
    }

    public void setRecommended(Boolean recommended) {
        this.recommended = recommended;
    }
}
