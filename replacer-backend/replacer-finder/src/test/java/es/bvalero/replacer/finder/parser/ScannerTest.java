package es.bvalero.replacer.finder.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;

public class ScannerTest {

    @Test
    public void testScanTokensComment() {
        String text = "Hello <!-- comment <!-- nested --> jarl --> and goodbye.";

        Scanner scanner = new Scanner();
        List<Token> tokenList = IterableUtils.toList(scanner.scanTokens(text));

        assertEquals(4, tokenList.size());
        assertEquals(new Token(TokenType.START_COMMENT, 6, 10), tokenList.get(0));
        assertEquals(new Token(TokenType.START_COMMENT, 19, 23), tokenList.get(1));
        assertEquals(new Token(TokenType.END_COMMENT, 31, 34), tokenList.get(2));
        assertEquals(new Token(TokenType.END_COMMENT, 40, 43), tokenList.get(3));

        Parser parser = new Parser();
        List<Expression> expressions = parser.parse(text);

        System.out.println(expressions);

        Iterable<Expression> flattened = parser.find(text);
        for (Expression expression : flattened) {
            System.out.println("--- " + expression);
        }

        /*

        assertEquals(1, expressions.size());
        assertEquals(ExpressionType.COMMENT, expressions.get(0).type());
        assertInstanceOf(Comment.class, expressions.get(0));
        Comment comment = (Comment) expressions.get(0);
        assertEquals(1, comment.content().size());
        List<Expression> subcontents = new ArrayList<>(comment.content());
        assertEquals(ExpressionType.COMMENT, subcontents.get(0).type());
        assertInstanceOf(Comment.class, subcontents.get(0));

         */
    }
}
