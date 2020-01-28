package es.bvalero.replacer.finder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class InterLanguageLinkFinderTest {

    @Test
    public void testRegexInterLanguageLink() {
        String link1 = "[[:pt:Title]]";
        String link2 = "[[:en:Title|Alias]]";
        String link3 = "[[Text]]";
        String link4 = "[[fr:Title]]";
        String text = String.format("En %s %s %s %s.", link1, link2, link3, link4);

        ImmutableFinder interLanguageLinkFinder = new InterLanguageLinkFinder();
        List<Immutable> matches = interLanguageLinkFinder.findList(text);

        Assert.assertEquals(
            Arrays.asList(link1, link2, link4),
            matches.stream().map(Immutable::getText).collect(Collectors.toList())
        );
    }
}
