package es.bvalero.replacer.finder.parser;

import java.util.List;

public interface Expression {
    int start();
    int end();
    ExpressionType type();
    List<Expression> nested();
}
