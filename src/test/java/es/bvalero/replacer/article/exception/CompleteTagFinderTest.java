package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CompleteTagFinderTest {

    @Test
    public void testRegexCompleteTag() {
        String tag1 = "<math class=\"latex\">Un <i>ejemplo</i>\n en LaTeX</math>";
        String tag2 = "<source>Otro ejemplo</source>";
        String text = "xxx " + tag1 + " / " + tag2 + " zzz";

        CompleteTagFinder completeTagFinder = new CompleteTagFinder();

        List<RegexMatch> matches = completeTagFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(tag1, matches.get(0).getOriginalText());
        Assert.assertEquals(tag2, matches.get(1).getOriginalText());

        matches = completeTagFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(StringUtils.escapeText(tag1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(tag2), matches.get(1).getOriginalText());
    }

}
