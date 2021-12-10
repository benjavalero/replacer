package es.bvalero.replacer.authentication;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private WikipediaService wikipediaService;

    @Test
    void testGetRequestToken() throws Exception {
        RequestToken requestToken = RequestToken.of("X", "Y");
        String authorizationUrl = "Z";
        when(oAuthService.getRequestToken()).thenReturn(requestToken);
        when(oAuthService.getAuthorizationUrl(requestToken)).thenReturn(authorizationUrl);

        mvc
            .perform(get("/api/authentication/request-token").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is(requestToken.getToken())))
            .andExpect(jsonPath("$.tokenSecret", is(requestToken.getTokenSecret())))
            .andExpect(jsonPath("$.authorizationUrl", is(authorizationUrl)));

        verify(oAuthService).getRequestToken();
        verify(oAuthService).getAuthorizationUrl(any(RequestToken.class));
    }

    @Test
    void testGetRequestTokenWithException() throws Exception {
        when(oAuthService.getRequestToken()).thenThrow(new ReplacerException());

        mvc
            .perform(get("/api/authentication/request-token").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(oAuthService).getRequestToken();
        verify(oAuthService, never()).getAuthorizationUrl(any(RequestToken.class));
    }

    @Test
    void testAuthenticate() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        AccessToken accessToken = AccessToken.of("A", "B");
        String userName = "C";
        WikipediaUser wikipediaUser = WikipediaUser.of(userName, List.of(WikipediaUserGroup.AUTOCONFIRMED), true);
        String oAuthVerifier = "V";

        when(oAuthService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);
        when(wikipediaService.getAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken))
            .thenReturn(wikipediaUser);

        AuthenticateRequest authenticateRequest = AuthenticateRequest.of(
            WikipediaLanguage.getDefault(),
            requestToken,
            oAuthVerifier
        );
        mvc
            .perform(
                post("/api/authentication/authenticate?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticateRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(wikipediaUser.getName())))
            .andExpect(jsonPath("$.hasRights", equalTo(true)))
            .andExpect(jsonPath("$.bot", equalTo(false)))
            .andExpect(jsonPath("$.admin", equalTo(true)))
            .andExpect(jsonPath("$.token", is(accessToken.getToken())))
            .andExpect(jsonPath("$.tokenSecret", is(accessToken.getTokenSecret())));

        verify(oAuthService).getAccessToken(any(RequestToken.class), anyString());
        verify(wikipediaService).getAuthenticatedUser(any(WikipediaLanguage.class), any(AccessToken.class));
    }

    @Test
    void testAuthenticateEmptyVerifier() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "";
        AuthenticateRequest authenticateRequest = AuthenticateRequest.of(
            WikipediaLanguage.getDefault(),
            requestToken,
            oAuthVerifier
        );
        mvc
            .perform(
                post("/api/authentication/authenticate?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticateRequest))
            )
            .andExpect(status().isBadRequest());

        verify(oAuthService, never()).getAccessToken(any(RequestToken.class), anyString());
    }
}
