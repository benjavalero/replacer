package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Set<String> expected = new HashSet<>(Arrays.asList(link1, link2, link4));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
