package es.bvalero.replacer.authentication;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.authentication.authenticateuser.AuthenticateUserService;
import es.bvalero.replacer.authentication.authenticateuser.AuthenticatedUser;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.authentication.requesttoken.GetRequestTokenResponse;
import es.bvalero.replacer.authentication.requesttoken.GetRequestTokenService;
import es.bvalero.replacer.authentication.userrights.CheckUserRightsService;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.common.exception.ForbiddenException;
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
    private GetRequestTokenService getRequestTokenService;

    @MockBean
    private AuthenticateUserService authenticateUserService;

    @MockBean
    private CheckUserRightsService checkUserRightsService;

    @Test
    void testGetRequestToken() throws Exception {
        GetRequestTokenResponse response = GetRequestTokenResponse.of("R", "S", "Z");
        when(getRequestTokenService.get()).thenReturn(response);

        mvc
            .perform(get("/api/authentication/request-token").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is(response.getToken())))
            .andExpect(jsonPath("$.tokenSecret", is(response.getTokenSecret())))
            .andExpect(jsonPath("$.authorizationUrl", is(response.getAuthorizationUrl())));

        verify(getRequestTokenService).get();
    }

    @Test
    void testGetRequestTokenWithException() throws Exception {
        when(getRequestTokenService.get()).thenThrow(new AuthenticationException());

        mvc
            .perform(get("/api/authentication/request-token").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(getRequestTokenService).get();
    }

    @Test
    void testAuthenticateUser() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        AccessToken accessToken = AccessToken.of("A", "B");
        String oAuthVerifier = "V";

        AuthenticatedUser authenticatedUser = AuthenticatedUser
            .builder()
            .name("C")
            .hasRights(true)
            .bot(false)
            .admin(true)
            .accessToken(AccessTokenDto.fromDomain(accessToken))
            .build();
        when(authenticateUserService.authenticateUser(WikipediaLanguage.getDefault(), requestToken, oAuthVerifier))
            .thenReturn(authenticatedUser);

        AuthenticateRequest authenticateRequest = new AuthenticateRequest();
        authenticateRequest.setToken(requestToken.getToken());
        authenticateRequest.setTokenSecret(requestToken.getTokenSecret());
        authenticateRequest.setOauthVerifier(oAuthVerifier);
        mvc
            .perform(
                post("/api/authentication/authenticate?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticateRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(authenticatedUser.getName())))
            .andExpect(jsonPath("$.hasRights", equalTo(authenticatedUser.getHasRights())))
            .andExpect(jsonPath("$.bot", equalTo(authenticatedUser.getBot())))
            .andExpect(jsonPath("$.admin", equalTo(authenticatedUser.getAdmin())))
            .andExpect(jsonPath("$.accessToken.token", is(accessToken.getToken())))
            .andExpect(jsonPath("$.accessToken.tokenSecret", is(accessToken.getTokenSecret())));

        verify(authenticateUserService).authenticateUser(WikipediaLanguage.getDefault(), requestToken, oAuthVerifier);
    }

    @Test
    void testAuthenticateUserEmptyVerifier() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "";

        AuthenticateRequest authenticateRequest = new AuthenticateRequest();
        authenticateRequest.setToken(requestToken.getToken());
        authenticateRequest.setTokenSecret(requestToken.getTokenSecret());
        authenticateRequest.setOauthVerifier(oAuthVerifier);
        mvc
            .perform(
                post("/api/authentication/authenticate?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticateRequest))
            )
            .andExpect(status().isBadRequest());

        verify(authenticateUserService, never())
            .authenticateUser(any(WikipediaLanguage.class), any(RequestToken.class), anyString());
    }

    @Test
    void testGetPublicIpNotAdmin() throws Exception {
        doThrow(ForbiddenException.class).when(checkUserRightsService).validateAdminUser(anyString());

        mvc
            .perform(get("/api/authentication/public-ip?user=x&lang=es").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(checkUserRightsService).validateAdminUser(anyString());
    }
}
