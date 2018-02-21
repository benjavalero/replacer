package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FalsePositiveFinderTest {

    @Test
    public void testLoadFalsePositives() {
        FalsePositiveMatchFinder falsePositiveFinder = new FalsePositiveMatchFinder();
        List<String> falsePositives = falsePositiveFinder.loadFalsePositives();
        Assert.assertFalse(falsePositives.isEmpty());
        Assert.assertTrue(falsePositives.contains("Index"));
        Assert.assertTrue(falsePositives.contains("Magazine"));
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un Link de Éstas en el Index Online de ésta Tropicos.org Aquél aquéllo Saint Martin.";

        FalsePositiveMatchFinder falsePositiveFinder = new FalsePositiveMatchFinder();
        List<RegexMatch> matches = falsePositiveFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(9, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(3, "Link")));
        Assert.assertTrue(matches.contains(new RegexMatch(11, "Éstas")));
        Assert.assertTrue(matches.contains(new RegexMatch(23, "Index")));
        Assert.assertTrue(matches.contains(new RegexMatch(29, "Online")));
        Assert.assertTrue(matches.contains(new RegexMatch(39, "ésta")));
        Assert.assertTrue(matches.contains(new RegexMatch(44, "Tropicos.org")));
        Assert.assertTrue(matches.contains(new RegexMatch(57, "Aquél")));
        Assert.assertTrue(matches.contains(new RegexMatch(63, "aquéllo")));
        Assert.assertTrue(matches.contains(new RegexMatch(71, "Saint Martin")));
    }

}
