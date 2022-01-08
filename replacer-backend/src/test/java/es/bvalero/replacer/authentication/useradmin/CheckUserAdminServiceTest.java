package es.bvalero.replacer.authentication.useradmin;

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

class CheckUserAdminServiceTest {

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private CheckUserAdminService checkUserAdminService;

    @BeforeEach
    public void setUp() {
        checkUserAdminService = new CheckUserAdminService();
        checkUserAdminService.setAdminUser("X");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsAdminUser() {
        assertTrue(checkUserAdminService.isAdminUser("X"));
        assertFalse(checkUserAdminService.isAdminUser("Z"));
    }

    @Test
    void testIsBot() throws WikipediaException {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(true);
        when(wikipediaService.getWikipediaUser(any(WikipediaLanguage.class), anyString())).thenReturn(user);

        assertTrue(checkUserAdminService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBot() throws WikipediaException {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(false);
        when(wikipediaService.getWikipediaUser(any(WikipediaLanguage.class), anyString())).thenReturn(user);

        assertFalse(checkUserAdminService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBotWithException() throws WikipediaException {
        when(wikipediaService.getWikipediaUser(any(WikipediaLanguage.class), anyString()))
            .thenThrow(new WikipediaException());

        assertFalse(checkUserAdminService.isBot(WikipediaLanguage.getDefault(), "X"));
    }
}
