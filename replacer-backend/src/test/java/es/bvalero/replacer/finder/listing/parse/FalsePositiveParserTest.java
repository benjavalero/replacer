package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.FalsePositive;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals(3, falsePositives.size());
        Assertions.assertTrue(falsePositives.contains("B"));
        Assertions.assertTrue(falsePositives.contains("b"));
        Assertions.assertTrue(falsePositives.contains("c"));
    }
}
