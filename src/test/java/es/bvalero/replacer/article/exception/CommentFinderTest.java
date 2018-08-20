package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CommentFinderTest {

    @Test
    public void testRegexComment() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        String text = "xxx " + comment1 + " / " + comment2 + " zzz";

        CommentFinder commentFinder = new CommentFinder();

        List<RegexMatch> matches = commentFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(comment1, matches.get(0).getOriginalText());
        Assert.assertEquals(comment2, matches.get(1).getOriginalText());

        matches = commentFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(StringUtils.escapeText(comment1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(comment2), matches.get(1).getOriginalText());
    }

}
