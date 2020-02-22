package es.bvalero.replacer.finder.benchmark.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DomainFinderTest {
    private String text;
    private Set<String> expected;

    @Before
    public void setUp() {
        String domain1 = "IMDb.org";
        String domain2 = "es.wikipedia.org";
        String domain3 = "www.acb.es";
        this.text = String.format("Entre %s, %s http://%s.", domain1, domain2, domain3);

        this.expected = new HashSet<>(Arrays.asList(domain1, domain2));
    }

    @Test
    public void testDomainRegexFinder() {
        DomainFinder finder = new DomainRegexFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainAutomatonFinder() {
        DomainFinder finder = new DomainAutomatonFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainLinearFinder() {
        DomainFinder finder = new DomainLinearFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }
}
