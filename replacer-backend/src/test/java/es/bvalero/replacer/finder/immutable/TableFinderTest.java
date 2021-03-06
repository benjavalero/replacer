package es.bvalero.replacer.finder.immutable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TableFinderTest {

    @Test
    void testTableStyles() {
        String text =
            "\n" +
            "{| class=\"wikitable\"\n" +
            "|+ Caption\n" +
            "|- style=\"bgcolor: salmon\"\n" +
            "| Example || Example || Example\n" +
            "|}";

        ImmutableFinder tableFinder = new TableFinder();
        List<Immutable> matches = tableFinder.findList(text);

        Set<String> expected = Set.of("{| class=\"wikitable\"", "|- style=\"bgcolor: salmon\"");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);

        // Check positions
        Assertions.assertTrue(
            matches.stream().allMatch(m -> text.substring(m.getStart(), m.getEnd()).equals(m.getText()))
        );
    }
}
