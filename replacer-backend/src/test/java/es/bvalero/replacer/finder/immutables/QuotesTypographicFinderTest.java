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

public class QuotesTypographicFinderTest {

    @Test
    public void testRegexQuotesTypographic() {
        String quotes1 = "“yáy”";
        String quotes2 = "“z, zz”";
        String quotes3 = "“z\nz”";
        String text = String.format("%s %s %s.", quotes1, quotes2, quotes3);

        ImmutableFinder quotesFinder = new QuotesTypographicFinder();
        List<Immutable> matches = quotesFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(quotes1, quotes2, quotes3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
