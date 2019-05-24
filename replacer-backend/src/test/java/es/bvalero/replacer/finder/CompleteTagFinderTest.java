package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CompleteTagFinderTest {

    @Test
    public void testRegexCompleteTag() {
        String tag1 = "<math class=\"latex\">Un <i>ejemplo</i>\n en LaTeX</math>";
        String tag2 = "<math>Otro ejemplo</math>";
        String tag3 = "<source>Otro ejemplo</source>";
        String text = String.format("%s %s %s", tag1, tag2, tag3);

        IgnoredReplacementFinder completeTagFinder = new CompleteTagFinder();

        List<MatchResult> matches = completeTagFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(tag1, matches.get(0).getText());
        Assert.assertEquals(tag2, matches.get(1).getText());
        Assert.assertEquals(tag3, matches.get(2).getText());
    }

}
