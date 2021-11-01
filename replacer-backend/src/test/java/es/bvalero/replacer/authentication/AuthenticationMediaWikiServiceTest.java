package es.bvalero.replacer.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.wikipedia.OAuthToken;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuthenticationMediaWikiServiceTest {

    @Mock
    private OAuth10aService oAuthMediaWikiService;

    @InjectMocks
    private AuthenticationMediaWikiService authenticationService;

    @BeforeEach
    public void setUp() {
        authenticationService = new AuthenticationMediaWikiService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetRequestToken() throws IOException, ExecutionException, InterruptedException, ReplacerException {
        OAuth1RequestToken requestToken = new OAuth1RequestToken("A", "B");
        when(oAuthMediaWikiService.getRequestToken()).thenReturn(requestToken);
        OAuthToken expected = OAuthToken.of("A", "B");

        assertEquals(expected, authenticationService.getRequestToken());
        verify(oAuthMediaWikiService).getRequestToken();
    }

    @Test
    void testGetRequestTokenWithException() throws IOException, ExecutionException, InterruptedException {
        when(oAuthMediaWikiService.getRequestToken()).thenThrow(new IOException());

        assertThrows(ReplacerException.class, () -> authenticationService.getRequestToken());
    }

    @Test
    void testGetAccessToken() throws IOException, ExecutionException, InterruptedException, ReplacerException {
        OAuthToken oAuthTokenRequest = OAuthToken.of("R", "S");
        String oAuthVerifier = "V";
        OAuth1RequestToken requestToken = new OAuth1RequestToken("R", "S");
        OAuthToken oAuthTokenAccess = OAuthToken.of("A", "B");
        OAuth1AccessToken accessToken = new OAuth1AccessToken("A", "B");
        when(oAuthMediaWikiService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);
        OAuthToken actual = authenticationService.getAccessToken(oAuthTokenRequest, oAuthVerifier);

        assertEquals(oAuthTokenAccess, actual);
    }
}
