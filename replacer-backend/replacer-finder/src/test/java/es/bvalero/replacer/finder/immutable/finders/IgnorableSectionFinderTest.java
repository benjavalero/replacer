package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = IgnorableSectionFinder.class)
class IgnorableSectionFinderTest {

    @Autowired
    private IgnorableSectionFinder ignorableSectionFinder;

    @Test
    void testIgnorableSection() {
        String ignorableSection =
            """
            == Bibliografía de Julio Verne ==
            Ignorable Content
            === Ignorable Subsection
            Text2
            """;
        String text =
            """
            Text
            == Section 1 ==
            Content 1
            """ +
            ignorableSection +
            """
            == Section 3 ==
            Content 3
            """;

        List<Immutable> matches = ignorableSectionFinder.findList(text);

        Set<String> expected = Set.of(ignorableSection);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testIgnorableSectionNotClosed() {
        String text = "Text == Bibliografía de Julio Verne";
        List<Immutable> matches = ignorableSectionFinder.findList(text);
        assertTrue(matches.isEmpty());
    }

    @Test
    void testIgnorableSectionAtTheEnd() {
        String ignorableSection =
            """
            == Bibliografía de Julio Verne ==
            Ignorable Content
            === Ignorable Subsection
            Text2
            """;
        String text =
            """
            Text
            == Section 1 ==
            Content 1
            """ +
            ignorableSection;

        List<Immutable> matches = ignorableSectionFinder.findList(text);

        Set<String> expected = Set.of(ignorableSection);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
