package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
public class CompleteTagFinderTest {
    @Resource
    private Set<String> completeTags;

    private String text;
    private Set<String> expected;

    @BeforeEach
    public void setUp() {
        String tag1 = "<math class=\"latex\">An <i>example</i>\n in LaTeX</math>";
        String tag2 = "<math>To test repeated tags</math>";
        String tag3 = "<source>Another example</source>";
        String tag4 = "<ref name=NH05/>";
        String tag5 = "<ref>Text</ref>";
        String tag6 = "<unknown>Unknown</unknown>";
        String tag7 = "<ref>Unclosed tag";
        this.text = String.format("En %s %s %s %s %s %s %s", tag1, tag2, tag3, tag4, tag5, tag6, tag7);
        this.expected = new HashSet<>(Arrays.asList(tag1, tag2, tag3, tag5));
    }

    @Test
    void testCompleteTagRegexIteratedFinder() {
        BenchmarkFinder finder = new CompleteTagRegexIteratedFinder(completeTags);
        Assertions.assertEquals(
            expected,
            finder.findMatches(text).stream().map(FinderResult::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testCompleteTagRegexBackReferenceFinder() {
        BenchmarkFinder finder = new CompleteTagRegexBackReferenceFinder(completeTags);
        Assertions.assertEquals(
            expected,
            finder.findMatches(text).stream().map(FinderResult::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testCompleteTagLinearIteratedFinder() {
        BenchmarkFinder finder = new CompleteTagLinearIteratedFinder(completeTags);
        Assertions.assertEquals(
            expected,
            finder.findMatches(text).stream().map(FinderResult::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testCompleteTagLinearFinder() {
        BenchmarkFinder finder = new CompleteTagLinearFinder(completeTags);
        Assertions.assertEquals(
            expected,
            finder.findMatches(text).stream().map(FinderResult::getText).collect(Collectors.toSet())
        );
    }
}
