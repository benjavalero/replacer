package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

        Set<String> expected = Set.of(quotes1, quotes3);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"«Nested quotes»\"", "param=\" \"" })
    void testValidQuotes(String text) {
        ImmutableFinder quotesFinder = new QuotesDoubleFinder();
        List<Immutable> matches = quotesFinder.findList(text);

        assertFalse(matches.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "En \" \".", "\"Text with {{template}}.\"" })
    void testInvalidQuotes(String text) {
        ImmutableFinder quotesFinder = new QuotesDoubleFinder();
        List<Immutable> matches = quotesFinder.findList(text);

        assertTrue(matches.isEmpty());
    }
}
