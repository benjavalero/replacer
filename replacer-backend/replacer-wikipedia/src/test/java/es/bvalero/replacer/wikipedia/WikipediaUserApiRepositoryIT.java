package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.JsonMapperConfiguration;
import es.bvalero.replacer.MediaWikiApiConfiguration;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaUserApiRepository.class,
        WikipediaApiRequestHelper.class,
        MediaWikiApiConfiguration.class,
        JsonMapperConfiguration.class,
    }
)
class WikipediaUserApiRepositoryIT {

    @Autowired
    private WikipediaUserApiRepository wikipediaUserApiRepository;

    @Test
    void testGetUser() {
        String username = "Benjavalero";
        WikipediaUser user = wikipediaUserApiRepository
            .findByUsername(WikipediaLanguage.SPANISH, username)
            .orElse(null);
        assertNotNull(user);
        assertEquals(username, user.getName());
        assertFalse(user.getGroups().isEmpty());
        assertTrue(user.getGroups().contains(WikipediaUserGroup.AUTO_CONFIRMED));
        assertFalse(user.getGroups().contains(WikipediaUserGroup.BOT));
    }
}
