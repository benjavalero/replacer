package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceTest {

    @Mock
    private WikipediaUserRepository wikipediaUserRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUserByToken() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        AccessToken accessToken = AccessToken.of("a", "b");

        WikipediaUser user = WikipediaUser.of(UserId.of(lang, "N"), List.of());
        // We pass on purpose a null access token as we are mocking the response
        when(wikipediaUserRepository.findAuthenticatedUser(lang, accessToken)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findAuthenticatedUser(lang, accessToken);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getId(), u.getId());
            assertFalse(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindUserWithRights() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        AccessToken accessToken = AccessToken.of("a", "b");

        WikipediaUser user = WikipediaUser.of(UserId.of(lang, "N"), List.of(WikipediaUserGroup.AUTO_CONFIRMED));
        // We pass on purpose a null access token as we are mocking the response
        when(wikipediaUserRepository.findAuthenticatedUser(lang, accessToken)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findAuthenticatedUser(lang, accessToken);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getId(), u.getId());
            assertTrue(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindBotUser() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        AccessToken accessToken = AccessToken.of("a", "b");

        WikipediaUser user = WikipediaUser.of(
            UserId.of(lang, "N"),
            List.of(WikipediaUserGroup.AUTO_CONFIRMED, WikipediaUserGroup.BOT)
        );
        // We pass on purpose a null access token as we are mocking the response
        when(wikipediaUserRepository.findAuthenticatedUser(lang, accessToken)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findAuthenticatedUser(lang, accessToken);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getId(), u.getId());
            assertTrue(u.hasRights());
            assertTrue(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindAdminUser() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        AccessToken accessToken = AccessToken.of("a", "b");
        String name = "ADMIN";

        userService.setAdminUser(name);
        WikipediaUser user = WikipediaUser.of(UserId.of(lang, name), List.of());
        // We pass on purpose a null access token as we are mocking the response
        when(wikipediaUserRepository.findAuthenticatedUser(lang, accessToken)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findAuthenticatedUser(lang, accessToken);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getId(), u.getId());
            assertFalse(u.hasRights());
            assertFalse(u.isBot());
            assertTrue(u.isAdmin());
        });
    }
}
