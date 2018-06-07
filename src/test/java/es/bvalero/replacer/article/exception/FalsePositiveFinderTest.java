package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FalsePositiveFinderTest {

    @Test
    public void testLoadFalsePositives() {
        List<String> falsePositives = FalsePositiveFinder.loadFalsePositives();
        Assert.assertFalse(falsePositives.isEmpty());
        Assert.assertTrue(falsePositives.contains("Index"));
        Assert.assertTrue(falsePositives.contains("Magazine"));
        Assert.assertFalse(falsePositives.contains("# LIST OF FALSE POSITIVES"));
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un sólo de Éstos en el Index Online de ésta Tropicos.org Aquél aquéllo Saint-Martin.";

        FalsePositiveFinder falsePositiveFinder = new FalsePositiveFinder();
        List<RegexMatch> matches = falsePositiveFinder.findExceptionMatches(text, false);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(8, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(3, "sólo")));
        Assert.assertTrue(matches.contains(new RegexMatch(11, "Éstos")));
        Assert.assertTrue(matches.contains(new RegexMatch(23, "Index")));
        Assert.assertTrue(matches.contains(new RegexMatch(29, "Online")));
        Assert.assertTrue(matches.contains(new RegexMatch(39, "ésta")));
        Assert.assertFalse(matches.contains(new RegexMatch(44, "Tropicos.org")));
        Assert.assertTrue(matches.contains(new RegexMatch(57, "Aquél")));
        Assert.assertTrue(matches.contains(new RegexMatch(63, "aquéllo")));
        Assert.assertTrue(matches.contains(new RegexMatch(71, "Saint-Martin")));
    }

}
