package es.bvalero.replacer.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuthenticationMediaWikiServiceTest {

    @Mock
    private OAuth10aService oAuthMediaWikiService;

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private AuthenticationMediaWikiService authenticationService;

    @BeforeEach
    public void setUp() {
        authenticationService = new AuthenticationMediaWikiService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetRequestToken() throws IOException, ExecutionException, InterruptedException, ReplacerException {
        OAuthToken oAuthToken = OAuthToken.of("A", "B");
        String authorizationUrl = "C";
        OAuth1RequestToken requestToken = authenticationService.convertToRequestToken(oAuthToken);
        when(oAuthMediaWikiService.getRequestToken()).thenReturn(requestToken);
        when(oAuthMediaWikiService.getAuthorizationUrl(requestToken)).thenReturn(authorizationUrl);
        RequestToken expected = RequestToken.of("A", "B", "C");

        Assertions.assertEquals(expected, authenticationService.getRequestToken());
        verify(oAuthMediaWikiService).getRequestToken();
        verify(oAuthMediaWikiService).getAuthorizationUrl(requestToken);
    }

    @Test
    void testGetRequestTokenWithException() throws IOException, ExecutionException, InterruptedException {
        when(oAuthMediaWikiService.getRequestToken()).thenThrow(new IOException());

        Assertions.assertThrows(ReplacerException.class, () -> authenticationService.getRequestToken());
    }

    @Test
    void testGetLoggedUser() throws IOException, ExecutionException, InterruptedException, ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        OAuthToken oAuthTokenRequest = OAuthToken.of("R", "S");
        String oAuthVerifier = "V";
        String name = "A";
        OAuth1RequestToken requestToken = authenticationService.convertToRequestToken(oAuthTokenRequest);
        OAuthToken oAuthTokenAccess = OAuthToken.of("A", "B");
        OAuth1AccessToken accessToken = authenticationService.convertToAccessToken(oAuthTokenAccess);
        when(oAuthMediaWikiService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);
        when(wikipediaService.getAuthenticatedUser(lang, oAuthTokenAccess))
            .thenReturn(WikipediaUser.of(name, List.of(WikipediaUserGroup.AUTOCONFIRMED)));
        AuthenticateResponse expected = AuthenticateResponse.of(name, true, false, true, "A", "B");

        authenticationService.setAdminUser(name);
        AuthenticateResponse actual = authenticationService.authenticate(lang, oAuthTokenRequest, oAuthVerifier);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testIsAdminUser() {
        authenticationService.setAdminUser("X");
        Assertions.assertTrue(authenticationService.isAdminUser("X"));
        Assertions.assertFalse(authenticationService.isAdminUser("Y"));
    }
}
