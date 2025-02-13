package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.ExpressionType.COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

public class Parser {

    //region Build AST
    private final Scanner scanner = new Scanner();
    private Iterator<Token> it;
    private Token currentToken;

    private Iterable<Expression> parse(String text) {
        it = scanner.scanTokens(text).iterator();
        return findExpressions(ExpressionType.NONE);
    }

    private void expect(TokenType type) {
        assert currentToken.type() == type;
    }

    private Iterable<Expression> findExpressions(ExpressionType context) {
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
        final Iterable<Expression> contents = findExpressions(COMMENT);
        // expect(END_COMMENT); // The comment can be truncated
        return new Comment(start, currentToken.end(), contents);
    }

    //endregion

    //region Flatten AST
    private Iterable<Expression> find(String text) {
        return () -> new ParserIterator(parse(text));
    }

    private static class ParserIterator implements Iterator<Expression> {

        private final Stack<Expression> stack = new Stack<>();

        ParserIterator(Iterable<Expression> expressions) {
            pushAll(expressions);
        }

        private void pushAll(Iterable<Expression> expressions) {
            expressions.forEach(stack::push);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public Expression next() {
            final Expression e = stack.pop();
            pushAll(e.nested());
            return e;
        }
    }

    //end region

    //region Match Expression Type
    private Iterable<Expression> expressions;

    private Iterable<Expression> getExpressions(String text) {
        if (expressions == null) {
            expressions = find(text);
        }
        return expressions;
    }

    public Iterable<MatchResult> find(String text, ExpressionType type) {
        return IterableUtils.transformedIterable(
            IterableUtils.filteredIterable(getExpressions(text), e -> e.type() == type),
            e -> convert(text, e)
        );
    }

    private MatchResult convert(String text, Expression expression) {
        return FinderMatchResult.of(text, expression.start(), expression.end());
    }
    //endregion
}
