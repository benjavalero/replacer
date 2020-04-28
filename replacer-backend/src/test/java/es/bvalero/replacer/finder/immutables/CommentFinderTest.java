package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommentFinderTest {

    @Test
    public void testRegexComment() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        String text = String.format("%s %s", comment1, comment2);

        ImmutableFinder commentFinder = new CommentFinder();

        List<Immutable> matches = commentFinder.findList(text);
        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(comment1, matches.get(0).getText());
        Assertions.assertEquals(comment2, matches.get(1).getText());
    }
}
