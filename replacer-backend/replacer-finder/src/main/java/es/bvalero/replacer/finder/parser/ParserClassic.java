package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.ExpressionType.COMMENT;
import static es.bvalero.replacer.finder.parser.ExpressionType.NONE;
import static es.bvalero.replacer.finder.parser.TokenType.END_COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import java.util.*;
import org.jetbrains.annotations.VisibleForTesting;

public class ParserClassic {

    private final List<Token> tokenList = new ArrayList<>();
    private int current = 0;

    /** Returns a list of expressions as a tree */
    @VisibleForTesting
    public List<Expression> parse(String text) {
        tokenList.addAll(new Scanner().scan(text));
        return findExpressions(NONE);
    }

    private List<Expression> findExpressions(ExpressionType context) {
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
        final List<Expression> contents = findExpressions(COMMENT);
        // expect(END_COMMENT); // The comment can be truncated
        return new Comment(start, currentToken().end(), contents);
    }

    private final Stack<Expression> stack = new Stack<>();

    /** Returns a list of expressions */
    @VisibleForTesting
    public List<Expression> find(String text) {
        final List<Expression> list = new ArrayList<>();
        pushAll(parse(text));
        while (!stack.isEmpty()) {
            final Expression e = stack.pop();
            pushAll(e.nested());
            list.add(e);
        }
        return list;
    }

    private void pushAll(List<Expression> expressionList) {
        ListIterator<Expression> listIterator = expressionList.listIterator(expressionList.size());
        while (listIterator.hasPrevious()) {
            stack.push(listIterator.previous());
        }
    }
    /*
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

     */
}
