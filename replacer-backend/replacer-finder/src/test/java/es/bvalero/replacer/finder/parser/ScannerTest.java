package es.bvalero.replacer.finder.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ScannerTest {

    @Test
    public void testScanTokensComment() {
        String text = "Hello <!-- comment --> and goodbye.";

        Scanner scanner = new Scanner(text);
        List<Token> tokenList = scanner.scanTokens();

        assertFalse(tokenList.isEmpty());
        tokenList.forEach(System.out::println);
    }
}
