package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.JsonMapperConfiguration;
import es.bvalero.replacer.MediaWikiApiConfiguration;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequestHelper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaPageApiRepository.class,
        WikipediaApiRequestHelper.class,
        MediaWikiApiConfiguration.class,
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
        assertTrue(page.getLastUpdate().getYear() >= 2016);
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

    @Test
    void testGetEditToken() throws WikipediaException {
        // We pass an empty access token to retrieve an anonymous edit token
        PageKey pageKey = PageKey.of(WikipediaLanguage.SPANISH, 6903884);
        EditToken editToken = wikipediaService.getEditToken(pageKey, AccessToken.empty());
        assertNotNull(editToken);
        assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        assertNotNull(editToken.getTimestamp());
    }

    @Test
    void testSavePageWithConflicts() throws WikipediaException {
        WikipediaPage page = wikipediaService
            .findByTitle(WikipediaLanguage.SPANISH, "Wikipedia:Zona de pruebas/5")
            .orElseThrow(WikipediaException::new);

        String originalContent = page.getContent();
        String newContent = originalContent + "\nEdición sencilla para probar conflictos de edición.";
        String conflictContent = originalContent + "\nOtra edición sencilla para probar conflictos de edición.";

        // Save the new content
        WikipediaPageSave pageSave = WikipediaPageSave
            .builder()
            .pageKey(page.getPageKey())
            .content(newContent)
            .editSummary("Replacer Integration Test")
            .queryTimestamp(page.getQueryTimestamp())
            .accessToken(AccessToken.empty())
            .build();
        wikipediaService.save(pageSave);

        // Save the conflict content started 1 day before
        LocalDateTime before = page.getQueryTimestamp().minusDays(1);
        WikipediaPageSave pageConflictSave = WikipediaPageSave
            .builder()
            .pageKey(page.getPageKey())
            .content(conflictContent)
            .editSummary("Replacer Integration Test")
            .queryTimestamp(before)
            .accessToken(AccessToken.empty())
            .build();

        assertThrows(WikipediaException.class, () -> wikipediaService.save(pageConflictSave));
    }
}
