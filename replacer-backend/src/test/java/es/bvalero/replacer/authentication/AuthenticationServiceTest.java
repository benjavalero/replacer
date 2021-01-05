package es.bvalero.replacer.authentication;

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
        OAuth1RequestToken token = Mockito.mock(OAuth1RequestToken.class);
        Mockito.when(oAuthService.getRequestToken()).thenReturn(token);

        Assertions.assertEquals(token, authenticationService.getRequestToken());
        Mockito.verify(oAuthService).getRequestToken();
    }

    @Test
    void testGetRequestTokenWithException() throws Exception {
        Mockito.when(oAuthService.getRequestToken()).thenThrow(new IOException());

        Assertions.assertThrows(AuthenticationException.class, () -> authenticationService.getRequestToken());
    }

    @Test
    void testGetAuthorizationUrl() {
        OAuth1RequestToken requestToken = Mockito.mock(OAuth1RequestToken.class);
        Mockito.when(oAuthService.getAuthorizationUrl(requestToken)).thenReturn("X");

        Assertions.assertEquals("X", authenticationService.getAuthorizationUrl(requestToken));
        Mockito.verify(oAuthService).getAuthorizationUrl(requestToken);
    }

    @Test
    void testGetAccessToken() throws Exception {
        OAuth1RequestToken requestToken = Mockito.mock(OAuth1RequestToken.class);
        OAuth1AccessToken accessToken = Mockito.mock(OAuth1AccessToken.class);
        Mockito.when(oAuthService.getAccessToken(requestToken, "")).thenReturn(accessToken);

        Assertions.assertEquals(accessToken, authenticationService.getAccessToken(requestToken, ""));
        Mockito.verify(oAuthService).getAccessToken(requestToken, "");
    }

    @Test
    void testGetAccessTokenWithException() throws Exception {
        OAuth1RequestToken requestToken = Mockito.mock(OAuth1RequestToken.class);
        Mockito.when(oAuthService.getAccessToken(requestToken, "")).thenThrow(new IOException());

        Assertions.assertThrows(
            AuthenticationException.class,
            () -> authenticationService.getAccessToken(requestToken, "")
        );
    }

    @Test
    void testAuthenticationServiceOffline() throws AuthenticationException {
        Assertions.assertNotNull(authenticationServiceOffline.getRequestToken());
        Assertions.assertNotNull(
            authenticationServiceOffline.getAuthorizationUrl(Mockito.mock(OAuth1RequestToken.class))
        );
        Assertions.assertNotNull(
            authenticationServiceOffline.getAccessToken(Mockito.mock(OAuth1RequestToken.class), "")
        );
    }
}
