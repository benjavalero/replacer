package es.bvalero.replacer.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.WebMvcConfiguration;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { AuthenticationController.class, WebMvcConfiguration.class })
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    @Test
    void testInitiateAuthentication() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        String authorizationUrl = "Z";
        when(authenticationService.getRequestToken()).thenReturn(requestToken);
        when(authenticationService.getAuthorizationUrl(requestToken)).thenReturn(authorizationUrl);

        mvc
            .perform(get("/api/authentication/initiate").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestToken.token", is(requestToken.getToken())))
            .andExpect(jsonPath("$.requestToken.tokenSecret", is(requestToken.getTokenSecret())))
            .andExpect(jsonPath("$.authorizationUrl", is(authorizationUrl)));

        verify(authenticationService).getRequestToken();
        verify(authenticationService).getAuthorizationUrl(requestToken);
    }

    @Test
    void testInitiateAuthenticationWithException() throws Exception {
        when(authenticationService.getRequestToken()).thenThrow(new AuthenticationException());

        mvc
            .perform(get("/api/authentication/initiate").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(authenticationService).getRequestToken();
    }

    @Test
    void testVerifyAuthentication() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        AccessToken accessToken = AccessToken.of("A", "B");
        String oAuthVerifier = "V";

        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        User user = User
            .builder()
            .id(UserId.of(lang, "C"))
            .accessToken(accessToken)
            .hasRights(true)
            .bot(false)
            .admin(true)
            .build();
        when(authenticationService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);
        when(userService.findAuthenticatedUser(lang, accessToken)).thenReturn(Optional.of(user));

        VerifyAuthenticationRequest verifyAuthenticationRequest = new VerifyAuthenticationRequest();
        verifyAuthenticationRequest.setRequestToken(RequestTokenDto.of(requestToken));
        verifyAuthenticationRequest.setOauthVerifier(oAuthVerifier);

        mvc
            .perform(
                post("/api/authentication/verify")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyAuthenticationRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(user.getId().getUsername())))
            .andExpect(jsonPath("$.hasRights", equalTo(user.hasRights())))
            .andExpect(jsonPath("$.bot", equalTo(user.isBot())))
            .andExpect(jsonPath("$.admin", equalTo(user.isAdmin())));

        verify(authenticationService).getAccessToken(requestToken, oAuthVerifier);
        verify(userService).findAuthenticatedUser(lang, accessToken);
    }

    @Test
    void testVerifyAuthenticationWithEmptyVerifier() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "";

        VerifyAuthenticationRequest verifyAuthenticationRequest = new VerifyAuthenticationRequest();
        verifyAuthenticationRequest.setRequestToken(RequestTokenDto.of(requestToken));
        verifyAuthenticationRequest.setOauthVerifier(oAuthVerifier);
        mvc
            .perform(
                post("/api/authentication/verify")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyAuthenticationRequest))
            )
            .andExpect(status().isBadRequest());

        verify(authenticationService, never()).getAccessToken(requestToken, oAuthVerifier);
    }
}
