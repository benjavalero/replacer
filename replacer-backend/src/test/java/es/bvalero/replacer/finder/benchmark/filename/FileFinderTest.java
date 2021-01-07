package es.bvalero.replacer.finder.benchmark.filename;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileFinderTest {

    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        String filename1 = " xx.jpg ";
        String file1 = String.format("[[Archivo:%s|thumb|Description]]", filename1);
        String filename2 = "a b.png";
        String file2 = String.format("[[Imagen:%s]]", filename2);

        this.text = String.format("%s %s", file1, file2);

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(10, filename1));
        this.expected.add(FinderResult.of(48, filename2));
    }

    @Test
    void testFileRegexFinder() {
        FileRegexFinder finder = new FileRegexFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testFileRegexGroupFinder() {
        FileRegexGroupFinder finder = new FileRegexGroupFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testFileAutomatonFinder() {
        FileAutomatonFinder finder = new FileAutomatonFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testFileLinearFinder() {
        FileLinearFinder finder = new FileLinearFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
