package es.bvalero.replacer.finder.parser;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Statement parse() {
        return statement();
    }

    private Statement statement() {
        int start = -1;
        List<Expression> expressions = new ArrayList<>();

        while (!isAtEnd()) {
            Token token = advance();
            if (start < 0) start = token.start();

            if (token.type() == TokenType.TEXT) {
                expressions.add(new Text(token.start(), null));
            } else if (token.type() == TokenType.START_COMMENT) {
                // TODO: Broken comments
                expressions.add(new Comment(token.start(), statement()));
            } else {
                break;
            }
        }

        return new Statement(start, expressions);
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token advance() {
        return tokens.get(current++);
    }
}
