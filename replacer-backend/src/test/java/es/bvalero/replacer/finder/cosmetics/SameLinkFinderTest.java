package es.bvalero.replacer.finder.cosmetics;

import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.CosmeticFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class SameLinkFinderTest {

    @Test
    public void testSameLinkFinder() {
        String link1 = "[[test|test]]";
        String link2 = "[[Test|test]]";
        String link3 = "[[test|Test]]";
        String link4 = "[[Test|Test]]";
        String link5 = "[[Test|Mock]]";
        String text = String.format("En %s %s %s %s %s.", link1, link2, link3, link4, link5);

        CosmeticFinder sameLinkFinder = new SameLinkFinder();
        List<Cosmetic> matches = sameLinkFinder.findList(text);

        Set<String> expectedMatches = new HashSet<>(Arrays.asList(link1, link2, link4));
        Set<String> actualMatches = matches.stream().map(Cosmetic::getText).collect(Collectors.toSet());
        Assert.assertEquals(expectedMatches, actualMatches);

        String fix1 = "[[test]]";
        String fix2 = "[[Test]]";
        Set<String> expectedFixes = new HashSet<>(Arrays.asList(fix1, fix2));
        Set<String> actualFixes = matches.stream().map(Cosmetic::getFix).collect(Collectors.toSet());
        Assert.assertEquals(expectedFixes, actualFixes);
    }
}
