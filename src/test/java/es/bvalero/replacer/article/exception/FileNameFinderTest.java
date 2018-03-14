package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
        String file1 = "File:España.png";
        String file2 = "File:Águila.jpeg";
        String file3 = "File: un fichero.PDF";
        String file4 = "File: otro-fichero.gif";
        String gallery = "<gallery>\n" + file3 + "| DESC \n " + file4 + "\n</gallery>";
        String file5 = "| imagen = Otra imagen.svg";
        String template = "{{Plantilla " + file5 + "}}";

        String text = "xxx [[" + file1 + "]] / [[" + file2 + "| Águila]] / " + gallery + " / " + template + " zzz";

        FileNameFinder fileNameFinder = new FileNameFinder();

        List<RegexMatch> matches = fileNameFinder.findExceptionMatches(text, false);
        Assert.assertEquals(5, matches.size());
        Assert.assertEquals(file1, matches.get(0).getOriginalText());
        Assert.assertEquals(file2, matches.get(1).getOriginalText());
        Assert.assertEquals(file3, matches.get(2).getOriginalText());
        Assert.assertEquals(file4, matches.get(3).getOriginalText());
        Assert.assertEquals(file5, matches.get(4).getOriginalText());

        matches = fileNameFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(5, matches.size());
        Assert.assertEquals(file1, matches.get(0).getOriginalText());
        Assert.assertEquals(file2, matches.get(1).getOriginalText());
        Assert.assertEquals(file3, matches.get(2).getOriginalText());
        Assert.assertEquals(file4, matches.get(3).getOriginalText());
        Assert.assertEquals(file5, matches.get(4).getOriginalText());
    }

}
