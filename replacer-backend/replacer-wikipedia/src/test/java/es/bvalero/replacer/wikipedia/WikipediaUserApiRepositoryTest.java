package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

        // We pass on purpose a null access token as we are mocking the response
        WikipediaUser user = wikipediaUserApiRepository
            .findAuthenticatedUser(WikipediaLanguage.getDefault(), null)
            .orElse(null);
        assertNotNull(user);
        assertEquals("Benjavalero", user.getId().getUsername());
        assertEquals(
            Set.of(WikipediaUserGroup.GENERIC, WikipediaUserGroup.USER, WikipediaUserGroup.AUTO_CONFIRMED),
            new HashSet<>(user.getGroups())
        );
    }

    @Test
    void testLoggedUser() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "query": {
                    "users": [
                        {
                            "userid": 24149,
                            "name": "Benjavalero",
                            "groups": [
                                "*",
                                "user",
                                "autoconfirmed"
                            ]
                        }
                    ]
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaUser user = wikipediaUserApiRepository
            .findById(UserId.of(WikipediaLanguage.getDefault(), "Benjavalero"))
            .orElse(null);
        assertNotNull(user);
        assertEquals("Benjavalero", user.getId().getUsername());
        assertEquals(
            Set.of(WikipediaUserGroup.GENERIC, WikipediaUserGroup.USER, WikipediaUserGroup.AUTO_CONFIRMED),
            new HashSet<>(user.getGroups())
        );
    }

    @Test
    void testMissingUser() throws Exception {
        // API response
        String textResponse =
            """
            {
                "batchcomplete": true,
                "query": {
                    "users": [
                        {
                            "name": "Missi",
                            "missing": true
                        }
                    ]
                }
            }
            """;
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaUser user = wikipediaUserApiRepository
            .findById(UserId.of(WikipediaLanguage.getDefault(), "Missi"))
            .orElse(null);
        assertNull(user);
    }

    @Test
    void testWikipediaServiceOffline() {
        // Offline user
        // We pass on purpose a null access token as we are mocking the response
        Optional<WikipediaUser> user = wikipediaUserRepository.findAuthenticatedUser(
            WikipediaLanguage.getDefault(),
            null
        );
        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals("offline", u.getId().getUsername());
            assertEquals(
                Arrays.stream(WikipediaUserGroup.values()).collect(Collectors.toUnmodifiableSet()),
                new HashSet<>(u.getGroups())
            );

            Optional<WikipediaUser> user2 = wikipediaUserRepository.findById(
                UserId.of(WikipediaLanguage.getDefault(), "x")
            );
            assertEquals(u, user2.orElse(null));
        });
    }
}
