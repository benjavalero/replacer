package es.bvalero.replacer.finder.parser;

import java.util.List;
import org.apache.commons.collections4.IterableUtils;

record Comment(int start, int end, Iterable<Expression> content) implements Expression {
    @Override
    public ExpressionType type() {
        return ExpressionType.COMMENT;
    }

    @Override
    public List<Expression> nested() {
        // TODO: field could be a List
        return IterableUtils.toList(content);
    }
}
