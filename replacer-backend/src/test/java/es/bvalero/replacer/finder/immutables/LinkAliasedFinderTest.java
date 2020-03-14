package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class LinkAliasedFinderTest {

    @Test
    public void testRegexUrl() {
        String aliased1 = "brasil";
        String aliased2 = "reacción química";
        String noAliased = "Text";
        String text = String.format("[[%s|Brasil]] [[%s]] [[ %s |reacción]].", aliased1, noAliased, aliased2);

        ImmutableFinder linkAliasedFinder = new LinkAliasedFinder();
        List<Immutable> matches = linkAliasedFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(aliased1, aliased2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }
}
