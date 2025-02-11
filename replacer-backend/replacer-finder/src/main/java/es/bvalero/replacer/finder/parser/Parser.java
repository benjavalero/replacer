package es.bvalero.replacer.finder.parser;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;
    private ExpressionType context;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Expression> parse() {
        return findExpressions();
    }

    private List<Expression> findExpressions() {
        final List<Expression> expressions = new ArrayList<>();

        while (!isAtEnd()) {
            final Token token = advance();
            if (token.type() == TokenType.END_COMMENT) {
                if (context == ExpressionType.COMMENT) {
                    break;
                }
            } else if (token.type() == TokenType.START_COMMENT) {
                context = ExpressionType.COMMENT;
                final List<Expression> contents = findExpressions();
                assert(previous().type() == TokenType.END_COMMENT);
                expressions.add(new Comment(token.start(), previous().end(), contents));
            /*
            } else {
                // Continue
             */
            }
        }

        return expressions;
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token advance() {
        return tokens.get(current++);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
