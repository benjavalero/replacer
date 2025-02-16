package es.bvalero.replacer.finder.parser;

import java.util.List;

public record Comment(int start, int end, List<Expression> content, boolean isTruncated) implements Expression {
    @Override
    public ExpressionType type() {
        return ExpressionType.COMMENT;
    }

    @Override
    public List<Expression> nested() {
        return content;
    }
}
