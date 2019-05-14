package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
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

        String text = String.format("%s %s %s %s %s %s", file1, file2, gallery1, param, gallery2, table);

        IgnoredReplacementFinder fileNameFinder = new FileNameFinder();

        List<MatchResult> matches = fileNameFinder.findIgnoredReplacements(text);
        Assert.assertEquals(9, matches.size());
        Assert.assertEquals("xx.jpg", matches.get(0).getText());
        Assert.assertEquals("a b.png", matches.get(1).getText());
        Assert.assertEquals("aa.jpg", matches.get(2).getText());
        Assert.assertEquals("b-c.jpg", matches.get(3).getText());
        Assert.assertEquals("doc.pdf", matches.get(4).getText());
        Assert.assertEquals("value.gif", matches.get(5).getText());
        Assert.assertEquals("dóc2.pdf", matches.get(6).getText());
        Assert.assertEquals("Value_2.gif", matches.get(7).getText());
        Assert.assertEquals("www.google.com", matches.get(8).getText()); // We capture also Internet domains
    }

}
