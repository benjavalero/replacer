package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CommentFinderTest {

    private String text;
    private Set<MatchResult> expected;

    @Before
    public void setUp() {
        String comment1 = "<!-- Esto <span>es</span> un- \n comentario -->";
        String comment2 = "<!-- Otro comentario -->";
        this.text = "xxx " + comment1 + " / " + comment2 + " zzz";

        this.expected = new HashSet<>();
        this.expected.add(new MatchResult(4, comment1));
        this.expected.add(new MatchResult(53, comment2));
    }

    @Test
    public void testCommentRegexFinder() {
        CommentRegexFinder finder = new CommentRegexFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCommentAutomatonFinder() {
        CommentAutomatonFinder finder = new CommentAutomatonFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
