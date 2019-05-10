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
    public void testGetEditToken() throws WikipediaException {
        // We pass a null access token to retrieve an anonymous edit token
        String editToken = wikipediaService.getEditToken(null);
        Assert.assertNotNull(editToken);
        Assert.assertTrue(editToken.endsWith("+\\"));
    }

    @Test
    public void testGetPageContent() throws WikipediaException {
        String pageContent = wikipediaService.getPageContent("Usuario:Benjavalero");
        Assert.assertNotNull(pageContent);
        Assert.assertTrue(pageContent.contains("Orihuela"));
    }

    @Test
    public void testGetPagesContent() throws WikipediaException {
        // We pass a null access token to retrieve an anonymous edit token
        Map<Integer, String> pagesContent = wikipediaService.getPagesContent(Arrays.asList(6219990, 6903884), null);
        Assert.assertNotNull(pagesContent);
        Assert.assertEquals(2, pagesContent.size());
        Assert.assertTrue(pagesContent.containsKey(6219990));
        Assert.assertTrue(pagesContent.get(6219990).contains("Orihuela"));
        Assert.assertTrue(pagesContent.containsKey(6903884));
        Assert.assertTrue(pagesContent.get(6903884).contains("Pais Vasco"));
    }

    @Test(expected = UnavailablePageException.class)
    public void testGetPageContentUnavailable() throws WikipediaException {
        wikipediaService.getPageContent("Usuario:Benjavaleroxx");
    }

}
