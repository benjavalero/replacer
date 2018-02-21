package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SourceCodeFinderTest {

    @Test
    public void testRegexTagMath() {
        String source = "<math>Un <i>ejemplo</i>\n en LaTeX</math>";
        String text = "xxx " + source + " zzz";

        SourceCodeMatchFinder sourceCodeFinder = new SourceCodeMatchFinder();
        List<RegexMatch> matches = sourceCodeFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(source, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexTagMathEscaped() {
        String source = "<math>Un <i>ejemplo</i>\n en LaTeX</math>";
        String text = "xxx " + source + " zzz";

        SourceCodeMatchFinder sourceCodeFinder = new SourceCodeMatchFinder();
        List<RegexMatch> matches = sourceCodeFinder.findExceptionMatches(StringUtils.escapeText(text));

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(source), matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexTagSource() {
        String source = "<source>Un <i>ejemplo</i>\n en LaTeX</source>";
        String text = "xxx " + source + " zzz";

        SourceCodeMatchFinder sourceCodeFinder = new SourceCodeMatchFinder();
        List<RegexMatch> matches = sourceCodeFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(source, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexTagSourceWithAttributes() {
        String source = "<source lang=\"python\">Un <i>ejemplo</i>\n en LaTeX</source>";
        String text = "xxx " + source + " zzz";

        SourceCodeMatchFinder sourceCodeFinder = new SourceCodeMatchFinder();
        List<RegexMatch> matches = sourceCodeFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(source, matches.get(0).getOriginalText());
    }

}
