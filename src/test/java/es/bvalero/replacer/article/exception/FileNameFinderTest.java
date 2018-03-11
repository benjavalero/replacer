package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
        String file1 = "File: de_españa.png";
        String file2 = "| co-co.jpeg";
        String file3 = "= pepe.PDF";
        String text = "[[" + file1 + "]] {{ X" + file2 + "| a " + file3 + "}}";

        FileNameFinder fileNameFinder = new FileNameFinder();

        List<RegexMatch> matches = fileNameFinder.findExceptionMatches(text, false);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(file1, matches.get(0).getOriginalText());
        Assert.assertEquals(file2, matches.get(1).getOriginalText());
        Assert.assertEquals(file3, matches.get(2).getOriginalText());

        matches = fileNameFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(file1, matches.get(0).getOriginalText());
        Assert.assertEquals(file2, matches.get(1).getOriginalText());
        Assert.assertEquals(file3, matches.get(2).getOriginalText());
    }

}
