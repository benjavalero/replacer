package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        UserId userId = UserId.of(lang, name);
        User user = User.builder().id(userId).hasRights(true).admin(true).build();
        when(userService.findUserById(userId)).thenReturn(Optional.of(user));

        assertTrue(userRightsService.isAdmin(userId));
    }

    @Test
    void testIsNotAdmin() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        UserId userId = UserId.of(lang, name);
        User user = User.builder().id(userId).hasRights(true).build();
        when(userService.findUserById(userId)).thenReturn(Optional.of(user));

        assertFalse(userRightsService.isAdmin(userId));
    }

    @Test
    void testValidateNotAdmin() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        UserId userId = UserId.of(lang, name);
        User user = User.builder().id(userId).hasRights(true).build();
        when(userService.findUserById(userId)).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> userRightsService.validateAdminUser(userId));
    }

    @Test
    void testIsBot() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        UserId userId = UserId.of(lang, name);
        User user = User.builder().id(userId).hasRights(true).bot(true).build();
        when(userService.findUserById(userId)).thenReturn(Optional.of(user));

        assertTrue(userRightsService.isBot(userId));
    }

    @Test
    void testIsNotBot() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        UserId userId = UserId.of(lang, name);
        User user = User.builder().id(userId).hasRights(true).bot(false).build();
        when(userService.findUserById(userId)).thenReturn(Optional.of(user));

        assertFalse(userRightsService.isBot(userId));
    }

    @Test
    void testIsNotBotWithException() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        UserId userId = UserId.of(lang, name);
        when(userService.findUserById(userId)).thenReturn(Optional.empty());

        assertFalse(userRightsService.isBot(userId));
    }

    @Test
    void testValidateNotBot() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "X";
        UserId userId = UserId.of(lang, name);
        User user = User.builder().id(userId).hasRights(true).bot(false).build();
        when(userService.findUserById(userId)).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> userRightsService.validateBotUser(userId));
    }
}
