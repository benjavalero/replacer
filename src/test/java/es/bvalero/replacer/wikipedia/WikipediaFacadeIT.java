package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WikipediaFacade.class, initializers = ConfigFileApplicationContextInitializer.class)
public class WikipediaFacadeIT {

    @Autowired
    private WikipediaFacade wikipediaFacade;

    @Test
    public void testGetArticleContent() throws WikipediaException {
        String articleContent = wikipediaFacade.getArticleContent("Ñu (desambiguación)");

        Assert.assertFalse(StringUtils.isEmpty(articleContent));
        Assert.assertTrue(articleContent.contains("África"));
    }

    @Test(expected = WikipediaException.class)
    public void testGetNonExistingArticleContent() throws WikipediaException {
        wikipediaFacade.getArticleContent("jander");
    }

    @Test(expected = WikipediaException.class)
    public void testDeletedArticleContent() throws WikipediaException {
        wikipediaFacade.getArticleContent("Gigia Talarico");
    }

    public void testEditContent() throws WikipediaException {
        String title = "Usuario:Benjavalero/Taller";
        String content = wikipediaFacade.getArticleContent(title);
        Assert.assertFalse(StringUtils.isEmpty(content));

        String edited = content + "xxxxx";
        wikipediaFacade.editArticleContent(title, edited, "Test edit");

        String contentEdited = wikipediaFacade.getArticleContent(title);
        Assert.assertTrue(contentEdited.endsWith("xxxxx"));

        wikipediaFacade.editArticleContent(title, content, "Revert test edit");
        String contentReverted = wikipediaFacade.getArticleContent(title);
        Assert.assertEquals(content, contentReverted);
    }

}
