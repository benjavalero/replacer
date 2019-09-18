package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DomainFinderTest {

    @Test
    public void testRegexFileName() {
        String domain1 = "IMDb.org";
        String domain2 = "www.space.info";
        String domain3 = "www.acb.es";
        String text = String.format("En %s %s http://%s.", domain1, domain2, domain3);

        IgnoredReplacementFinder domainFinder = new DomainFinder();
        List<MatchResult> matches = domainFinder.findIgnoredReplacements(text);

        Assert.assertEquals(Arrays.asList(domain1, domain2, domain3),
                matches.stream().map(MatchResult::getText).collect(Collectors.toList()));
    }

}
