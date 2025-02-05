package es.bvalero.replacer.finder.benchmark.completetag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = BenchmarkFinder.class)
class CompleteTagFinderTest {

    @Autowired
    private FinderProperties finderProperties;

    private Set<String> completeTags;

    private String text;
    private Set<String> expected;

    @BeforeEach
    public void setUp() {
        this.completeTags = this.finderProperties.getCompleteTags();

        String tag1 = "<math class=\"latex\">An <i>example</i>\n in LaTeX</math>";
        String tag2 = "<math>To test repeated tags</math>";
        String tag3 = "<source>Another example</source>";
        String tag4 = "<ref name=NH05/>";
        String tag5 = "<ref>Text</ref>";
        String tag6 = "<unknown>Unknown</unknown>";
        String tag7 = "<ref>Unclosed tag";
        this.text = String.format("En %s %s %s %s %s %s %s", tag1, tag2, tag3, tag4, tag5, tag6, tag7);
        this.expected = Set.of(tag1, tag2, tag3, tag5);
    }

    @Test
    void testCompleteTagRegexIteratedFinder() {
        BenchmarkFinder finder = new CompleteTagRegexIteratedFinder(completeTags);
        assertEquals(
            expected,
            finder.findMatches(text).stream().map(BenchmarkResult::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testCompleteTagRegexBackReferenceFinder() {
        BenchmarkFinder finder = new CompleteTagRegexBackReferenceFinder(completeTags);
        assertEquals(
            expected,
            finder.findMatches(text).stream().map(BenchmarkResult::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testCompleteTagLinearIteratedFinder() {
        BenchmarkFinder finder = new CompleteTagLinearIteratedFinder(completeTags);
        assertEquals(
            expected,
            finder.findMatches(text).stream().map(BenchmarkResult::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testCompleteTagLinearFinder() {
        BenchmarkFinder finder = new CompleteTagLinearFinder(completeTags);
        assertEquals(
            expected,
            finder.findMatches(text).stream().map(BenchmarkResult::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testCompleteTagFinalFinder() {
        BenchmarkFinder finder = new CompleteTagFinalFinder(completeTags);
        assertEquals(
            expected,
            finder.findMatches(text).stream().map(BenchmarkResult::getText).collect(Collectors.toSet())
        );
    }
}
