package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuotesTypographicFinderTest {

    private QuotesTypographicFinder quotesFinder;

    @BeforeEach
    public void setUp() {
        quotesFinder = spy(new QuotesTypographicFinder());
    }

    @Test
    void testSeveralTypographicQuotes() {
        String quotes1 = "“xxx”";
        String quotes2 = "“yyy\n”"; // Truncated with new line
        String quotes3 = "“zzz”";
        String quotes4 = "“aaa"; // Truncated with end
        String text = String.format("%s %s %s %s.", quotes1, quotes2, quotes3, quotes4);

        List<Immutable> matches = quotesFinder.findList(text);

        Set<String> expected = Set.of(quotes1, quotes3);
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);

        verify(quotesFinder, times(2)).logImmutableCheck(any(FinderPage.class), anyInt(), anyInt(), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "“Text with {{template}}.”", "“\"Nested quotes\"”" })
    void testTypographicQuotes(String text) {
        List<Immutable> matches = quotesFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).text());
    }

    @ParameterizedTest
    @ValueSource(strings = { "“ ”" })
    void testTypographicQuotesNonValid(String text) {
        List<Immutable> matches = quotesFinder.findList(text);

        assertTrue(matches.isEmpty());
        verify(quotesFinder).logImmutableCheck(any(FinderPage.class), anyInt(), anyInt(), anyString());
    }
}
