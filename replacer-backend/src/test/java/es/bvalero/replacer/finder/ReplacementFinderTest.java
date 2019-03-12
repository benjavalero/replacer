package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

public class ReplacementFinderTest extends ReplacementFinder {

    @Test
    public void testStartsWithUpperCase() {
        Assert.assertTrue(startsWithUpperCase("Álvaro"));
        Assert.assertFalse(startsWithUpperCase("úlcera"));
    }

}
