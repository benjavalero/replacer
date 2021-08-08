package es.bvalero.replacer.wikipedia.api;

import es.bvalero.replacer.common.DateUtils;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.wikipedia.OAuthToken;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.oauth.OAuthMediaWikiConfiguration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaApiService.class,
        WikipediaRequestService.class,
        OAuthMediaWikiConfiguration.class,
        XmlConfiguration.class,
    }
)
class WikipediaServiceIT {

    @Autowired
    private WikipediaApiService wikipediaService;

    @Test
    void testGetPageContent() throws ReplacerException {
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService
            .getPageByTitle(WikipediaLanguage.SPANISH, title)
            .orElseThrow(ReplacerException::new);
        Assertions.assertNotNull(page);
        Assertions.assertEquals(6219990, page.getId());
        Assertions.assertEquals(title, page.getTitle());
        Assertions.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assertions.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assertions.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPagesContent() throws ReplacerException {
        // We pass a null access token to retrieve an anonymous edit token
        List<WikipediaPage> pages = wikipediaService.getPagesByIds(
            Arrays.asList(6219990, 6903884),
            WikipediaLanguage.SPANISH
        );
        Assertions.assertNotNull(pages);
        Assertions.assertEquals(2, pages.size());
        Assertions.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6219990));
        Assertions.assertTrue(
            pages
                .stream()
                .filter(page -> page.getId() == 6219990)
                .findAny()
                .orElseThrow(ReplacerException::new)
                .getContent()
                .contains("Orihuela")
        );
        Assertions.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6903884));
        Assertions.assertTrue(
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
        Assertions.assertFalse(
            wikipediaService.getPageByTitle(WikipediaLanguage.SPANISH, "Usuario:Benjavaleroxx").isPresent()
        );
    }

    @Test
    void testGetEditToken() throws ReplacerException {
        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(6903884, WikipediaLanguage.SPANISH, OAuthToken.ofEmpty());
        Assertions.assertNotNull(editToken);
        Assertions.assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        Assertions.assertNotNull(editToken.getTimestamp());
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

        Assertions.assertThrows(
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
