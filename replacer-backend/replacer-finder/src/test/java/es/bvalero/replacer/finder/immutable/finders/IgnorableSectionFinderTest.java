package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.FinderPropertiesConfiguration;
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
            "== Bibliografía de Julio Verne ==\n" + "Ignorable Content\n === Ignorable Subsection\n";
        String text =
            "Text\n" + "== Section 1 ==\n" + "Content 1\n" + ignorableSection + "== Section 3 ==\n" + "Content 3";

        List<Immutable> matches = ignorableSectionFinder.findList(text);

        Set<String> expected = Set.of(ignorableSection);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
