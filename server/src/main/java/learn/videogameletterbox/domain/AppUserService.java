package learn.videogameletterbox.domain;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import learn.videogameletterbox.data.AppRoleRepository;
import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.dtos.RegisterRequest;
import learn.videogameletterbox.models.AppRole;
import learn.videogameletterbox.models.AppUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public AppUserService(AppUserRepository appUserRepository,
                          AppRoleRepository appRoleRepository,
                          PasswordEncoder passwordEncoder,
                          Validator validator) {
        this.appUserRepository = appUserRepository;
        this.appRoleRepository = appRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmailIgnoreCase(email)
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    @Transactional
    public Result<AppUser> register(RegisterRequest request) {
        Result<AppUser> result = validate(request);
        if (!result.isSuccess()) {
            return result;
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByDisplayUsernameIgnoreCase(username)) {
            result.addErrorMessage("Username is already taken.");
        }

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            result.addErrorMessage("Email is already registered.");
        }

        if (!result.isSuccess()) {
            return result;
        }

        AppRole userRole = appRoleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role is missing."));

        AppUser appUser = new AppUser();
        appUser.setDisplayUsername(username);
        appUser.setEmail(email);
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUser.setEnabled(true);
        appUser.getRoles().add(userRole);

        result.setPayload(appUserRepository.save(appUser));

        return result;
    }

    private Result<AppUser> validate(RegisterRequest request) {
        Result<AppUser> result = new Result<>();

        if (request == null) {
            result.addErrorMessage("Registration information is required.");
            return result;
        }

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        for (ConstraintViolation<RegisterRequest> violation : violations) {
            result.addErrorMessage(violation.getMessage());
        }

        String password = request.getPassword();
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > 72) {
            result.addErrorMessage(
                    "Password is too long. Use at most 72 bytes; some characters use more than one byte.");
        }
        if (password != null && !password.isBlank() && !isValidPassword(password)) {
            result.addErrorMessage(
                    "Password must be at least 8 characters and contain a letter, a number, and a special character.");
        }

        return result;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;

        for (char character : password.toCharArray()) {
            if (Character.isLetter(character)) {
                hasLetter = true;
            } else if (Character.isDigit(character)) {
                hasNumber = true;
            } else {
                hasSpecialCharacter = true;
            }
        }

        return hasLetter && hasNumber && hasSpecialCharacter;
    }
}
