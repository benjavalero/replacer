package es.bvalero.replacer.finder.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;

public class ScannerTest {

    @Test
    public void testScanTokensComment() {
        String text = "Hello <!-- comment <!-- nested --> jarl --> and goodbye.";

        Scanner scanner = new Scanner(text);
        List<Token> tokenList = scanner.scanTokens();

        assertEquals(4, tokenList.size());
        assertEquals(new Token(TokenType.START_COMMENT, 6, 10), tokenList.get(0));
        assertEquals(new Token(TokenType.START_COMMENT, 19, 23), tokenList.get(1));
        assertEquals(new Token(TokenType.END_COMMENT, 31, 34), tokenList.get(2));
        assertEquals(new Token(TokenType.END_COMMENT, 40, 43), tokenList.get(3));

        Parser parser = new Parser(tokenList);
        List<Expression> expressions = parser.parse();

        System.out.println(expressions);

        assertEquals(1, expressions.size());
        assertEquals(ExpressionType.COMMENT, expressions.get(0).type());
        assertInstanceOf(Comment.class, expressions.get(0));
        Comment comment = (Comment) expressions.get(0);
        assertEquals(1, comment.content().size());
        assertEquals(ExpressionType.COMMENT, comment.content().get(0).type());
        assertInstanceOf(Comment.class, comment.content().get(0));
    }
}
