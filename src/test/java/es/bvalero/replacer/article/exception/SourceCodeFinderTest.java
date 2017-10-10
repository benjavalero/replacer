package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SourceCodeFinderTest {

    @Test
    public void testRegexTagMath() {
        String source = "<math>Un <i>ejemplo</i>\n en LaTeX</math>";
        String text = "xxx " + source + " zzz";

        SourceCodeFinder sourceCodeFinder = new SourceCodeFinder();
        List<RegexMatch> matches = sourceCodeFinder.findErrorExceptions(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(source, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexTagSource() {
        String source = "<source>Un <i>ejemplo</i>\n en LaTeX</source>";
        String text = "xxx " + source + " zzz";

        SourceCodeFinder sourceCodeFinder = new SourceCodeFinder();
        List<RegexMatch> matches = sourceCodeFinder.findErrorExceptions(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(source, matches.get(0).getOriginalText());
    }

}
