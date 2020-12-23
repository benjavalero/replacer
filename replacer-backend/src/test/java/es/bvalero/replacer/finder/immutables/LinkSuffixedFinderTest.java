package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LinkSuffixedFinderTest {

    @Test
    void testLinkSuffixed() {
        String suffixed1 = "[[brasil]]eño";
        String suffixed2 = "[[reacción química|reaccion]]es";
        String noSuffixed = "[[Text]]";
        String inFile = "[[totem]]s";
        String file = String.format("[[Archivo:totem.jpg|Los %s]]", inFile);
        String text = String.format("%s, %s, %s y %s", suffixed1, file, suffixed2, noSuffixed);

        ImmutableFinder linkSuffixedFinder = new LinkSuffixedFinder();
        List<Immutable> matches = linkSuffixedFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(suffixed1, suffixed2, inFile));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
