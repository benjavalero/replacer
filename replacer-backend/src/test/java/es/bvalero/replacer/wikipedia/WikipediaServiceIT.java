package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.ReplacerException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@Ignore
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WikipediaServiceImpl.class, WikipediaRequestService.class, WikipediaConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class WikipediaServiceIT {

    @Autowired
    private WikipediaServiceImpl wikipediaService;

    @Test
    void testGetPageContent() throws ReplacerException {
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService.getPageByTitle(title, WikipediaLanguage.SPANISH)
                .orElseThrow(ReplacerException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(6219990, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    void testGetPagesContent() throws ReplacerException {
        // We pass a null access token to retrieve an anonymous edit token
        List<WikipediaPage> pages = wikipediaService.getPagesByIds(Arrays.asList(6219990, 6903884), WikipediaLanguage.SPANISH);
        Assert.assertNotNull(pages);
        Assert.assertEquals(2, pages.size());
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6219990));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6219990).findAny().orElseThrow(ReplacerException::new).getContent().contains("Orihuela"));
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6903884));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6903884).findAny().orElseThrow(ReplacerException::new).getContent().contains("Pais Vasco"));
    }

    @Test
    void testGetPageContentUnavailable() throws ReplacerException {
        Assert.assertFalse(wikipediaService.getPageByTitle("Usuario:Benjavaleroxx", WikipediaLanguage.SPANISH).isPresent());
    }

    @Test
    void testGetEditToken() throws ReplacerException {
        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(6903884, WikipediaLanguage.SPANISH, new OAuth1AccessToken("", ""));
        Assert.assertNotNull(editToken);
        Assert.assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        Assert.assertNotNull(editToken.getTimestamp());
    }

    @Test(expected = ReplacerException.class)
    void testSavePageWithConflicts() throws ReplacerException {
        WikipediaPage page = wikipediaService.getPageByTitle("Wikipedia:Zona de pruebas/5", WikipediaLanguage.SPANISH)
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
        wikipediaService.savePageContent(
            WikipediaLanguage.SPANISH,
            page.getId(),
            0,
            conflictContent,
            before,
            new OAuth1AccessToken("", "")
        );
    }
}
