package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommentFinderTest {

    private CommentFinder commentFinder;

    @BeforeEach
    public void setUp() {
        commentFinder = new CommentFinder();
    }

    @ParameterizedTest
    @ValueSource(strings = { "<!-- A simple comment -->", "<!-- This <span>is</span> a- \n comment -->" })
    void testComment(String text) {
        List<Immutable> matches = commentFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
    }

    @Test
    void testCommentTruncated() {
        String comment = "<!-- Comment not closed";
        String text = String.format("A %s", comment);

        List<Immutable> matches = commentFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(comment, matches.get(0).getText());
    }

    @Test
    void testSeveralComments() {
        String comment1 = "<!-- A comment -->";
        String comment2 = "<!-- Other comment -->";
        String text = String.format("Text %s Text %s.", comment1, comment2);

        List<Immutable> matches = commentFinder.findList(text);

        Set<String> expected = Set.of(comment1, comment2);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
