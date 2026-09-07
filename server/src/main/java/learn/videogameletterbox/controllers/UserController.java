package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.Result;
import learn.videogameletterbox.domain.ResultType;
import learn.videogameletterbox.domain.UserService;
import learn.videogameletterbox.dtos.ProfileResponse;
import learn.videogameletterbox.dtos.UpdateProfileRequest;
import learn.videogameletterbox.models.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> findByUsername(@PathVariable String username,
                                                         @AuthenticationPrincipal AppUser viewer) {
        return ResponseEntity.of(userService.findByUsername(
                username, viewer == null ? null : viewer.getUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateBio(@AuthenticationPrincipal AppUser user,
                                       @RequestBody UpdateProfileRequest request) {
        Result<Void> result = userService.updateBio(user.getUserId(), request);
        if (!result.isSuccess()) {
            HttpStatus status = result.getType() == ResultType.NOT_FOUND
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result.getMessages(), status);
        }
        return ResponseEntity.noContent().build();
    }
}
