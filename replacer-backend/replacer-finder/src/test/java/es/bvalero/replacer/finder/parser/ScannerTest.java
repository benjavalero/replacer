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

        tokenList.forEach(System.out::println);

        assertEquals(9, tokenList.size());
        tokenList.forEach(token -> assertEquals(token.text(), text.substring(token.start(), token.end())));

        Parser parser = new Parser(tokenList);
        List<Expression> expressions = parser.parse();

        System.out.println(expressions);

        assertEquals(3, expressions.size());
        assertInstanceOf(Comment.class, expressions.get(1));
        Comment comment = (Comment) expressions.get(1);
        assertEquals(3, comment.content().expressions().size());
        assertInstanceOf(Comment.class, comment.content().expressions().get(1));
    }
}
