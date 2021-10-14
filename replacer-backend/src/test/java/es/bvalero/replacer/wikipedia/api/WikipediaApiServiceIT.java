package es.bvalero.replacer.wikipedia.api;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaApiService.class,
        WikipediaApiRequestHelper.class,
        MediaWikiApiConfiguration.class,
        JsonMapperConfiguration.class,
    }
)
class WikipediaApiServiceIT {

    @Autowired
    private WikipediaApiService wikipediaService;

    @Test
    void testGetPageContent() throws ReplacerException {
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService
            .getPageByTitle(WikipediaLanguage.SPANISH, title)
            .orElseThrow(ReplacerException::new);
        assertNotNull(page);
        assertEquals(6219990, page.getId());
        assertEquals(title, page.getTitle());
        assertEquals(WikipediaNamespace.USER, page.getNamespace());
        assertTrue(page.getLastUpdate().getYear() >= 2016);
        assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPagesContent() throws ReplacerException {
        // We pass a null access token to retrieve an anonymous edit token
        List<WikipediaPage> pages = wikipediaService.getPagesByIds(
            Arrays.asList(6219990, 6903884),
            WikipediaLanguage.SPANISH
        );
        assertNotNull(pages);
        assertEquals(2, pages.size());
        assertTrue(pages.stream().anyMatch(page -> page.getId() == 6219990));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getId() == 6219990)
                .findAny()
                .orElseThrow(ReplacerException::new)
                .getContent()
                .contains("Orihuela")
        );
        assertTrue(pages.stream().anyMatch(page -> page.getId() == 6903884));
        assertTrue(
            pages
                .stream()
                .filter(page -> page.getId() == 6903884)
                .findAny()
                .orElseThrow(ReplacerException::new)
                .getContent()
                .contains("Pais Vasco")
        );
    }

    @Test
    void testGetPageContentUnavailable() throws ReplacerException {
        assertFalse(wikipediaService.getPageByTitle(WikipediaLanguage.SPANISH, "Usuario:Benjavaleroxx").isPresent());
    }

    @Test
    void testGetEditToken() throws ReplacerException {
        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(6903884, WikipediaLanguage.SPANISH, OAuthToken.ofEmpty());
        assertNotNull(editToken);
        assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        assertNotNull(editToken.getTimestamp());
    }

    @Test
    void testSavePageWithConflicts() throws ReplacerException {
        WikipediaPage page = wikipediaService
            .getPageByTitle(WikipediaLanguage.SPANISH, "Wikipedia:Zona de pruebas/5")
            .orElseThrow(ReplacerException::new);

        String originalContent = page.getContent();
        String newContent = originalContent + "\nEdición sencilla para probar conflictos de edición.";
        String conflictContent = originalContent + "\nOtra edición sencilla para probar conflictos de edición.";

        // Save the new content
        wikipediaService.savePageContent(
            WikipediaLanguage.SPANISH,
            page.getId(),
            0,
            newContent,
            page.getQueryTimestamp(),
            "Replacer Integration Test",
            OAuthToken.ofEmpty()
        );

        // Save the conflict content started 1 day before
        LocalDateTime before = page.getQueryTimestamp().minusDays(1);

        assertThrows(
            ReplacerException.class,
            () ->
                wikipediaService.savePageContent(
                    WikipediaLanguage.SPANISH,
                    page.getId(),
                    0,
                    conflictContent,
                    before,
                    "Replacer Integration Test",
                    OAuthToken.ofEmpty()
                )
        );
    }
}
