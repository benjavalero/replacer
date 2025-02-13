package es.bvalero.replacer.finder.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.List;
import java.util.regex.MatchResult;
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
        List<MatchResult> matches = IterableUtils.toList(parser.find(text, ExpressionType.COMMENT));

        assertEquals(2, matches.size());
        assertEquals(FinderMatchResult.of(text, 6, 43), matches.get(0));
        assertEquals(FinderMatchResult.of(text, 19, 34), matches.get(1));
    }
}
