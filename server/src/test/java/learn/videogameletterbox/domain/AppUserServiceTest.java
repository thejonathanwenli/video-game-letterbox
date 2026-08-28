package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.AppRoleRepository;
import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.dtos.RegisterRequest;
import learn.videogameletterbox.models.AppRole;
import learn.videogameletterbox.models.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AppUserServiceTest {

    @Autowired
    AppUserService service;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    AppUserRepository appUserRepository;

    @MockitoBean
    AppRoleRepository appRoleRepository;

    @Test
    void shouldRegisterAValidUser() {
        RegisterRequest request = new RegisterRequest(
                "playerOne",
                "PLAYER@example.com",
                "P@ssword1");

        when(appRoleRepository.findByName("USER"))
                .thenReturn(Optional.of(new AppRole("USER")));
        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> {
                    AppUser appUser = invocation.getArgument(0);
                    appUser.setUserId(1L);
                    return appUser;
                });

        Result<AppUser> result = service.register(request);

        assertTrue(result.isSuccess());
        assertTrue(passwordEncoder.matches("P@ssword1", result.getPayload().getPasswordHash()));
        assertNotEquals("P@ssword1", result.getPayload().getPasswordHash());
        assertTrue(result.getPayload().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("USER")));
        assertTrue(result.getPayload().getEmail().equals("player@example.com"));
    }

    @Test
    void shouldRejectInvalidRegistrationInformation() {
        RegisterRequest request = new RegisterRequest("", "not-an-email", "password");

        Result<AppUser> result = service.register(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains("Username is required."));
        assertTrue(result.getMessages().contains("Email must be valid."));
        assertTrue(result.getMessages().contains(
                "Password must be at least 8 characters and contain a letter, a number, and a special character."));
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateUsernameAndEmail() {
        RegisterRequest request = new RegisterRequest(
                "playerOne",
                "player@example.com",
                "P@ssword1");

        when(appUserRepository.existsByDisplayUsernameIgnoreCase("playerOne"))
                .thenReturn(true);
        when(appUserRepository.existsByEmailIgnoreCase("player@example.com"))
                .thenReturn(true);

        Result<AppUser> result = service.register(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessages().contains("Username is already taken."));
        assertTrue(result.getMessages().contains("Email is already registered."));
        verify(appUserRepository, never()).save(any());
    }
}
