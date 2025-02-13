package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.ExpressionType.COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.END_COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Parser {

    private final Scanner scanner = new Scanner();
    private Token current;
    private ExpressionType context;

    public List<Expression> parse(String text) {
        return findExpressions(scanner.scanTokens(text).iterator());
    }

    private List<Expression> findExpressions(Iterator<Token> it) {
        final List<Expression> expressions = new ArrayList<>();
        while (it.hasNext()) {
            current = it.next();
            if (current.type() == END_COMMENT) {
                if (context == COMMENT) {
                    break;
                }
            } else if (current.type() == START_COMMENT) {
                context = COMMENT;
                final int start = current.start();
                final List<Expression> contents = findExpressions(it);
                assert current.type() == END_COMMENT;
                expressions.add(new Comment(start, current.end(), contents));
                /*
            } else {
                // Continue
            */
            }
        }

        return expressions;
    }
}
