package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class XmlTagFinderTest {

    @Test
    void testXmlTagFinder() {
        String tag1 = "<span style=\"color:green;\">";
        String tag2 = "</span>";
        String tag3 = "<br />";
        String text = String.format("%s %s %s", tag1, tag2, tag3);

        ImmutableFinder xmlTagFinder = new XmlTagFinder();
        List<Immutable> matches = xmlTagFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(tag1, tag2, tag3));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testRegexCommentNotMatched() {
        String comment = "<!-- Esto es un comentario -->";
        String text = "xxx " + comment + " zzz";

        ImmutableFinder xmlTagFinder = new XmlTagFinder();
        List<Immutable> matches = xmlTagFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }
}
