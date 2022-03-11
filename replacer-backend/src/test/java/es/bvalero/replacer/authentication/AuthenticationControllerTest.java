package es.bvalero.replacer.authentication;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.authentication.dto.RequestTokenDto;
import es.bvalero.replacer.authentication.dto.VerifyAuthenticationRequest;
import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.ReplacerUser;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.UserService;
import java.util.Optional;
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
    private UserService userService;

    @Test
    void testInitiateAuthentication() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        String authorizationUrl = "Z";
        when(oAuthService.getRequestToken()).thenReturn(requestToken);
        when(oAuthService.getAuthorizationUrl(requestToken)).thenReturn(authorizationUrl);

        mvc
            .perform(get("/api/authentication/initiate").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestToken.token", is(requestToken.getToken())))
            .andExpect(jsonPath("$.requestToken.tokenSecret", is(requestToken.getTokenSecret())))
            .andExpect(jsonPath("$.authorizationUrl", is(authorizationUrl)));

        verify(oAuthService).getRequestToken();
        verify(oAuthService).getAuthorizationUrl(requestToken);
    }

    @Test
    void testInitiateAuthenticationWithException() throws Exception {
        when(oAuthService.getRequestToken()).thenThrow(new AuthenticationException());

        mvc
            .perform(get("/api/authentication/initiate").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(oAuthService).getRequestToken();
    }

    @Test
    void testVerifyAuthentication() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        AccessToken accessToken = AccessToken.of("A", "B");
        String oAuthVerifier = "V";

        WikipediaLanguage lang = WikipediaLanguage.SPANISH;
        ReplacerUser user = ReplacerUser.builder().lang(lang).name("C").hasRights(true).bot(false).admin(true).build();
        when(oAuthService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);
        when(userService.findUser(lang, accessToken)).thenReturn(Optional.of(user));

        VerifyAuthenticationRequest verifyAuthenticationRequest = new VerifyAuthenticationRequest();
        verifyAuthenticationRequest.setRequestToken(RequestTokenDto.fromDomain(requestToken));
        verifyAuthenticationRequest.setOauthVerifier(oAuthVerifier);

        mvc
            .perform(
                post("/api/authentication/verify?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyAuthenticationRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(user.getName())))
            .andExpect(jsonPath("$.hasRights", equalTo(user.hasRights())))
            .andExpect(jsonPath("$.bot", equalTo(user.isBot())))
            .andExpect(jsonPath("$.admin", equalTo(user.isAdmin())))
            .andExpect(jsonPath("$.accessToken.token", is(accessToken.getToken())))
            .andExpect(jsonPath("$.accessToken.tokenSecret", is(accessToken.getTokenSecret())));

        verify(oAuthService).getAccessToken(requestToken, oAuthVerifier);
        verify(userService).findUser(lang, accessToken);
    }

    @Test
    void testVerifyAuthenticationWithEmptyVerifier() throws Exception {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "";

        VerifyAuthenticationRequest verifyAuthenticationRequest = new VerifyAuthenticationRequest();
        verifyAuthenticationRequest.setRequestToken(RequestTokenDto.fromDomain(requestToken));
        verifyAuthenticationRequest.setOauthVerifier(oAuthVerifier);
        mvc
            .perform(
                post("/api/authentication/verify?lang=es")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(verifyAuthenticationRequest))
            )
            .andExpect(status().isBadRequest());

        verify(oAuthService, never()).getAccessToken(requestToken, oAuthVerifier);
    }
}
