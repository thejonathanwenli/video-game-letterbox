package learn.videogameletterbox.dtos;

import learn.videogameletterbox.models.AppUser;

import java.util.List;

public class UserResponse {

    private final Long userId;
    private final String username;
    private final String email;
    private final List<String> roles;

    public UserResponse(Long userId, String username, String email, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public static UserResponse fromAppUser(AppUser appUser) {
        List<String> roles = appUser.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();

        return new UserResponse(
                appUser.getUserId(),
                appUser.getDisplayUsername(),
                appUser.getEmail(),
                roles);
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
