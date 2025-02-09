package es.bvalero.replacer.finder.parser;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record Statement(int start, List<Expression> expressions) implements Expression {
    @Override
    public int end() {
        return this.expressions.get(this.expressions.size() - 1).end();
    }

    @Override
    public String text() {
        return StringUtils.join(this.expressions.stream().map(Expression::text));
    }

    @Override
    public ExpressionType type() {
        return ExpressionType.STATEMENT;
    }
}
