package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class FileNameFinderTest {

    @Test
    public void testRegexFileName() {
        String filename1 = "xx.jpg";
        String file1 = String.format("[[File:%s]]", filename1);
        String filename2 = "a b.png";
        String file2 = String.format("[[Image: %s ]]", filename2);
        String filename3 = "aa.jpg";
        String filename4 = "abc.JPEG";
        String filename5 = "b-c.jpg";
        String gallery1 = String.format(
            "<gallery>\nFile: %s | Desc\n%s|Desc\n Image:%s|Desc \n</gallery>",
            filename3,
            filename4,
            filename5
        );
        String filename6 = "doc.pdf";
        String filename7 = "value.gif";
        String filename8 = "image.JPG";
        String param = String.format(
            "{{Template| param1 = %s |param2=zzz.|param3=%s|param4=%s{{!}}Texto }}",
            filename6,
            filename7,
            filename8
        );
        String filename9 = "dóc2.pdf";
        String filename10 = "Value_2.gif";
        String gallery2 = String.format("{{Gallery\n| %s | Desc1 \n | %s | Desc2 \n}}", filename9, filename10);
        String domain1 = "www.google.com";
        String table = String.format("{| class=\"wikitable\"\n |-\n| %s\n| Any text.large\n|}", domain1);
        String domain2 = "link.org";
        String link = String.format("* [http://www.link.org Link %s]", domain2);
        String text = String.format("%s %s %s %s %s %s %s", file1, file2, gallery1, param, gallery2, table, link);

        ImmutableFinder fileNameFinder = new FileNameFinder();
        List<Immutable> matches = fileNameFinder.findList(text);

        // We capture also Internet domains
        Set<String> expected = new HashSet<>(
            Arrays.asList(
                filename1,
                filename2,
                filename3,
                filename4,
                filename5,
                filename6,
                filename7,
                filename8,
                filename9,
                filename10,
                domain1
            )
        );
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assert.assertEquals(expected, actual);
    }
}
