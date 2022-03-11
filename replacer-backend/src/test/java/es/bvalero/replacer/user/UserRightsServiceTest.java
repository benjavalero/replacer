package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacerUser;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class UserRightsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRightsService userRightsService;

    @BeforeEach
    public void setUp() {
        userRightsService = new UserRightsService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsAdmin() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        ReplacerUser user = ReplacerUser.builder()
            .lang(lang)
            .name(name)
            .hasRights(true)
            .admin(true)
            .build();
        when(userService.findUser(lang, name)).thenReturn(Optional.of(user));

        assertTrue(userRightsService.isAdmin(lang, name));
    }

    @Test
    void testIsNotAdmin() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        ReplacerUser user = ReplacerUser.builder()
            .lang(lang)
            .name(name)
            .hasRights(true)
            .admin(false)
            .build();
        when(userService.findUser(lang, name)).thenReturn(Optional.of(user));

        assertFalse(userRightsService.isAdmin(lang, name));
    }

    @Test
    void testValidateNotAdmin() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        ReplacerUser user = ReplacerUser.builder()
            .lang(lang)
            .name(name)
            .hasRights(true)
            .admin(false)
            .build();
        when(userService.findUser(lang, name)).thenReturn(Optional.of(user));

        assertThrows(
            ForbiddenException.class,
            () -> userRightsService.validateAdminUser(lang, name)
        );
    }

    @Test
    void testIsBot() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        ReplacerUser user = ReplacerUser.builder()
            .lang(lang)
            .name(name)
            .hasRights(true)
            .bot(true)
            .build();
        when(userService.findUser(lang, name)).thenReturn(Optional.of(user));

        assertTrue(userRightsService.isBot(lang, name));
    }

    @Test
    void testIsNotBot() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        ReplacerUser user = ReplacerUser.builder()
            .lang(lang)
            .name(name)
            .hasRights(true)
            .bot(false)
            .build();
        when(userService.findUser(lang, name)).thenReturn(Optional.of(user));

        assertFalse(userRightsService.isBot(lang, name));
    }

    @Test
    void testIsNotBotWithException() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        when(userService.findUser(lang, name))
            .thenReturn(Optional.empty());

        assertFalse(userRightsService.isBot(lang, name));
    }

    @Test
    void testValidateNotBot() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        ReplacerUser user = ReplacerUser.builder()
            .lang(lang)
            .name(name)
            .hasRights(true)
            .bot(false)
            .build();
        when(userService.findUser(lang, name)).thenReturn(Optional.of(user));

        assertThrows(
            ForbiddenException.class,
            () -> userRightsService.validateBotUser(lang, name)
        );
    }
}
