package es.bvalero.replacer.finder.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;

class ParserTest {

    @Test
    void testScanTokensComment() {
        String text = "Hello <!-- comment <!-- nested --> jarl --> and goodbye.";

        Parser parser = new Parser();

        List<Expression> expressionTree = IterableUtils.toList(parser.parse(text));
        assertEquals(1, expressionTree.size());
        assertEquals(ExpressionType.COMMENT, expressionTree.get(0).type());
        assertEquals(6, expressionTree.get(0).start());
        assertEquals(43, expressionTree.get(0).end());
        assertEquals(1, expressionTree.get(0).nested().size());

        List<Expression> expressionList = IterableUtils.toList(parser.find(text));
        assertEquals(2, expressionList.size());
        assertEquals(ExpressionType.COMMENT, expressionList.get(0).type());
        assertEquals(6, expressionList.get(0).start());
        assertEquals(43, expressionList.get(0).end());
        assertEquals(ExpressionType.COMMENT, expressionList.get(1).type());
        assertEquals(19, expressionList.get(1).start());
        assertEquals(34, expressionList.get(1).end());
        /*
        List<MatchResult> matches = IterableUtils.toList(parser.find(text, ExpressionType.COMMENT));

        assertEquals(2, matches.size());
        assertEquals(FinderMatchResult.of(text, 6, 43), matches.get(0));
        assertEquals(FinderMatchResult.of(text, 19, 34), matches.get(1));
         */
    }
}
