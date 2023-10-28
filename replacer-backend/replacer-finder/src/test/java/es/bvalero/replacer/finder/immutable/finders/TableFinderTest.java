package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TableFinderTest {

    private TableFinder tableFinder;

    @BeforeEach
    public void setUp() {
        tableFinder = new TableFinder();
    }

    @Test
    void testTable() {
        String text =
            """

            {| class="wikitable"
            |+ Caption
            |- style="bgcolor: salmon"
            | Example || Example || Example
            |}
            """;

        List<Immutable> matches = tableFinder.findList(text);

        Set<String> expected = Set.of("{| class=\"wikitable\"", "|- style=\"bgcolor: salmon\"");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testTableLineAtTheEnd() {
        String text = "{| class=\"wikitable\"";

        List<Immutable> matches = tableFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
    }
}
