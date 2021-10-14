package es.bvalero.replacer.finder.listing.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.listing.FalsePositive;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FalsePositiveParserTest {

    private FalsePositiveParser falsePositiveParser;

    @BeforeEach
    public void setUp() {
        falsePositiveParser = new FalsePositiveParser();
    }

    @Test
    void testParseFalsePositiveListText() {
        String falsePositiveListText =
            "Text\n" +
            "\n" + // Empty line
            " \n" + // Blank line
            " # A\n" + // Commented
            "A\n" + // No starting whitespace
            " B\n" +
            " b # X\n" + // With trailing comment
            " c\n" +
            " c\n"; // Duplicated

        Collection<String> falsePositives = falsePositiveParser
            .parseListing(falsePositiveListText)
            .stream()
            .map(FalsePositive::getExpression)
            .collect(Collectors.toSet());
        assertEquals(3, falsePositives.size());
        assertTrue(falsePositives.contains("B"));
        assertTrue(falsePositives.contains("b"));
        assertTrue(falsePositives.contains("c"));
    }
}
