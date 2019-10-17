package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AuthenticationServiceTest {

    @Mock
    private OAuth10aService oAuthService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationService authenticationServiceOffline;

    @Before
    public void setUp() {
        authenticationService = new AuthenticationServiceImpl();
        authenticationServiceOffline = new AuthenticationServiceOfflineImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetRequestToken()
            throws InterruptedException, ExecutionException, IOException, AuthenticationException {
        OAuth1RequestToken token = Mockito.mock(OAuth1RequestToken.class);
        Mockito.when(oAuthService.getRequestToken()).thenReturn(token);

        Assert.assertEquals(token, authenticationService.getRequestToken());
        Mockito.verify(oAuthService).getRequestToken();
    }

    @Test(expected = AuthenticationException.class)
    public void testGetRequestTokenWithException()
            throws InterruptedException, ExecutionException, IOException, AuthenticationException {
        Mockito.when(oAuthService.getRequestToken()).thenThrow(new IOException());

        authenticationService.getRequestToken();
    }

    @Test(expected = AuthenticationException.class)
    public void testGetRequestTokenWithInterruption()
            throws InterruptedException, ExecutionException, IOException, AuthenticationException {
        Mockito.when(oAuthService.getRequestToken()).thenThrow(new InterruptedException());

        authenticationService.getRequestToken();
    }

    @Test
    public void testGetAuthorizationUrl() {
        OAuth1RequestToken requestToken = Mockito.mock(OAuth1RequestToken.class);
        Mockito.when(oAuthService.getAuthorizationUrl(requestToken)).thenReturn("X");

        Assert.assertEquals("X", authenticationService.getAuthorizationUrl(requestToken));
        Mockito.verify(oAuthService).getAuthorizationUrl(requestToken);
    }

    @Test
    public void testGetAccessToken()
            throws InterruptedException, ExecutionException, IOException, AuthenticationException {
        OAuth1RequestToken requestToken = Mockito.mock(OAuth1RequestToken.class);
        OAuth1AccessToken accessToken = Mockito.mock(OAuth1AccessToken.class);
        Mockito.when(oAuthService.getAccessToken(requestToken, "")).thenReturn(accessToken);

        Assert.assertEquals(accessToken, authenticationService.getAccessToken(requestToken, ""));
        Mockito.verify(oAuthService).getAccessToken(requestToken, "");
    }

    @Test(expected = AuthenticationException.class)
    public void testGetAccessTokenWithException()
            throws InterruptedException, ExecutionException, IOException, AuthenticationException {
        OAuth1RequestToken requestToken = Mockito.mock(OAuth1RequestToken.class);
        Mockito.when(oAuthService.getAccessToken(requestToken, "")).thenThrow(new IOException());

        authenticationService.getAccessToken(requestToken, "");
    }

    @Test(expected = AuthenticationException.class)
    public void testGetAccessTokenWithInterruption()
            throws InterruptedException, ExecutionException, IOException, AuthenticationException {
        OAuth1RequestToken requestToken = Mockito.mock(OAuth1RequestToken.class);
        Mockito.when(oAuthService.getAccessToken(requestToken, "")).thenThrow(new InterruptedException());

        authenticationService.getAccessToken(requestToken, "");
    }

    @Test
    public void testAuthenticationServiceOffline() throws AuthenticationException {
        Assert.assertNotNull(authenticationServiceOffline.getRequestToken());
        Assert.assertNotNull(authenticationServiceOffline.getAuthorizationUrl(Mockito.mock(OAuth1RequestToken.class)));
        Assert.assertNotNull(authenticationServiceOffline.getAccessToken(Mockito.mock(OAuth1RequestToken.class), ""));
    }

}
