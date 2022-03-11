package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CommentFinderTest {

    @Test
    void testRegexComment() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        String comment3 = "<!-- Comment not closed";
        String text = String.format("%s %s %s", comment1, comment2, comment3);

        ImmutableFinder commentFinder = new CommentFinder();
        List<Immutable> matches = commentFinder.findList(text);

        Set<String> expected = Set.of(comment1, comment2);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
