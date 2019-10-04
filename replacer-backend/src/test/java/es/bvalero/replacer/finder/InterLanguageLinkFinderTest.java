package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InterLanguageLinkFinderTest {

    @Test
    public void testRegexInterLanguageLink() {
        String link1 = "[[:pt:Title]]";
        String link2 = "[[:en:Title|Alias]]";
        String link3 = "[[Text]]";
        String link4 = "[[fr:Title]]";
        String text = String.format("En %s %s %s %s.", link1, link2, link3, link4);

        IgnoredReplacementFinder interLanguageLinkFinder = new InterLanguageLinkFinder();
        List<IgnoredReplacement> matches = interLanguageLinkFinder.findIgnoredReplacements(text);

        Assert.assertEquals(Arrays.asList(link1, link2, link4),
                matches.stream().map(IgnoredReplacement::getText).collect(Collectors.toList()));
    }

}
