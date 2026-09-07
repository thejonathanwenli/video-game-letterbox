package learn.videogameletterbox.dtos;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 500, message = "Biography must be 500 characters or fewer.")
    private String bio;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
