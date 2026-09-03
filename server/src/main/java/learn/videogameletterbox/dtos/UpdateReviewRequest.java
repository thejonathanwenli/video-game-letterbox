package learn.videogameletterbox.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateReviewRequest {

    @NotBlank(message = "Review is required.")
    @Size(max = 5000, message = "Review cannot exceed 5000 characters.")
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
