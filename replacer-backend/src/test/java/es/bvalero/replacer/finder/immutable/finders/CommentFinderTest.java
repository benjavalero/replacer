package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommentFinderTest {

    @Test
    void testRegexComment() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        String text = String.format("%s %s", comment1, comment2);

        ImmutableFinder commentFinder = new CommentFinder();

        List<Immutable> matches = commentFinder.findList(text);
        assertEquals(2, matches.size());
        assertEquals(comment1, matches.get(0).getText());
        assertEquals(comment2, matches.get(1).getText());
    }
}
