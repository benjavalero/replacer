package es.bvalero.replacer.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserAuthenticatorTest {

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private UserAuthenticator userAuthenticator;

    @BeforeEach
    public void setUp() {
        userAuthenticator = new UserAuthenticator();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAuthenticatedUser() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        AccessToken accessToken = AccessToken.of("A", "B");

        WikipediaUser wikipediaUser = WikipediaUser.of("C", List.of(WikipediaUserGroup.AUTOCONFIRMED));
        when(wikipediaService.getAuthenticatedUser(lang, accessToken)).thenReturn(wikipediaUser);

        userAuthenticator.setAdminUser("C");

        AuthenticatedUser actual = userAuthenticator.getAuthenticatedUser(lang, accessToken);

        AuthenticatedUser expected = AuthenticatedUser
            .builder()
            .name("C")
            .hasRights(true)
            .bot(false)
            .admin(true)
            .token(accessToken.getToken())
            .tokenSecret(accessToken.getTokenSecret())
            .build();
        assertEquals(expected, actual);

        verify(wikipediaService).getAuthenticatedUser(lang, accessToken);
    }
}
