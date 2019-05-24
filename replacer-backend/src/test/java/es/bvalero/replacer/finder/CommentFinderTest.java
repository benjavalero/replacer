package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CommentFinderTest {

    @Test
    public void testRegexComment() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        String text = String.format("%s %s", comment1, comment2);

        IgnoredReplacementFinder commentFinder = new CommentFinder();

        List<MatchResult> matches = commentFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(comment1, matches.get(0).getText());
        Assert.assertEquals(comment2, matches.get(1).getText());
    }

}
