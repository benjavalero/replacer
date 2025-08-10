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
import es.bvalero.replacer.auth.AuthorizationException;
import es.bvalero.replacer.auth.RequestToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { AuthorizationController.class, WebMvcConfiguration.class })
class AuthorizationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebUtils webUtils;

    @MockitoBean
    private AuthorizationApi authorizationApi;

    @Test
    void testInitiateAuthentication() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        RequestToken requestToken = RequestToken.of("R", "S", "Z");
        when(authorizationApi.getRequestToken()).thenReturn(requestToken);

        mvc
            .perform(get("/api/user/initiate-authorization").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is(requestToken.getToken())))
            .andExpect(jsonPath("$.tokenSecret", is(requestToken.getTokenSecret())))
            .andExpect(jsonPath("$.authorizationUrl", is(requestToken.getAuthorizationUrl())));

        verify(authorizationApi).getRequestToken();
    }

    @Test
    void testInitiateAuthorizationWithException() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        when(authorizationApi.getRequestToken()).thenThrow(new AuthorizationException());

        mvc
            .perform(get("/api/user/initiate-authorization").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(authorizationApi).getRequestToken();
    }

    @Test
    void testVerifyAuthorization() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S", "Z");
        String oAuthVerifier = "V";

        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);
        User user = User.buildTestAdminUser();
        when(authorizationApi.getAuthenticatedUser(lang, requestToken, oAuthVerifier)).thenReturn(user);

        VerifyAuthorizationRequest verifyAuthorizationRequest = new VerifyAuthorizationRequest();
        verifyAuthorizationRequest.setRequestToken(RequestTokenDto.of(requestToken));
        verifyAuthorizationRequest.setOauthVerifier(oAuthVerifier);

        mvc
            .perform(
                post("/api/user/verify-authorization")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyAuthorizationRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(user.getId().getUsername())))
            .andExpect(jsonPath("$.hasRights", equalTo(user.hasRights())))
            .andExpect(jsonPath("$.bot", equalTo(user.isBot())))
            .andExpect(jsonPath("$.admin", equalTo(user.isAdmin())));

        verify(authorizationApi).getAuthenticatedUser(lang, requestToken, oAuthVerifier);
    }

    @Test
    void testVerifyAuthorizationWithEmptyVerifier() throws Exception {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        when(webUtils.getLanguageHeader(any(HttpServletRequest.class))).thenReturn(lang);

        RequestToken requestToken = RequestToken.of("R", "S", "Z");
        String oAuthVerifier = "";

        VerifyAuthorizationRequest verifyAuthorizationRequest = new VerifyAuthorizationRequest();
        verifyAuthorizationRequest.setRequestToken(RequestTokenDto.of(requestToken));
        verifyAuthorizationRequest.setOauthVerifier(oAuthVerifier);
        mvc
            .perform(
                post("/api/user/verify-authorization")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, WikipediaLanguage.getDefault().getCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyAuthorizationRequest))
            )
            .andExpect(status().isBadRequest());

        verify(authorizationApi, never()).getAuthenticatedUser(lang, requestToken, oAuthVerifier);
    }
}
