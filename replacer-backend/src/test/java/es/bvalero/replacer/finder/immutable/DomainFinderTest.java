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

public class DomainFinderTest {

    @Test
    public void testRegexFileName() {
        String domain1 = "IMDb.org";
        String domain2 = "es.wikipedia.org";
        String domain3 = "www.acb.es";
        String text = String.format("En %s, %s http://%s.", domain1, domain2, domain3);

        ImmutableFinder domainFinder = new DomainFinder();
        List<Immutable> matches = domainFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(domain1, domain2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }
}
