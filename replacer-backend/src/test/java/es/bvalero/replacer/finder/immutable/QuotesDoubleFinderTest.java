package es.bvalero.replacer.finder.immutable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QuotesDoubleFinderTest {

    @Test
    void testRegexDoubleQuotes() {
        String quotes1 = "\"xxx\"";
        String quotes2 = "\"yyy\n"; // Truncated with new line
        String quotes3 = "\"zzz\"";
        String quotes4 = "\"aaa"; // Truncated with end
        String text = String.format("%s %s %s %s.", quotes1, quotes2, quotes3, quotes4);

        ImmutableFinder quotesFinder = new QuotesDoubleFinder();
        List<Immutable> matches = quotesFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(quotes1, quotes3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
