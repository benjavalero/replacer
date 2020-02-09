package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CommentFinderTest {

    @Test
    public void testRegexComment() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        String text = String.format("%s %s", comment1, comment2);

        ImmutableFinder commentFinder = new CommentFinder();

        List<Immutable> matches = commentFinder.findList(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(comment1, matches.get(0).getText());
        Assert.assertEquals(comment2, matches.get(1).getText());
    }
}
