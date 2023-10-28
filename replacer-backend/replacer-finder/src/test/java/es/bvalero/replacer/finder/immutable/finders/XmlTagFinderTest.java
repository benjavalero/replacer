package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

        Set<String> expected = Set.of(tag1, tag2, tag3);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testRegexCommentNotMatched() {
        String comment = "<!-- Esto es un comentario -->";
        String text = "xxx " + comment + " zzz";

        ImmutableFinder xmlTagFinder = new XmlTagFinder();
        List<Immutable> matches = xmlTagFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testTagNotClosed() {
        String text = "A not closed tag <br ";

        ImmutableFinder xmlTagFinder = new XmlTagFinder();
        List<Immutable> matches = xmlTagFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testTagWithForbiddenChars() {
        String text = "A tag with forbidden chars <span #>";

        ImmutableFinder xmlTagFinder = new XmlTagFinder();
        List<Immutable> matches = xmlTagFinder.findList(text);

        assertTrue(matches.isEmpty());
    }
}
