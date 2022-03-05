package es.bvalero.replacer.authentication.authenticateuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.authentication.AuthenticationException;
import es.bvalero.replacer.authentication.oauth.OAuthService;
import es.bvalero.replacer.authentication.oauth.RequestToken;
import es.bvalero.replacer.authentication.userrights.CheckUserRightsService;
import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.AccessTokenDto;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
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
    private WikipediaPageRepository wikipediaPageRepository;

    @Mock
    private CheckUserRightsService checkUserRightsService;

    @InjectMocks
    private AuthenticateUserService authenticateUserService;

    @BeforeEach
    public void setUp() {
        authenticateUserService = new AuthenticateUserService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAuthenticatedUser() throws AuthenticationException, WikipediaException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        RequestToken requestToken = RequestToken.of("R", "S");
        String oAuthVerifier = "Z";

        AccessToken accessToken = AccessToken.of("A", "B");
        when(oAuthService.getAccessToken(requestToken, oAuthVerifier)).thenReturn(accessToken);

        WikipediaUser wikipediaUser = WikipediaUser
            .builder()
            .lang(lang)
            .name("C")
            .group(WikipediaUserGroup.AUTO_CONFIRMED)
            .build();
        when(wikipediaPageRepository.getAuthenticatedUser(lang, accessToken)).thenReturn(wikipediaUser);

        String username = "C";
        when(checkUserRightsService.isAdmin(username)).thenReturn(true);

        AuthenticatedUser actual = authenticateUserService.authenticateUser(lang, requestToken, oAuthVerifier);

        AuthenticatedUser expected = AuthenticatedUser
            .builder()
            .name(username)
            .hasRights(true)
            .bot(false)
            .admin(true)
            .accessToken(AccessTokenDto.fromDomain(accessToken))
            .build();
        assertEquals(expected, actual);

        verify(oAuthService).getAccessToken(requestToken, oAuthVerifier);
        verify(wikipediaPageRepository).getAuthenticatedUser(lang, accessToken);
        verify(checkUserRightsService).isAdmin(username);
    }
}
