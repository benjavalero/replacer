package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
        String file1 = "España.png";
        String file2 = "Águila.jpeg";
        String file3 = "un fichero.pdf";
        String file4 = "otro-fichero.gif";
        String gallery = "<gallery>\n" + file3 + "| DESC\nFile: " + file4 + "\n</gallery>";
        String file5 = "Otra imagen.svg";
        String file6 = "Una más.jpg";
        String template = "{{Plantilla | imagen = " + file5 + " | " + file6 + "}}";

        String text = "xxx [[File:" + file1 + "]] / [[Archivo:" + file2 + "| Águila]] / " + gallery + " / " + template + " zzz";

        IgnoredReplacementFinder fileNameFinder = new FileNameFinder();

        List<ArticleReplacement> matches = fileNameFinder.findIgnoredReplacements(text);
        Assert.assertEquals(6, matches.size());
        Assert.assertEquals(file1, matches.get(0).getText());
        Assert.assertEquals(file2, matches.get(1).getText());
        Assert.assertEquals(file4, matches.get(2).getText());
        Assert.assertEquals(file3, matches.get(3).getText());
        Assert.assertEquals(file5, matches.get(4).getText());
        Assert.assertEquals(file6, matches.get(5).getText());
    }

}
