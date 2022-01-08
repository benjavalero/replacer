package es.bvalero.replacer.authentication.userrights;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CheckUserRightsServiceTest {

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private CheckUserRightsService checkUserRightsService;

    @BeforeEach
    public void setUp() {
        checkUserRightsService = new CheckUserRightsService();
        checkUserRightsService.setAdminUser("X");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsAdmin() {
        assertTrue(checkUserRightsService.isAdmin("X"));
        assertFalse(checkUserRightsService.isAdmin("Z"));
    }

    @Test
    void testIsBot() throws WikipediaException {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(true);
        when(wikipediaService.getWikipediaUser(any(WikipediaLanguage.class), anyString())).thenReturn(user);

        assertTrue(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBot() throws WikipediaException {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(false);
        when(wikipediaService.getWikipediaUser(any(WikipediaLanguage.class), anyString())).thenReturn(user);

        assertFalse(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBotWithException() throws WikipediaException {
        when(wikipediaService.getWikipediaUser(any(WikipediaLanguage.class), anyString()))
            .thenThrow(new WikipediaException());

        assertFalse(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }
}
