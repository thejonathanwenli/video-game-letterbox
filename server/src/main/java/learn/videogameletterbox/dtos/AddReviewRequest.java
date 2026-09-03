package learn.videogameletterbox.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AddReviewRequest {

    @NotNull(message = "Library entry is required.")
    private Long userGameId;

    @NotBlank(message = "Review is required.")
    @Size(max = 5000, message = "Review cannot exceed 5000 characters.")
    private String body;

    public Long getUserGameId() {
        return userGameId;
    }

    public void setUserGameId(Long userGameId) {
        this.userGameId = userGameId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
