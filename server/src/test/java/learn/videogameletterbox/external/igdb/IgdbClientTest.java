package learn.videogameletterbox.external.igdb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(IgdbClient.class)
@TestPropertySource(properties = {
        "igdb.client-id=test-client-id",
        "igdb.client-secret=test-client-secret"
})
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IgdbClientTest {

    @Autowired
    IgdbClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void shouldLogHttpStatusWithoutCredentialsOrResponseBody(CapturedOutput output) {
        expectToken();
        server.expect(requestTo("https://api.igdb.com/v4/games"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("private-provider-response"));

        IgdbApiException error = assertThrows(IgdbApiException.class, () -> client.findById(1942));

        assertEquals("Unable to load that game from IGDB right now.", error.getMessage());
        assertTrue(output.getOut().contains("IGDB games request failed: HTTP 429"));
        assertFalse(output.getAll().contains("private-provider-response"));
        assertFalse(output.getAll().contains("test-access-token"));
        assertFalse(output.getAll().contains("test-client-secret"));
        server.verify();
    }

    @Test
    void shouldLogConnectionFailureTypeWithoutItsMessage(CapturedOutput output) {
        expectToken();
        server.expect(requestTo("https://api.igdb.com/v4/games"))
                .andRespond(withException(new SocketTimeoutException("private-connection-detail")));

        assertThrows(IgdbApiException.class, () -> client.findById(1942));

        assertTrue(output.getOut().contains("cause: SocketTimeoutException"));
        assertFalse(output.getAll().contains("private-connection-detail"));
        server.verify();
    }

    @Test
    void shouldLogAuthenticationFailureWithoutSecretInUrl(CapturedOutput output) {
        server.expect(requestTo(tokenUrl()))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("private-auth-response"));

        IgdbApiException error = assertThrows(IgdbApiException.class, () -> client.findById(1942));

        assertEquals("Unable to authenticate with IGDB right now.", error.getMessage());
        assertTrue(output.getOut().contains("Twitch token request failed: HTTP 401"));
        assertFalse(output.getAll().contains("private-auth-response"));
        assertFalse(output.getAll().contains("test-client-secret"));
        server.verify();
    }

    private void expectToken() {
        server.expect(requestTo(tokenUrl())).andRespond(withSuccess(
                "{\"access_token\":\"test-access-token\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
    }

    private String tokenUrl() {
        return "https://id.twitch.tv/oauth2/token?client_id=test-client-id"
                + "&client_secret=test-client-secret&grant_type=client_credentials";
    }
}
