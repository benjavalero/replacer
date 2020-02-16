package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class QuotesAngularFinderTest {

    @Test
    public void testRegexQuotesAngular() {
        String quotes1 = "«yáy»";
        String quotes2 = "«z, zz»";
        String quotes3 = "«z\nz»";
        String text = String.format("%s %s %s.", quotes1, quotes2, quotes3);

        ImmutableFinder quotesFinder = new QuotesAngularFinder();
        List<Immutable> matches = quotesFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(quotes1, quotes2, quotes3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }
}
