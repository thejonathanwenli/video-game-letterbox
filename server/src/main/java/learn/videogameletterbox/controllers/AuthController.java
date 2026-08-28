package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.AppUserService;
import learn.videogameletterbox.domain.Result;
import learn.videogameletterbox.dtos.AuthResponse;
import learn.videogameletterbox.dtos.LoginRequest;
import learn.videogameletterbox.dtos.RegisterRequest;
import learn.videogameletterbox.dtos.UserResponse;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.security.JwtConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtConverter jwtConverter;
    private final AppUserService appUserService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtConverter jwtConverter,
                          AppUserService appUserService) {
        this.authenticationManager = authenticationManager;
        this.jwtConverter = jwtConverter;
        this.appUserService = appUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        Result<AppUser> result = appUserService.register(request);

        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                UserResponse.fromAppUser(result.getPayload()),
                HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null) {
            return new ResponseEntity<>(
                    List.of("Email and password are required."),
                    HttpStatus.UNAUTHORIZED);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            AppUser appUser = (AppUser) authentication.getPrincipal();
            String token = jwtConverter.getTokenFromUser(appUser);

            return ResponseEntity.ok(
                    new AuthResponse(token, UserResponse.fromAppUser(appUser)));
        } catch (AuthenticationException ex) {
            return new ResponseEntity<>(
                    List.of("Email or password is incorrect."),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal AppUser appUser) {
        return UserResponse.fromAppUser(appUser);
    }
}
