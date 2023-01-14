package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class OAuthMediaWikiServiceTest {

    @Mock
    private OAuth10aService oAuth10aService;

    @InjectMocks
    private OAuthService oAuthMediaWikiService;

    private OAuthService oAuthOfflineService;

    @BeforeEach
    public void setUp() {
        oAuthMediaWikiService = new OAuthMediaWikiService();
        oAuthOfflineService = new OAuthOfflineService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRequestToken() throws IOException, ExecutionException, InterruptedException, AuthenticationException {
        OAuth1RequestToken requestToken = new OAuth1RequestToken("A", "B");
        when(oAuth10aService.getRequestToken()).thenReturn(requestToken);

        RequestToken expected = RequestToken.of("A", "B");
        RequestToken actual = oAuthMediaWikiService.getRequestToken();
        assertEquals(expected, actual);

        verify(oAuth10aService).getRequestToken();
    }

    @Test
    void testGetRequestTokenWithException() throws IOException, ExecutionException, InterruptedException {
        when(oAuth10aService.getRequestToken()).thenThrow(new IOException());

        assertThrows(AuthenticationException.class, () -> oAuthMediaWikiService.getRequestToken());
    }

    @Test
    void testGetAuthorizationUrl() {
        RequestToken requestToken = RequestToken.of("A", "B");
        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("A", "B");
        String authorizationUrl = "Z";
        when(oAuth10aService.getAuthorizationUrl(oAuth1RequestToken)).thenReturn(authorizationUrl);

        assertEquals(authorizationUrl, oAuthMediaWikiService.getAuthorizationUrl(requestToken));

        verify(oAuth10aService).getAuthorizationUrl(oAuth1RequestToken);
    }

    @Test
    void testGetAccessToken() throws IOException, ExecutionException, InterruptedException, AuthenticationException {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "V";

        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("R", "S");
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken("A", "B");
        when(oAuth10aService.getAccessToken(oAuth1RequestToken, oAuthVerifier)).thenReturn(oAuth1AccessToken);

        AccessToken expected = AccessToken.of("A", "B");
        AccessToken actual = oAuthMediaWikiService.getAccessToken(requestToken, oAuthVerifier);

        assertEquals(expected, actual);

        verify(oAuth10aService).getAccessToken(oAuth1RequestToken, oAuthVerifier);
    }

    @Test
    void testGetAccessTokenWithException() throws IOException, ExecutionException, InterruptedException {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "V";

        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("R", "S");
        when(oAuth10aService.getAccessToken(oAuth1RequestToken, oAuthVerifier)).thenThrow(new IOException());

        assertThrows(
            AuthenticationException.class,
            () -> oAuthMediaWikiService.getAccessToken(requestToken, oAuthVerifier)
        );
    }

    @Test
    void testOAuthServiceOffline() throws AuthenticationException {
        RequestToken requestToken = oAuthOfflineService.getRequestToken();
        assertNotNull(requestToken);
        assertFalse(oAuthOfflineService.getAuthorizationUrl(requestToken).isEmpty());
        String authorizationUrl = "Z";
        assertNotNull(oAuthOfflineService.getAccessToken(requestToken, authorizationUrl));
    }
}
