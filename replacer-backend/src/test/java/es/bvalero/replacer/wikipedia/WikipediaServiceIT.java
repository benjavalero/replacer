package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.authentication.AuthenticationConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WikipediaServiceImpl.class, AuthenticationConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class WikipediaServiceIT {

    @Autowired
    private WikipediaServiceImpl wikipediaService;

    @Test
    public void testGetPageContent() throws WikipediaException {
        String title = "Usuario:Benjavalero";
        WikipediaPage page = wikipediaService.getPageByTitle(title)
                .orElseThrow(WikipediaException::new);
        Assert.assertNotNull(page);
        Assert.assertEquals(6219990, page.getId());
        Assert.assertEquals(title, page.getTitle());
        Assert.assertEquals(WikipediaNamespace.USER, page.getNamespace());
        Assert.assertTrue(page.getLastUpdate().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    public void testGetPagesContent() throws WikipediaException {
        // We pass a null access token to retrieve an anonymous edit token
        List<WikipediaPage> pages = wikipediaService.getPagesByIds(Arrays.asList(6219990, 6903884));
        Assert.assertNotNull(pages);
        Assert.assertEquals(2, pages.size());
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6219990));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6219990).findAny().orElseThrow(WikipediaException::new).getContent().contains("Orihuela"));
        Assert.assertTrue(pages.stream().anyMatch(page -> page.getId() == 6903884));
        Assert.assertTrue(pages.stream().filter(page -> page.getId() == 6903884).findAny().orElseThrow(WikipediaException::new).getContent().contains("Pais Vasco"));
    }

    @Test
    public void testGetPageContentUnavailable() throws WikipediaException {
        Assert.assertFalse(wikipediaService.getPageByTitle("Usuario:Benjavaleroxx").isPresent());
    }

    @Test
    public void testGetEditToken() throws WikipediaException {
        // We pass a null access token to retrieve an anonymous edit token
        EditToken editToken = wikipediaService.getEditToken(6903884, new OAuth1AccessToken("", ""));
        Assert.assertNotNull(editToken);
        Assert.assertTrue(editToken.getCsrfToken().endsWith("+\\"));
        Assert.assertNotNull(editToken.getTimestamp());
    }

    @Test(expected = WikipediaException.class)
    public void testSavePageWithConflicts() throws WikipediaException {
        WikipediaPage page = wikipediaService.getPageByTitle("Wikipedia:Zona de pruebas/5")
                .orElseThrow(WikipediaException::new);

        String originalContent = page.getContent();
        String newContent = originalContent + "\nEdición sencilla para probar conflictos de edición.";
        String conflictContent = originalContent + "\nOtra edición sencilla para probar conflictos de edición.";

        // Save the new content
        wikipediaService.savePageContent(page.getId(),
                newContent,
                0,
                page.getQueryTimestamp(),
                new OAuth1AccessToken("", ""));

        // Save the conflict content started 1 minute before
        String before = WikipediaPage.formatWikipediaTimestamp(
                WikipediaPage.parseWikipediaTimestamp(
                        page.getQueryTimestamp()).minusMinutes(1));
        wikipediaService.savePageContent(page.getId(),
                conflictContent,
                0,
                before,
                new OAuth1AccessToken("", ""));
    }

}
