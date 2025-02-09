package es.bvalero.replacer.finder.parser;

import java.util.List;
import java.util.Objects;

import static es.bvalero.replacer.finder.parser.TokenType.EOF;

class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Statement parse() {
        return statement();
    }

    private Statement statement() {
        Statement statement = new Statement();

        while (!isAtEnd()) {
            Token token = advance();
            if (token.type == TokenType.TEXT) {
                Text text = new Text();
                text.text = token.lexeme;
                statement.expressions.add(text);
            } else if (token.type == TokenType.OPEN_COMMENT) {
                Comment comment = new Comment();
                comment.content = statement();
                statement.expressions.add(comment);
            } else {
                break;
            }
        }

        return statement;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        return tokens.get(current++);
    }


    /*
    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
     */
}
