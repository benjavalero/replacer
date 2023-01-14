package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

class WikipediaUserApiRepositoryTest {

    @Spy
    private ObjectMapper jsonMapper;

    @Mock
    private WikipediaApiRequestHelper wikipediaApiRequestHelper;

    @InjectMocks
    private WikipediaUserApiRepository wikipediaUserApiRepository;

    private WikipediaUserRepository wikipediaUserRepository;

    @BeforeEach
    void setUp() {
        wikipediaUserApiRepository = new WikipediaUserApiRepository();
        wikipediaUserRepository = new WikipediaUserOfflineRepository();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoggedUserName() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":\"\",\"query\":{\"userinfo\":{\"id\":24149,\"name\":\"Benjavalero\",\"groups\":[\"*\",\"user\",\"autoconfirmed\"]}}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaUser user = wikipediaUserApiRepository
            .findAuthenticatedUser(WikipediaLanguage.getDefault(), AccessToken.empty())
            .orElse(null);
        assertNotNull(user);
        assertEquals("Benjavalero", user.getName());
        assertEquals(
            Set.of(WikipediaUserGroup.GENERIC, WikipediaUserGroup.USER, WikipediaUserGroup.AUTO_CONFIRMED),
            new HashSet<>(user.getGroups())
        );
    }

    @Test
    void testLoggedUser() throws Exception {
        // API response
        String textResponse =
            "{\"batchcomplete\":true,\"query\":{\"users\":[{\"userid\":24149,\"name\":\"Benjavalero\",\"groups\":[\"*\",\"user\",\"autoconfirmed\"]}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaUser user = wikipediaUserApiRepository
            .findByUsername(WikipediaLanguage.getDefault(), "Benjavalero")
            .orElse(null);
        assertNotNull(user);
        assertEquals("Benjavalero", user.getName());
        assertEquals(
            Set.of(WikipediaUserGroup.GENERIC, WikipediaUserGroup.USER, WikipediaUserGroup.AUTO_CONFIRMED),
            new HashSet<>(user.getGroups())
        );
    }

    @Test
    void testMissingUser() throws Exception {
        // API response
        String textResponse = "{\"batchcomplete\":true,\"query\":{\"users\":[{\"name\":\"Missi\",\"missing\":true}]}}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiRequestHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaUser user = wikipediaUserApiRepository
            .findByUsername(WikipediaLanguage.getDefault(), "Missi")
            .orElse(null);
        assertNull(user);
    }

    @Test
    void testWikipediaServiceOffline() {
        // Offline user
        Optional<WikipediaUser> user = wikipediaUserRepository.findAuthenticatedUser(
            WikipediaLanguage.getDefault(),
            AccessToken.empty()
        );
        assertTrue(user.isPresent());
        user.ifPresent(u -> {
            assertEquals("offline", u.getName());
            assertEquals(
                Arrays.stream(WikipediaUserGroup.values()).collect(Collectors.toUnmodifiableSet()),
                new HashSet<>(u.getGroups())
            );

            Optional<WikipediaUser> user2 = wikipediaUserRepository.findByUsername(WikipediaLanguage.getDefault(), "x");
            assertEquals(u, user2.orElse(null));
        });
    }
}
