package learn.videogameletterbox.dtos;

import learn.videogameletterbox.models.AppUser;

import java.time.LocalDateTime;
import java.util.List;

public class ProfileResponse {

    private final Long userId;
    private final String username;
    private final String bio;
    private final LocalDateTime createdAt;
    private final boolean following;
    private final List<LibraryEntryResponse> library;
    private final List<ReviewResponse> reviews;

    public ProfileResponse(AppUser user, boolean following,
                           List<LibraryEntryResponse> library, List<ReviewResponse> reviews) {
        this.userId = user.getUserId();
        this.username = user.getDisplayUsername();
        this.bio = user.getBio();
        this.createdAt = user.getCreatedAt();
        this.following = following;
        this.library = library;
        this.reviews = reviews;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isFollowing() {
        return following;
    }

    public List<LibraryEntryResponse> getLibrary() {
        return library;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }
}
