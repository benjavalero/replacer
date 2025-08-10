package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    // Dependency injection
    private WikipediaUserRepository wikipediaUserRepository;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        wikipediaUserRepository = mock(WikipediaUserRepository.class);
        userService = new UserService(wikipediaUserRepository);
    }

    @Test
    void testFindUserByToken() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        AccessToken accessToken = AccessToken.of("a", "b");

        WikipediaUser user = WikipediaUser.builder().id(UserId.of(lang, "N")).build();
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

        WikipediaUser user = WikipediaUser.builder().id(UserId.of(lang, "N")).autoConfirmed(true).build();
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

        WikipediaUser user = WikipediaUser.builder().id(UserId.of(lang, "N")).autoConfirmed(true).bot(true).build();
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
        WikipediaUser user = WikipediaUser.builder().id(UserId.of(lang, name)).build();
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
