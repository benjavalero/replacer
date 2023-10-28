package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = CompleteTagFinder.class)
class CompleteTagFinderTest {

    @SpyBean
    private CompleteTagFinder completeTagFinder;

    @ParameterizedTest
    @ValueSource(
        strings = {
            """
            <math class="latex">An <i>example</i>
            in LaTeX</math>""",
            "<source>Another example</source>",
            "<ref>Text</ref>",
        }
    )
    void testCompleteTag(String text) {
        List<Immutable> matches = completeTagFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "<Ref>Text</Ref>", // Uppercase
            "<ref", // Truncated
            "<unknown>Unknown</unknown>", // Not supported
        }
    )
    void testCompleteTagNotValid(String text) {
        List<Immutable> matches = completeTagFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testCompleteTagNotClosed() {
        String text = "An open tag not closed.<ref>Text";

        List<Immutable> matches = completeTagFinder.findList(text);

        assertTrue(matches.isEmpty());
        verify(completeTagFinder).logImmutableCheck(any(FinderPage.class), anyInt(), anyInt(), anyString());
    }

    @Test
    void testSeveralCompleteTags() {
        String tag1 = "<ref>A</ref>";
        String tag2 = "<ref>B</ref>";
        String text = String.format("A %s and %s.", tag1, tag2);

        List<Immutable> matches = completeTagFinder.findList(text);

        Set<String> expected = Set.of(tag1, tag2);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
