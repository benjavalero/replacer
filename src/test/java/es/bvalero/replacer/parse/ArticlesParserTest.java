package es.bvalero.replacer.parse;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ArticlesParserTest {

    @Test
    public void testParseBz2() {
        String bz2Path = getClass().getResource("/pages-articles.xml.bz2").getFile();
        ArticlesParser parser = new ArticlesParser();
        ArticlesHandler handler = Mockito.mock(ArticlesHandler.class);
        boolean success = parser.parse(bz2Path, handler);
        Assert.assertTrue(success);
    }

    @Test
    public void testParseNonExistingBz2() {
        ArticlesParser parser = new ArticlesParser();
        ArticlesHandler handler = Mockito.mock(ArticlesHandler.class);
        boolean success = parser.parse("xxxx.bz2", handler);
        Assert.assertFalse(success);
    }

}
