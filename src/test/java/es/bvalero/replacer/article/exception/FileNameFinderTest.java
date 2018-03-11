package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
        String text = "[[File: de_españa.png | España]] {{ X | co-co.svg | a = pepe.pdf }}";

        FileNameFinder fileNameFinder = new FileNameFinder();

        List<RegexMatch> matches = fileNameFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertTrue(matches.contains(new RegexMatch(7, " de_españa.png")));
        Assert.assertTrue(matches.contains(new RegexMatch(39, " co-co.svg")));
        Assert.assertTrue(matches.contains(new RegexMatch(55, " pepe.pdf")));

        matches = fileNameFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertTrue(matches.contains(new RegexMatch(7, " de_españa.png")));
        Assert.assertTrue(matches.contains(new RegexMatch(39, " co-co.svg")));
        Assert.assertTrue(matches.contains(new RegexMatch(55, " pepe.pdf")));
    }

}
