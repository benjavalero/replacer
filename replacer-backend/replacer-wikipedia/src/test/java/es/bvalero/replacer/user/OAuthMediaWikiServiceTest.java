package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OAuthMediaWikiServiceTest {

    // Dependency injection
    private OAuth10aService mediaWikiService;

    private OAuthService oAuthMediaWikiService;
    private OAuthService oAuthOfflineService;

    @BeforeEach
    public void setUp() {
        mediaWikiService = mock(OAuth10aService.class);
        oAuthMediaWikiService = new OAuthMediaWikiService(mediaWikiService);
        oAuthOfflineService = new OAuthOfflineService();
    }

    @Test
    void testGetRequestToken() throws IOException, ExecutionException, InterruptedException, AuthorizationException {
        OAuth1RequestToken requestToken = new OAuth1RequestToken("A", "B");
        when(mediaWikiService.getRequestToken()).thenReturn(requestToken);
        when(mediaWikiService.getAuthorizationUrl(any(OAuth1RequestToken.class))).thenReturn("Z");

        RequestToken expected = RequestToken.of("A", "B", "Z");
        RequestToken actual = oAuthMediaWikiService.getRequestToken();
        assertEquals(expected, actual);

        verify(mediaWikiService).getRequestToken();
    }

    @Test
    void testGetRequestTokenWithException() throws IOException, ExecutionException, InterruptedException {
        when(mediaWikiService.getRequestToken()).thenThrow(new IOException());

        assertThrows(AuthorizationException.class, () -> oAuthMediaWikiService.getRequestToken());
    }

    @Test
    void testGetAccessToken() throws IOException, ExecutionException, InterruptedException, AuthorizationException {
        RequestToken requestToken = RequestToken.of("R", "S", "Z");
        String oAuthVerifier = "V";

        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("R", "S");
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken("A", "B");
        when(mediaWikiService.getAccessToken(oAuth1RequestToken, oAuthVerifier)).thenReturn(oAuth1AccessToken);

        AccessToken expected = AccessToken.of("A", "B");
        AccessToken actual = oAuthMediaWikiService.getAccessToken(requestToken, oAuthVerifier);

        assertEquals(expected, actual);

        verify(mediaWikiService).getAccessToken(oAuth1RequestToken, oAuthVerifier);
    }

    @Test
    void testGetAccessTokenWithException() throws IOException, ExecutionException, InterruptedException {
        RequestToken requestToken = RequestToken.of("R", "S", "Z");
        String oAuthVerifier = "V";

        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("R", "S");
        when(mediaWikiService.getAccessToken(oAuth1RequestToken, oAuthVerifier)).thenThrow(new IOException());

        assertThrows(AuthorizationException.class, () ->
            oAuthMediaWikiService.getAccessToken(requestToken, oAuthVerifier)
        );
    }

    @Test
    void testOAuthServiceOffline() throws AuthorizationException {
        RequestToken requestToken = oAuthOfflineService.getRequestToken();
        assertNotNull(requestToken);
        String authorizationUrl = "Z";
        assertNotNull(oAuthOfflineService.getAccessToken(requestToken, authorizationUrl));
    }
}
