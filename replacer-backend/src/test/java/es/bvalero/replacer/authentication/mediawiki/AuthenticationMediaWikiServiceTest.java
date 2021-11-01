package es.bvalero.replacer.authentication.mediawiki;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.authentication.RequestToken;
import es.bvalero.replacer.domain.AccessToken;
import es.bvalero.replacer.domain.ReplacerException;
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

        RequestToken expected = RequestToken.of("A", "B");
        RequestToken actual = authenticationService.getRequestToken();
        assertEquals(expected, actual);

        verify(oAuthMediaWikiService, times(1)).getRequestToken();
    }

    @Test
    void testGetRequestTokenWithException() throws IOException, ExecutionException, InterruptedException {
        when(oAuthMediaWikiService.getRequestToken()).thenThrow(new IOException());

        assertThrows(ReplacerException.class, () -> authenticationService.getRequestToken());
    }

    @Test
    void testGetAccessToken() throws IOException, ExecutionException, InterruptedException, ReplacerException {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "V";

        OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken("R", "S");
        OAuth1AccessToken oAuth1AccessToken = new OAuth1AccessToken("A", "B");
        when(oAuthMediaWikiService.getAccessToken(oAuth1RequestToken, oAuthVerifier)).thenReturn(oAuth1AccessToken);

        AccessToken expected = AccessToken.of("A", "B");
        AccessToken actual = authenticationService.getAccessToken(requestToken, oAuthVerifier);

        assertEquals(expected, actual);
    }

    @Test
    void testGetAccessTokenWithException() throws IOException, ExecutionException, InterruptedException {
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "V";

        when(oAuthMediaWikiService.getAccessToken(any(OAuth1RequestToken.class), anyString()))
            .thenThrow(new IOException());

        assertThrows(ReplacerException.class, () -> authenticationService.getAccessToken(requestToken, oAuthVerifier));
    }
}
