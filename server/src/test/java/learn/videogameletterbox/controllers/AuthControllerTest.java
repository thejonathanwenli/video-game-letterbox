package learn.videogameletterbox.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.videogameletterbox.data.AppRoleRepository;
import learn.videogameletterbox.data.AppUserRepository;
import learn.videogameletterbox.dtos.LoginRequest;
import learn.videogameletterbox.dtos.RegisterRequest;
import learn.videogameletterbox.models.AppRole;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.security.JwtConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtConverter jwtConverter;

    @MockitoBean
    AppUserRepository appUserRepository;

    @MockitoBean
    AppRoleRepository appRoleRepository;

    AppUser appUser;

    @BeforeEach
    void setUp() {
        AppRole role = new AppRole("USER");
        appUser = new AppUser();
        appUser.setUserId(1L);
        appUser.setDisplayUsername("playerOne");
        appUser.setEmail("player@example.com");
        appUser.setPasswordHash(passwordEncoder.encode("P@ssword1"));
        appUser.setEnabled(true);
        appUser.getRoles().add(role);
    }

    @Test
    void shouldRegisterAndReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "playerOne",
                "player@example.com",
                "P@ssword1");

        when(appRoleRepository.findByName("USER"))
                .thenReturn(Optional.of(new AppRole("USER")));
        when(appUserRepository.save(any(AppUser.class)))
                .thenReturn(appUser);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("playerOne"))
                .andExpect(jsonPath("$.email").value("player@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldRejectAnInvalidRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest("", "bad-email", "password");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginAndReturnToken() throws Exception {
        when(appUserRepository.findByEmailIgnoreCase("player@example.com"))
                .thenReturn(Optional.of(appUser));

        LoginRequest request = new LoginRequest("player@example.com", "P@ssword1");

        MvcResult response = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("playerOne"))
                .andReturn();

        String token = objectMapper.readTree(response.getResponse().getContentAsString())
                .get("token").asText();
        var claims = objectMapper.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertEquals(3600, claims.get("exp").asLong() - claims.get("iat").asLong());
    }

    @Test
    void shouldRejectAnIncorrectPassword() throws Exception {
        when(appUserRepository.findByEmailIgnoreCase("player@example.com"))
                .thenReturn(Optional.of(appUser));

        LoginRequest request = new LoginRequest("player@example.com", "wrong-password");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnTheCurrentUserForAValidToken() throws Exception {
        String token = jwtConverter.getTokenFromUser(appUser);

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("playerOne"))
                .andExpect(jsonPath("$.email").value("player@example.com"));
    }

    @Test
    void shouldRejectCurrentUserWithoutAToken() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }
}
