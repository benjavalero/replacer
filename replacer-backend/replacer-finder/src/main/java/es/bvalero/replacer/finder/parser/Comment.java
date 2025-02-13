package es.bvalero.replacer.finder.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record Comment(int start, int end, Collection<Expression> content) implements Expression {
    @Override
    public ExpressionType type() {
        return ExpressionType.COMMENT;
    }

    @Override
    public List<Expression> nested() {
        return new ArrayList<>(content);
    }
}
