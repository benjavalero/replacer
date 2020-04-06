package es.bvalero.replacer.finder.benchmark;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommentFinderTest {
    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        this.text = "xxx " + comment1 + " / " + comment2 + " zzz";

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(4, comment1));
        this.expected.add(FinderResult.of(53, comment2));
    }

    @Test
    public void testCommentRegexFinder() {
        CommentRegexFinder finder = new CommentRegexFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCommentAutomatonFinder() {
        CommentAutomatonFinder finder = new CommentAutomatonFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
