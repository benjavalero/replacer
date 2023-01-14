package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserGroup;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import java.util.Collections;
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
        AccessToken accessToken = AccessToken.empty();

        WikipediaUser user = WikipediaUser.of("N", Collections.emptyList());
        when(wikipediaUserRepository.findAuthenticatedUser(lang, accessToken)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findAuthenticatedUser(lang, accessToken);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getName(), u.getName());
            assertFalse(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindUserByName() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "N";

        WikipediaUser user = WikipediaUser.of(name, Collections.emptyList());
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByName(lang, name);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getName(), u.getName());
            assertFalse(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindUserWithRights() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "N";

        WikipediaUser user = WikipediaUser.of(name, List.of(WikipediaUserGroup.AUTO_CONFIRMED));
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByName(lang, name);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getName(), u.getName());
            assertTrue(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindBotUser() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "N";

        WikipediaUser user = WikipediaUser.of(name, List.of(WikipediaUserGroup.AUTO_CONFIRMED, WikipediaUserGroup.BOT));
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByName(lang, name);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getName(), u.getName());
            assertTrue(u.hasRights());
            assertTrue(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindAdminUser() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "ADMIN";

        userService.setAdminUser(name);
        WikipediaUser user = WikipediaUser.of(name, Collections.emptyList());
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByName(lang, name);

        assertTrue(result.isPresent());
        result.ifPresent(u -> {
            assertEquals(user.getName(), u.getName());
            assertFalse(u.hasRights());
            assertFalse(u.isBot());
            assertTrue(u.isAdmin());
        });
    }
}
