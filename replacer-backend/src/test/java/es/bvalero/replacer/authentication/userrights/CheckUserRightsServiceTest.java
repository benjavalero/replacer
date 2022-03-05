package es.bvalero.replacer.authentication.userrights;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CheckUserRightsServiceTest {

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

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
    void testValidateNotAdminUser() {
        checkUserRightsService.validateAdminUser("X");
        assertThrows(ForbiddenException.class, () -> checkUserRightsService.validateAdminUser("Z"));
    }

    @Test
    void testIsBot() throws WikipediaException {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(true);
        when(wikipediaPageRepository.getWikipediaUser(any(WikipediaLanguage.class), anyString())).thenReturn(user);

        assertTrue(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBot() throws WikipediaException {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(false);
        when(wikipediaPageRepository.getWikipediaUser(any(WikipediaLanguage.class), anyString())).thenReturn(user);

        assertFalse(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBotWithException() throws WikipediaException {
        when(wikipediaPageRepository.getWikipediaUser(any(WikipediaLanguage.class), anyString()))
            .thenThrow(new WikipediaException());

        assertFalse(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testValidateNotBot() throws WikipediaException {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(false);
        when(wikipediaPageRepository.getWikipediaUser(any(WikipediaLanguage.class), anyString())).thenReturn(user);

        assertThrows(
            ForbiddenException.class,
            () -> checkUserRightsService.validateBotUser(WikipediaLanguage.SPANISH, "X")
        );
    }
}
