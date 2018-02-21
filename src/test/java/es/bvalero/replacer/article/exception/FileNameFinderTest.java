package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
        String text = "[[File: de_españa.png | España]] {{ X | co-co.svg | a = pepe.pdf }}";

        FileNameMatchFinder fileNameFinder = new FileNameMatchFinder();
        List<RegexMatch> matches = fileNameFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertTrue(matches.contains(new RegexMatch(7, " de_españa.png")));
        Assert.assertTrue(matches.contains(new RegexMatch(39, " co-co.svg")));
        Assert.assertTrue(matches.contains(new RegexMatch(55, " pepe.pdf")));
    }

}
