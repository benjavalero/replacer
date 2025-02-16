package es.bvalero.replacer.finder.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScannerTest {

    @Test
    void testScanComment() {
        String text = "Hello <!-- comment <!-- nested --> jarl --> and goodbye.";

        Scanner scanner = new Scanner();
        List<Token> tokenList = scanner.scan(text);

        assertEquals(9, tokenList.size());
        assertEquals(new Token(TokenType.TEXT, 0, 6), tokenList.get(0));
        assertEquals(new Token(TokenType.START_COMMENT, 6, 10), tokenList.get(1));
        assertEquals(new Token(TokenType.TEXT, 10, 19), tokenList.get(2));
        assertEquals(new Token(TokenType.START_COMMENT, 19, 23), tokenList.get(3));
        assertEquals(new Token(TokenType.TEXT, 23, 31), tokenList.get(4));
        assertEquals(new Token(TokenType.END_COMMENT, 31, 34), tokenList.get(5));
        assertEquals(new Token(TokenType.TEXT, 34, 40), tokenList.get(6));
        assertEquals(new Token(TokenType.END_COMMENT, 40, 43), tokenList.get(7));
        assertEquals(new Token(TokenType.TEXT, 43, 56), tokenList.get(8));
    }
}
