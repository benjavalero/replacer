package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class AuthenticationServiceTest {

    @Mock
    private OAuth10aService oAuthService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationService authenticationServiceOffline;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl();
        authenticationServiceOffline = new AuthenticationServiceOfflineImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetRequestToken() throws Exception {
        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("A", "B");
        String authorizationUrl = "C";
        Mockito.when(oAuthService.getRequestToken()).thenReturn(oAuth1RequestToken);
        Mockito.when(oAuthService.getAuthorizationUrl(oAuth1RequestToken)).thenReturn(authorizationUrl);

        RequestToken requestToken = RequestToken.of("A", "B", "C");
        Assertions.assertEquals(requestToken, authenticationService.getRequestToken());
        Mockito.verify(oAuthService).getRequestToken();
        Mockito.verify(oAuthService).getAuthorizationUrl(oAuth1RequestToken);
    }

    @Test
    void testGetRequestTokenWithException() throws Exception {
        Mockito.when(oAuthService.getRequestToken()).thenThrow(new IOException());

        Assertions.assertThrows(AuthenticationException.class, () -> authenticationService.getRequestToken());
    }

    @Test
    void testGetAccessToken() throws Exception {
        OAuth1RequestToken requestToken = new OAuth1RequestToken("X", "Y");
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken("A", "B");
        String oauthVerifier = "C";
        Mockito.when(oAuthService.getAccessToken(requestToken, oauthVerifier)).thenReturn(oAuth1AccessToken);

        AccessToken accessToken = AccessToken.of("A", "B");
        Assertions.assertEquals(accessToken, authenticationService.getAccessToken("X", "Y", oauthVerifier));
        Mockito.verify(oAuthService).getAccessToken(requestToken, oauthVerifier);
    }

    @Test
    void testGetAccessTokenWithException() throws Exception {
        Mockito
            .when(oAuthService.getAccessToken(Mockito.any(OAuth1RequestToken.class), Mockito.anyString()))
            .thenThrow(new IOException());

        Assertions.assertThrows(AuthenticationException.class, () -> authenticationService.getAccessToken("", "", ""));
    }

    @Test
    void testAuthenticationServiceOffline() throws AuthenticationException {
        Assertions.assertNotNull(authenticationServiceOffline.getRequestToken());
        Assertions.assertNotNull(authenticationServiceOffline.getAccessToken("", "", ""));
    }
}
