package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.authentication.AuthenticationServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WikipediaServiceImpl.class, AuthenticationServiceImpl.class},
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
        Assert.assertTrue(page.getTimestamp().getYear() >= 2016);
        Assert.assertTrue(page.getContent().contains("Orihuela"));
    }

    @Test
    public void testGetPagesContent() throws WikipediaException {
        // We pass a null access token to retrieve an anonymous edit token
        Map<Integer, WikipediaPage> pages = wikipediaService.getPagesByIds(Arrays.asList(6219990, 6903884));
        Assert.assertNotNull(pages);
        Assert.assertEquals(2, pages.size());
        Assert.assertTrue(pages.containsKey(6219990));
        Assert.assertTrue(pages.get(6219990).getContent().contains("Orihuela"));
        Assert.assertTrue(pages.containsKey(6903884));
        Assert.assertTrue(pages.get(6903884).getContent().contains("Pais Vasco"));
    }

    @Test
    public void testGetPageContentUnavailable() throws WikipediaException {
        Assert.assertFalse(wikipediaService.getPageByTitle("Usuario:Benjavaleroxx").isPresent());
    }

}
