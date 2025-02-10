package es.bvalero.replacer.finder.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

public class ScannerTest {

    @Test
    public void testScanTokensComment() {
        String text = "Hello <!-- comment <!-- nested --> jarl --> and goodbye.";

        ScannerNoText scanner = new ScannerNoText(text);
        Collection<Token> tokenList = scanner.scanTokens();

        tokenList.forEach(System.out::println);

        assertEquals(4, tokenList.size());

        ScannerSimple scannerSimple = new ScannerSimple(text);
        Collection<Token> tokenList2 = scannerSimple.scanTokens();

        tokenList2.forEach(System.out::println);

        assertEquals(4, tokenList2.size());

        Parser parser = new Parser(new ArrayList<>(tokenList));
        Statement statement = parser.parse();

        System.out.println(statement);

        assertEquals(3, statement.expressions().size());
        assertInstanceOf(Comment.class, statement.expressions().get(1));
        Comment comment = (Comment) statement.expressions().get(1);
        assertEquals(3, comment.content().expressions().size());
        assertInstanceOf(Comment.class, comment.content().expressions().get(1));
    }
}
