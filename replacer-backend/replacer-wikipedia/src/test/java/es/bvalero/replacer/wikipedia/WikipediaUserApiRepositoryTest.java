package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

class WikipediaUserApiRepositoryTest {

    @Spy
    private ObjectMapper jsonMapper;

    @Mock
    private WikipediaApiHelper wikipediaApiHelper;

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
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"query\": {\n" +
            "        \"userinfo\": {\n" +
            "            \"id\": 24149,\n" +
            "            \"name\": \"Benjavalero\",\n" +
            "            \"groups\": [\n" +
            "                \"*\",\n" +
            "                \"user\",\n" +
            "                \"autoconfirmed\"\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
        WikipediaApiResponse response = jsonMapper.readValue(textResponse, WikipediaApiResponse.class);
        when(wikipediaApiHelper.executeApiRequest(any(WikipediaApiRequest.class))).thenReturn(response);

        WikipediaUser user = wikipediaUserApiRepository
            .findAuthenticatedUser(WikipediaLanguage.getDefault(), AccessToken.empty())
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
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"query\": {\n" +
            "        \"users\": [\n" +
            "            {\n" +
            "                \"userid\": 24149,\n" +
            "                \"name\": \"Benjavalero\",\n" +
            "                \"groups\": [\n" +
            "                    \"*\",\n" +
            "                    \"user\",\n" +
            "                    \"autoconfirmed\"\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
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
            "{\n" +
            "    \"batchcomplete\": true,\n" +
            "    \"query\": {\n" +
            "        \"users\": [\n" +
            "            {\n" +
            "                \"name\": \"Missi\",\n" +
            "                \"missing\": true\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";
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
        Optional<WikipediaUser> user = wikipediaUserRepository.findAuthenticatedUser(
            WikipediaLanguage.getDefault(),
            AccessToken.empty()
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
