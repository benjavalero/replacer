package es.bvalero.replacer.authentication.authenticateuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.authentication.useradmin.CheckUserAdminService;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuthenticateUserServiceTest {

    @Mock
    private OAuthService oAuthService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private CheckUserAdminService checkUserAdminService;

    @InjectMocks
    private AuthenticateUserService authenticateUserService;

    @BeforeEach
    public void setUp() {
        authenticateUserService = new AuthenticateUserService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAuthenticatedUser() throws AuthenticationException, ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "Z";

        AccessToken accessToken = AccessToken.of("A", "B");
        when(oAuthService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);

        WikipediaUser wikipediaUser = WikipediaUser.builder().name("C").group(WikipediaUserGroup.AUTOCONFIRMED).build();
        when(wikipediaService.getAuthenticatedUser(lang, accessToken)).thenReturn(wikipediaUser);

        String username = "C";
        when(checkUserAdminService.isAdminUser(username)).thenReturn(true);

        AuthenticatedUser actual = authenticateUserService.authenticateUser(lang, requestToken, oAuthVerifier);

        AuthenticatedUser expected = AuthenticatedUser
            .builder()
            .name(username)
            .hasRights(true)
            .bot(false)
            .admin(true)
            .token(accessToken.getToken())
            .tokenSecret(accessToken.getTokenSecret())
            .build();
        assertEquals(expected, actual);

        verify(oAuthService).getAccessToken(requestToken, oAuthVerifier);
        verify(wikipediaService).getAuthenticatedUser(lang, accessToken);
        verify(checkUserAdminService).isAdminUser(username);
    }
}
