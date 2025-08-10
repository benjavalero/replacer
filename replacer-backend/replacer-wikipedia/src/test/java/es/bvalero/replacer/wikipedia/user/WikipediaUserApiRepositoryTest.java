package es.bvalero.replacer.wikipedia.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import es.bvalero.replacer.wikipedia.WikipediaUserRepository;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WikipediaUserApiRepositoryTest {

    // Dependency injection
    private ObjectMapper jsonMapper;
    private WikipediaApiHelper wikipediaApiHelper;

    private WikipediaUserApiRepository wikipediaUserApiRepository;
    private WikipediaUserRepository wikipediaUserRepository;

    @BeforeEach
    void setUp() {
        jsonMapper = spy(ObjectMapper.class);
        wikipediaApiHelper = mock(WikipediaApiHelper.class);
        wikipediaUserApiRepository = new WikipediaUserApiRepository(wikipediaApiHelper);
        wikipediaUserRepository = new WikipediaUserOfflineRepository();
    }

    @Test
    void testLoggedUserName() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "query": {
                    "userinfo": {
                        "id": 24149,
                        "name": "Benjavalero",
                        "groups": [
                            "*",
                            "user",
                            "autoconfirmed"
                        ]
                    }
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        AccessToken accessToken = AccessToken.of("a", "b");
        WikipediaUser user = wikipediaUserApiRepository
            .findAuthenticatedUser(WikipediaLanguage.getDefault(), accessToken)
            .orElse(null);
        assertNotNull(user);
        assertEquals("Benjavalero", user.getId().getUsername());
        assertTrue(user.isAutoConfirmed());
        assertFalse(user.isBot());
    }

    @Test
    void testWikipediaServiceOffline() {
        // Offline user
        AccessToken accessToken = AccessToken.of("a", "b");
        Optional<WikipediaUser> user = wikipediaUserRepository.findAuthenticatedUser(
            WikipediaLanguage.getDefault(),
            accessToken
        );
        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals("offline", u.getId().getUsername());
            assertTrue(u.isAutoConfirmed());
            assertFalse(u.isBot());
        });
    }
}
