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

        Parser parser = new Parser(tokenList);
        Statement statement = parser.parse();

        System.out.println(statement);

        assertEquals(3, statement.expressions().size());
        assertInstanceOf(Comment.class, statement.expressions().get(1));
        Comment comment = (Comment) statement.expressions().get(1);
        assertEquals(3, comment.content().expressions().size());
        assertInstanceOf(Comment.class, comment.content().expressions().get(1));
    }
}
