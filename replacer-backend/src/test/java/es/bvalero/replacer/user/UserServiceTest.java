package es.bvalero.replacer.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
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

        WikipediaUser wikipediaUser = WikipediaUser
            .builder()
            .lang(lang)
            .name("N")
            .group(WikipediaUserGroup.USER)
            .build();
        when(wikipediaUserRepository.findAuthenticatedUser(lang, accessToken)).thenReturn(Optional.of(wikipediaUser));

        Optional<ReplacerUser> user = userService.findUser(lang, accessToken);

        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals(lang, u.getLang());
            assertEquals(wikipediaUser.getName(), u.getName());
            assertFalse(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindUserByName() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "N";

        WikipediaUser wikipediaUser = WikipediaUser
            .builder()
            .lang(lang)
            .name(name)
            .group(WikipediaUserGroup.USER)
            .build();
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(wikipediaUser));

        Optional<ReplacerUser> user = userService.findUser(lang, name);

        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals(lang, u.getLang());
            assertEquals(wikipediaUser.getName(), u.getName());
            assertFalse(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindUserWithRights() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "N";

        WikipediaUser wikipediaUser = WikipediaUser
            .builder()
            .lang(lang)
            .name(name)
            .group(WikipediaUserGroup.USER)
            .group(WikipediaUserGroup.AUTO_CONFIRMED)
            .build();
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(wikipediaUser));

        Optional<ReplacerUser> user = userService.findUser(lang, name);

        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals(lang, u.getLang());
            assertEquals(wikipediaUser.getName(), u.getName());
            assertTrue(u.hasRights());
            assertFalse(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindBotUser() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "N";

        WikipediaUser wikipediaUser = WikipediaUser
            .builder()
            .lang(lang)
            .name(name)
            .group(WikipediaUserGroup.USER)
            .group(WikipediaUserGroup.AUTO_CONFIRMED)
            .group(WikipediaUserGroup.BOT)
            .build();
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(wikipediaUser));

        Optional<ReplacerUser> user = userService.findUser(lang, name);

        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals(lang, u.getLang());
            assertEquals(wikipediaUser.getName(), u.getName());
            assertTrue(u.hasRights());
            assertTrue(u.isBot());
            assertFalse(u.isAdmin());
        });
    }

    @Test
    void testFindAdminUser() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        String name = "N";

        WikipediaUser wikipediaUser = WikipediaUser
            .builder()
            .lang(lang)
            .name(name)
            .group(WikipediaUserGroup.USER)
            .group(WikipediaUserGroup.AUTO_CONFIRMED)
            .build();
        when(wikipediaUserRepository.findByUsername(lang, name)).thenReturn(Optional.of(wikipediaUser));
        userService.setAdminUser(name);

        Optional<ReplacerUser> user = userService.findUser(lang, name);

        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals(lang, u.getLang());
            assertEquals(wikipediaUser.getName(), u.getName());
            assertTrue(u.hasRights());
            assertFalse(u.isBot());
            assertTrue(u.isAdmin());
        });
    }
}
