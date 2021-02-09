package es.bvalero.replacer.finder.immutable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QuotesAngularFinderTest {

    @Test
    void testRegexQuotesAngular() {
        String quotes1 = "«yáy»";
        String quotes2 = "«z, zz»";
        String quotes3 = "«z\nz»";
        String text = String.format("%s %s %s.", quotes1, quotes2, quotes3);

        ImmutableFinder quotesFinder = new QuotesAngularFinder();
        List<Immutable> matches = quotesFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(quotes1, quotes2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}