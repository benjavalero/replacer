package es.bvalero.replacer.wikipedia.authentication;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthService;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.UserInfo;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class AuthenticationServiceTest {

    @Mock
    private OAuthService oAuthService;

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        authenticationService = new AuthenticationService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetRequestToken() throws ReplacerException {
        OAuthToken oAuthToken = OAuthToken.of("A", "B");
        String authorizationUrl = "C";
        Mockito.when(oAuthService.getRequestToken()).thenReturn(oAuthToken);
        Mockito.when(oAuthService.getAuthorizationUrl(oAuthToken)).thenReturn(authorizationUrl);
        RequestToken expected = RequestToken.of("A", "B", "C");

        Assertions.assertEquals(expected, authenticationService.getRequestToken());
        Mockito.verify(oAuthService).getRequestToken();
        Mockito.verify(oAuthService).getAuthorizationUrl(oAuthToken);
    }

    @Test
    void testGetRequestTokenWithException() throws ReplacerException {
        Mockito.when(oAuthService.getRequestToken()).thenThrow(new ReplacerException());

        Assertions.assertThrows(ReplacerException.class, () -> authenticationService.getRequestToken());
    }

    @Test
    void testGetLoggedUser() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        OAuthToken requestToken = OAuthToken.of("R", "S");
        String oAuthVerifier = "V";
        String name = "A";
        OAuthToken accessToken = OAuthToken.of("A", "B");
        Mockito.when(oAuthService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);
        Mockito
            .when(wikipediaService.getUserInfo(lang, accessToken))
            .thenReturn(UserInfo.of(name, List.of(AuthenticationService.GROUP_AUTOCONFIRMED)));
        AuthenticateResponse expected = AuthenticateResponse.of(name, true, true, "A", "B");

        authenticationService.setAdminUser(name);
        AuthenticateResponse actual = authenticationService.authenticate(lang, requestToken, oAuthVerifier);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testIsAdminUser() {
        authenticationService.setAdminUser("X");
        Assertions.assertTrue(authenticationService.isAdminUser("X"));
        Assertions.assertFalse(authenticationService.isAdminUser("Y"));
    }
}
