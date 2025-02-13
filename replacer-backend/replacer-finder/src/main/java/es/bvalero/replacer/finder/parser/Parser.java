package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.ExpressionType.COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.END_COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class Parser {

    private final Scanner scanner = new Scanner();
    private Iterator<Token> it;
    private Token currentToken;

    public List<Expression> parse(String text) {
        it = scanner.scanTokens(text).iterator();
        return findExpressions(ExpressionType.NONE);
    }

    private void expect(TokenType type) {
        assert currentToken.type() == type;
    }

    private List<Expression> findExpressions(ExpressionType context) {
        final List<Expression> expressions = new ArrayList<>();
        while (it.hasNext()) {
            currentToken = it.next();
            switch (currentToken.type()) {
                case END_COMMENT -> {
                    if (context == COMMENT) {
                        return expressions;
                    }
                }
                case START_COMMENT -> expressions.add(comment());
                default -> {
                    // Continue
                }
            }
        }

        return expressions;
    }

    private Comment comment() {
        expect(START_COMMENT);
        final int start = currentToken.start();
        final List<Expression> contents = findExpressions(COMMENT);
        expect(END_COMMENT);
        final int end = currentToken.end();
        return new Comment(start, end, contents);
    }

    public Iterable<Expression> find(String text) {
        return () -> new ParserIterator(parse(text));
    }

    private static class ParserIterator implements Iterator<Expression> {
        private final Stack<Expression> expressionStack = new Stack<>();

        ParserIterator(List<Expression> root) {
            pushAll(root);
        }

        @Override
        public boolean hasNext() {
            return !expressionStack.isEmpty();
        }

        @Override
        public Expression next() {
            final Expression e = expressionStack.pop();
            pushAll(e.nested());
            return e;
        }

        private void pushAll(List<Expression> expressions) {
            expressions.forEach(expressionStack::push);
        }
    }
}
