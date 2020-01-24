package es.bvalero.replacer.finder;

import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class DomainFinderTest {

    @Test
    public void testRegexFileName() {
        String domain1 = "IMDb.org";
        String domain2 = "www.space.info";
        String domain3 = "www.acb.es";
        String text = String.format("En %s %s http://%s.", domain1, domain2, domain3);

        ImmutableFinder domainFinder = new DomainFinder();
        List<Immutable> matches = domainFinder.findList(text);

        Assert.assertEquals(
            Arrays.asList(domain1, domain2, domain3),
            matches.stream().map(Immutable::getText).collect(Collectors.toList())
        );
    }
}
