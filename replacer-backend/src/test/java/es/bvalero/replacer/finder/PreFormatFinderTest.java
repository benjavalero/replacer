package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PreFormatFinderTest {

    @Test
    public void testPreFormat() {
        String text1 = "Text1 Text1";
        String text2 = " Text2 Text2";
        String text3 = "Text3 Text3";
        String text = String.format("%s\n%s\n%s", text1, text2, text3);

        IgnoredReplacementFinder preFormatFinder = new PreFormatFinder();

        List<MatchResult> matches = preFormatFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(text2, matches.get(0).getText());
    }

}
