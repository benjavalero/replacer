package es.bvalero.replacer.finder.parser;

import java.util.Collection;

public record Comment(int start, int end, Collection<Expression> content) implements Expression {
    @Override
    public ExpressionType type() {
        return ExpressionType.COMMENT;
    }
}
