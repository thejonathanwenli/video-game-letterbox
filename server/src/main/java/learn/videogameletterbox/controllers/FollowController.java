package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.FollowService;
import learn.videogameletterbox.domain.Result;
import learn.videogameletterbox.domain.ResultType;
import learn.videogameletterbox.dtos.FollowingResponse;
import learn.videogameletterbox.models.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PutMapping("/follows/{userId}")
    public ResponseEntity<?> follow(@AuthenticationPrincipal AppUser user, @PathVariable Long userId) {
        Result<Void> result = followService.follow(user.getUserId(), userId);
        if (!result.isSuccess()) {
            HttpStatus status = result.getType() == ResultType.NOT_FOUND
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result.getMessages(), status);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/follows/{userId}")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal AppUser user, @PathVariable Long userId) {
        followService.unfollow(user.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/following")
    public FollowingResponse findFollowing(@AuthenticationPrincipal AppUser user) {
        return followService.findFollowing(user.getUserId());
    }
}
