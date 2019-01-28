package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CompleteTagFinderTest {

    @Test
    public void testRegexCompleteTag() {
        String tag1 = "<math class=\"latex\">Un <i>ejemplo</i>\n en LaTeX</math>";
        String tag2 = "<math>Otro ejemplo</math>";
        String tag3 = "<source>Otro ejemplo</source>";
        String text = "xxx " + tag1 + " / " + tag2 + " / " + tag3 + " zzz";

        IgnoredReplacementFinder completeTagFinder = new CompleteTagFinder();

        List<ArticleReplacement> matches = completeTagFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(tag1, matches.get(0).getText());
        Assert.assertEquals(tag2, matches.get(1).getText());
        Assert.assertEquals(tag3, matches.get(2).getText());
    }

}
