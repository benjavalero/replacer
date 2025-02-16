package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.ExpressionType.COMMENT;
import static es.bvalero.replacer.finder.parser.ExpressionType.NONE;
import static es.bvalero.replacer.finder.parser.TokenType.END_COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.VisibleForTesting;

@VisibleForTesting
public class Parser {

    //region Build AST
    private final List<Expression> expressionTree = new ArrayList<>();
    private final List<Token> tokenList = new ArrayList<>();
    private int current = 0;

    /** Returns a list of expressions as a tree */
    @VisibleForTesting
    public List<Expression> expressionTree(String text) {
        // We can assume that we have one instance per text
        // so we can cache the expression tree for further calls
        if (expressionTree.isEmpty()) {
            tokenList.addAll(new Scanner().scan(text));
            expressionTree.addAll(findExpressionList(NONE));
        }
        return expressionTree;
    }

    private List<Expression> findExpressionList(ExpressionType context) {
        final List<Expression> expressions = new ArrayList<>();
        while (isNotAtEnd()) {
            TokenType currentType = currentToken().type();
            if (currentType == END_COMMENT) {
                if (context == COMMENT) {
                    return expressions;
                }
            } else if (currentType == START_COMMENT) {
                expressions.add(comment());
            }
            advance();
        }
        return expressions;
    }

    private Token currentToken() {
        return tokenList.get(current);
    }

    private void advance() {
        current++;
    }

    private boolean isNotAtEnd() {
        return current < tokenList.size();
    }

    private boolean check(TokenType type) {
        return isNotAtEnd() && currentToken().type() == type;
    }

    private void expect(TokenType type) {
        assert check(type);
    }

    private Comment comment() {
        expect(START_COMMENT);
        final int start = currentToken().start();
        advance();
        final List<Expression> contents = findExpressionList(COMMENT);
        if (check(END_COMMENT)) {
            return new Comment(start, currentToken().end(), contents, false);
        } else {
            assert current == tokenList.size();
            final Token lastToken = tokenList.get(current - 1);
            return new Comment(start, lastToken.end(), contents, true);
        }
    }

    //endregion

    //region Flatten AST

    /** Returns the expression tree as a flattened iterable */
    @VisibleForTesting
    public Iterable<Expression> expressionIterable(String text) {
        return () -> new ParserIterator(expressionTree(text));
    }

    private static class ParserIterator implements Iterator<Expression> {

        private final Stack<Expression> stack = new Stack<>();

        ParserIterator(List<Expression> expressionList) {
            pushAll(expressionList);
        }

        private void pushAll(List<Expression> expressionList) {
            // Add the nested in reverse in order to preserve the order in the resulting iterable
            ListIterator<Expression> listIterator = expressionList.listIterator(expressionList.size());
            while (listIterator.hasPrevious()) {
                stack.push(listIterator.previous());
            }
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
    private Iterable<Expression> findFilteredIterable(String text, ExpressionType type) {
        return IterableUtils.filteredIterable(expressionIterable(text), exp -> exp.type() == type);
    }

    private Iterable<Expression> findDecoratedIterable(
        Iterable<Expression> it,
        Function<Expression, Expression> decorator
    ) {
        return IterableUtils.transformedIterable(it, decorator::apply);
    }

    public Iterable<MatchResult> find(String text, ExpressionType type, Function<Expression, Expression> decorator) {
        return IterableUtils.transformedIterable(
            findDecoratedIterable(findFilteredIterable(text, type), decorator),
            exp -> convert(text, exp)
        );
    }

    public Iterable<MatchResult> find(String text, ExpressionType type) {
        return find(text, type, Function.identity());
    }

    private MatchResult convert(String text, Expression expression) {
        return FinderMatchResult.of(text, expression.start(), expression.end());
    }
    //endregion
}
