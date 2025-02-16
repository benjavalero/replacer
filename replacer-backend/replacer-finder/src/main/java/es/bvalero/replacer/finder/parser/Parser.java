package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.ExpressionType.COMMENT;
import static es.bvalero.replacer.finder.parser.ExpressionType.NONE;
import static es.bvalero.replacer.finder.parser.TokenType.END_COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.*;
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
            expressionTree.addAll(findExpressionIterable(NONE));
        }
        return expressionTree;
    }

    private List<Expression> findExpressionIterable(ExpressionType context) {
        final List<Expression> expressions = new ArrayList<>();
        while (current < tokenList.size()) {
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

    private void expect(TokenType type) {
        assert currentToken().type() == type;
    }

    private Comment comment() {
        expect(START_COMMENT);
        final int start = currentToken().start();
        advance();
        final List<Expression> contents = findExpressionIterable(COMMENT);
        // expect(END_COMMENT); // The comment can be truncated
        return new Comment(start, currentToken().end(), contents);
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

    public Iterable<MatchResult> find(String text, ExpressionType type) {
        return IterableUtils.transformedIterable(findFilteredIterable(text, type), exp -> convert(text, exp));
    }

    private MatchResult convert(String text, Expression expression) {
        return FinderMatchResult.of(text, expression.start(), expression.end());
    }
    //endregion
}
