package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LinkSuffixedFinderTest {

    @Test
    public void testRegexUrl() {
        String suffixed = "[[brasil]]eño";
        String text = "xxx " + suffixed + " zzz";

        IgnoredReplacementFinder linkSuffixedFinder = new LinkSuffixedFinder();

        List<ArticleReplacement> matches = linkSuffixedFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(suffixed, matches.get(0).getText());
    }

}
