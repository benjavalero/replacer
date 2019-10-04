package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
        String file1 = "[[File:xx.jpg]]";
        String file2 = "[[Image: a b.png ]]";
        String gallery1 = "<gallery>\n"
                + "File: aa.jpg | Desc\n"
                + "abc.JPEG|Desc\n"
                + " Image:b-c.jpg|Desc \n"
                + "</gallery>";
        String param = "{{Template| param1 = doc.pdf |param2=zzz.|param3=value.gif|param4=image.JPG{{!}}Texto }}";
        String gallery2 = "{{Gallery\n"
                + "| dóc2.pdf | Desc1 \n"
                + " | Value_2.gif | Desc2 \n"
                + "}}";
        String table = "{| class=\"wikitable\"\n " +
                "|-\n" +
                "| www.google.com\n" +
                "| Any text.large\n" +
                "|}";
        String link = "* [http://www.link.org Link link.org]";

        String text = String.format("%s %s %s %s %s %s %s", file1, file2, gallery1, param, gallery2, table, link);

        IgnoredReplacementFinder fileNameFinder = new FileNameFinder();
        List<IgnoredReplacement> matches = fileNameFinder.findIgnoredReplacements(text);

        Set<String> expected = new HashSet<>(Arrays.asList(
                "xx.jpg", "a b.png", "aa.jpg", "abc.JPEG", "b-c.jpg",
                "doc.pdf", "value.gif", "image.JPG", "dóc2.pdf", "Value_2.gif",
                "www.google.com")); // We capture also Internet domains
        Assert.assertEquals(expected, matches.stream().map(IgnoredReplacement::getText).collect(Collectors.toSet()));
    }

}
