package es.bvalero.replacer.page.find;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.JsonMapperConfiguration;
import es.bvalero.replacer.MediaWikiConfiguration;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.WikipediaException;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaPageApiRepository.class,
        WikipediaApiHelper.class,
        MediaWikiConfiguration.class,
        JsonMapperConfiguration.class,
    }
)
class WikipediaPageApiRepositoryIT {

    @Autowired
    private WikipediaPageApiRepository wikipediaService;

    @Test
    void testGetPageContent() throws WikipediaException {
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService
            .findByTitle(WikipediaLanguage.SPANISH, title)
            .orElseThrow(WikipediaException::new);
        assertNotNull(page);
        assertEquals(6219990, page.getPageId());
        assertEquals(title, page.getTitle());
        assertEquals(WikipediaNamespace.USER, page.getNamespace());
        assertTrue(page.getLastUpdate().toLocalDate().getYear() >= 2016);
        assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPagesContent() throws WikipediaException {
        // We pass a null access token to retrieve an anonymous edit token
        WikipediaLanguage lang = WikipediaLanguage.SPANISH;
        Collection<WikipediaPage> pages = wikipediaService.findByKeys(
            List.of(PageKey.of(lang, 6219990), PageKey.of(lang, 6903884))
        );
        assertNotNull(pages);
        assertEquals(2, pages.size());
        assertTrue(pages.stream().anyMatch(page -> page.getPageId() == 6219990));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getPageId() == 6219990)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getContent()
                .contains("Orihuela")
        );
        assertTrue(pages.stream().anyMatch(page -> page.getPageId() == 6903884));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getPageId() == 6903884)
                .findAny()
                .orElseThrow(WikipediaException::new)
                .getContent()
                .contains("Pais Vasco")
        );
    }

    @Test
    void testGetPageContentUnavailable() {
        assertFalse(wikipediaService.findByTitle(WikipediaLanguage.SPANISH, "Usuario:Benjavaleroxx").isPresent());
    }
}
