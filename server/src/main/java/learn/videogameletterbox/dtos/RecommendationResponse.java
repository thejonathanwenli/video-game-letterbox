package learn.videogameletterbox.dtos;

import learn.videogameletterbox.models.UserGame;

public class RecommendationResponse {

    private final String username;
    private final LibraryEntryResponse entry;

    public RecommendationResponse(UserGame userGame) {
        this.username = userGame.getUser().getDisplayUsername();
        this.entry = new LibraryEntryResponse(userGame);
    }

    public String getUsername() {
        return username;
    }

    public LibraryEntryResponse getEntry() {
        return entry;
    }
}
