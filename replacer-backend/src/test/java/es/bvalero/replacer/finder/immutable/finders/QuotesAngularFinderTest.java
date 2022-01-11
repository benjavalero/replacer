package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class QuotesAngularFinderTest {

    @Test
    void testRegexQuotesAngular() {
        String quotes1 = "«xxx»";
        String quotes2 = "«yyy\n"; // Truncated with new line
        String quotes3 = "«zzz»";
        String quotes4 = "«aaa"; // Truncated with end
        String text = String.format("%s %s %s %s.", quotes1, quotes2, quotes3, quotes4);

        ImmutableFinder quotesFinder = new QuotesAngularFinder();
        List<Immutable> matches = quotesFinder.findList(text);

        Set<String> expected = Set.of(quotes1, quotes3);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
