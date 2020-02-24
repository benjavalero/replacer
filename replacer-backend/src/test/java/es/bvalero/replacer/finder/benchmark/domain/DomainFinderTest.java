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

    String domain1 = "IMDb.org";
    String domain2 = "es.wikipedia.org"; // To be captured at least the end
    String domain3 = "acb.es";
    String domain4 = "www.domain.com/index.php"; // Not to be captured unless it gives best performance;

    @Before
    public void setUp() {
        this.text = String.format("Entre %s, %s {{=%s}} [http://%s]", domain1, domain2, domain3, domain4);
        this.expected = new HashSet<>(Arrays.asList(domain1, domain2, domain3));
    }

    @Test
    public void testDomainAutomatonFinder() {
        DomainFinder finder = new DomainAutomatonFinder();
        Set<String> expected = new HashSet<>(Arrays.asList(domain1, domain2, domain3, "www.domain.com", "index.php"));
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainAutomatonPrefixFinder() {
        DomainFinder finder = new DomainAutomatonPrefixFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainAutomatonSuffixFinder() {
        DomainFinder finder = new DomainAutomatonSuffixFinder();
        Set<String> expected = new HashSet<>(Arrays.asList(domain1, domain2, domain3, "www.domain.com"));
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainAutomatonPrefixSuffixFinder() {
        DomainFinder finder = new DomainAutomatonPrefixSuffixFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainLinearFinder() {
        DomainFinder finder = new DomainLinearFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainLinearSuffixFinder() {
        DomainFinder finder = new DomainLinearSuffixFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testDomainLinearSuffixListFinder() {
        DomainFinder finder = new DomainLinearSuffixListFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }
}
