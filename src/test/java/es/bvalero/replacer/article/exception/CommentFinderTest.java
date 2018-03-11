package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CommentFinderTest {

    @Test
    public void testRegexComment() {
        String comment = "<!-- Esto es un \n comentario -->";
        String text = "xxx " + comment + " zzz";

        CommentFinder commentFinder = new CommentFinder();
        List<RegexMatch> matches = commentFinder.findExceptionMatches(text, false);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(comment, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexCommentWithTagsInside() {
        String comment = "<!-- Esto <span>es</span> un \n comentario -->";
        String text = "xxx " + comment + " zzz";

        CommentFinder commentFinder = new CommentFinder();
        List<RegexMatch> matches = commentFinder.findExceptionMatches(text, false);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(comment, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexCommentEscaped() {
        String comment = "<!-- Esto es un \n comentario -->";
        String text = "xxx " + comment + " zzz";

        CommentFinder commentFinder = new CommentFinder();
        List<RegexMatch> matches = commentFinder.findExceptionMatches(StringUtils.escapeText(text), true);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(comment), matches.get(0).getOriginalText());
    }

}
