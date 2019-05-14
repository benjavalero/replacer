package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class FileFinderTest {

    private String text;
    private Set<MatchResult> expected;

    @Before
    public void setUp() {
        String file1 = "[[File:xx.jpg]]";
        String file2 = "[[Image: a b.png ]]";
        String gallery1 = "<gallery>\n"
                + "File: aa.jpg \n"
                + " Image:b-c.jpg|Desc \n"
                + "</gallery>";
        String param = "{{Template| param1 = doc.pdf |param2=zzz.|param3=value.gif}}";
        String gallery2 = "{{Gallery\n"
                + "| dóc2.pdf | Desc1 \n"
                + " | Value_2.gif | Desc2 \n"
                + "}}";
        String table = "{| class=\"wikitable\"\n " +
                "|-\n" +
                "| www.google.com\n" +
                "| Any text\n" +
                "|}";

        this.text = String.format("%s %s %s %s %s %s", file1, file2, gallery1, param, gallery2, table);

        this.expected = new HashSet<>();
        this.expected.add(new MatchResult(7, "xx.jpg"));
        this.expected.add(new MatchResult(25, "a b.png"));
        this.expected.add(new MatchResult(52, "aa.jpg"));
        this.expected.add(new MatchResult(67, "b-c.jpg"));
        this.expected.add(new MatchResult(113, "doc.pdf"));
        this.expected.add(new MatchResult(141, "value.gif"));
        this.expected.add(new MatchResult(165, "dóc2.pdf"));
        this.expected.add(new MatchResult(186, "Value_2.gif"));
        this.expected.add(new MatchResult(237, "www.google.com")); // We capture also Internet domains
    }

    @Test
    public void testFileRegexFinder() {
        FileRegexFinder finder = new FileRegexFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testFileRegexLazyFinder() {
        FileRegexLazyFinder finder = new FileRegexLazyFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testFileAutomatonFinder() {
        FileAutomatonFinder finder = new FileAutomatonFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testFileRegexNoStartFinder() {
        FileRegexNoStartFinder finder = new FileRegexNoStartFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testFileRegexNoStartLazyFinder() {
        FileRegexNoStartLazyFinder finder = new FileRegexNoStartLazyFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testFileAutomatonNoStartFinder() {
        FileAutomatonNoStartFinder finder = new FileAutomatonNoStartFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
