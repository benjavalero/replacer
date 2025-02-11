package es.bvalero.replacer.finder.parser;

import java.util.List;

public record Comment(int start, int end, List<Expression> content) implements Expression {
    @Override
    public ExpressionType type() {
        return ExpressionType.COMMENT;
    }
}
