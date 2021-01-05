package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.TestConfiguration;
import es.bvalero.replacer.XmlConfiguration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        WikipediaServiceImpl.class,
        WikipediaRequestService.class,
        WikipediaConfig.class,
        XmlConfiguration.class,
        TestConfiguration.class,
    }
)
class WikipediaServiceIT {

    @Autowired
    private WikipediaServiceImpl wikipediaService;

    @Test
    void testGetPageContent() throws ReplacerException {
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService
            .getPageByTitle(title, WikipediaLanguage.SPANISH)
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
            wikipediaService.getPageByTitle("Usuario:Benjavaleroxx", WikipediaLanguage.SPANISH).isPresent()
        );
    }

    @Test
    void testGetEditToken() throws ReplacerException {
        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(
            6903884,
            WikipediaLanguage.SPANISH,
            new OAuth1AccessToken("", "")
        );
        Assertions.assertNotNull(editToken);
        Assertions.assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        Assertions.assertNotNull(editToken.getTimestamp());
    }

    @Test
    void testSavePageWithConflicts() throws ReplacerException {
        WikipediaPage page = wikipediaService
            .getPageByTitle("Wikipedia:Zona de pruebas/5", WikipediaLanguage.SPANISH)
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
            new OAuth1AccessToken("", "")
        );

        // Save the conflict content started 1 day before
        String before = WikipediaPage.formatWikipediaTimestamp(
            WikipediaPage.parseWikipediaTimestamp(page.getQueryTimestamp()).atTime(0, 0).minusDays(1)
        );

        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                wikipediaService.savePageContent(
                    WikipediaLanguage.SPANISH,
                    page.getId(),
                    0,
                    conflictContent,
                    before,
                    new OAuth1AccessToken("", "")
                )
        );
    }
}
