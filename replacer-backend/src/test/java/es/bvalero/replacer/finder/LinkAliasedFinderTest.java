package es.bvalero.replacer.finder;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LinkAliasedFinderTest {

    @Test
    public void testRegexUrl() {
        String aliased1 = "brasil";
        String aliased2 = "reacción química";
        String noAliased = "Text";
        String text = String.format("[[%s|Brasil]] [[%s|reacción]] [[%s]].", aliased1, aliased2, noAliased);

        ImmutableFinder linkAliasedFinder = new LinkAliasedFinder();

        List<Immutable> matches = linkAliasedFinder.findList(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(aliased1, matches.get(0).getText());
        Assert.assertEquals(aliased2, matches.get(1).getText());
    }
}
