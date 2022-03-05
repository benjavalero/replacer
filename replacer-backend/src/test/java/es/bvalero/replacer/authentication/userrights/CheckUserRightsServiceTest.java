package es.bvalero.replacer.authentication.userrights;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.common.domain.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class CheckUserRightsServiceTest {

    @Mock
    private WikipediaUserRepository wikipediaUserRepository;

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
    void testIsBot() {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(true);
        when(wikipediaUserRepository.findByUsername(any(WikipediaLanguage.class), anyString())).thenReturn(Optional.of(user));

        assertTrue(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBot() {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(false);
        when(wikipediaUserRepository.findByUsername(any(WikipediaLanguage.class), anyString())).thenReturn(Optional.of(user));

        assertFalse(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testIsNotBotWithException() {
        when(wikipediaUserRepository.findByUsername(any(WikipediaLanguage.class), anyString()))
            .thenReturn(Optional.empty());

        assertFalse(checkUserRightsService.isBot(WikipediaLanguage.getDefault(), "X"));
    }

    @Test
    void testValidateNotBot() {
        WikipediaUser user = mock(WikipediaUser.class);
        when(user.isBot()).thenReturn(false);
        when(wikipediaUserRepository.findByUsername(any(WikipediaLanguage.class), anyString())).thenReturn(Optional.of(user));

        assertThrows(
            ForbiddenException.class,
            () -> checkUserRightsService.validateBotUser(WikipediaLanguage.SPANISH, "X")
        );
    }
}
