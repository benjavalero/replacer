package es.bvalero.replacer.finder.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ScannerTest {

    @Test
    public void testScanTokensComment() {
        String text = "Hello <!-- comment <!-- nested --> jarl --> and {{ t | k = v }} goodbye.";

        Scanner scanner = new Scanner(text);
        List<Token> tokenList = scanner.scanTokens();

        assertFalse(tokenList.isEmpty());
        for (Token token : tokenList) {
            System.out.println(token);
            assertEquals(token.lexeme, text.substring(token.start, token.end()));
        }

        Parser parser = new Parser(tokenList);
        Statement statement = parser.parse();
        System.out.println(statement);
    }
}
