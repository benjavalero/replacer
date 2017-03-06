package es.bvalero.replacer.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WikipediaService.class, initializers = ConfigFileApplicationContextInitializer.class)
public class WikipediaServiceIT {

    @Autowired
    private WikipediaService wikipediaService;

    @Test
    public void testGetArticleContent() {
        String articleContent = wikipediaService.getArticleContent("Ñu (desambiguación)");
        assertFalse(StringUtils.isEmpty(articleContent));
        assertTrue(articleContent.contains("África"));
    }

    @Test
    public void testGetNonExistingArticleContent() {
        String articleContent = wikipediaService.getArticleContent("jander");
        assertNull(articleContent);
    }

    @Test
    public void testEditContent() {
        String title = "Usuario:Benjavalero/Taller";
        String content = wikipediaService.getArticleContent(title);
        assertFalse(StringUtils.isEmpty(content));

        String edited = content + "xxxxx";
        wikipediaService.editArticleContent(title, edited, "Test edit");

        String contentEdited = wikipediaService.getArticleContent(title);
        assertTrue(contentEdited.endsWith("xxxxx"));

        wikipediaService.editArticleContent(title, content, "Revert test edit");
        String contentReverted = wikipediaService.getArticleContent(title);
        assertEquals(content, contentReverted);
    }

}
