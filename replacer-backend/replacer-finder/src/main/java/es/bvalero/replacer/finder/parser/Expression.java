package es.bvalero.replacer.finder.parser;

import java.util.List;

public interface Expression {
    int start();
    int end();
    ExpressionType type();
    List<Expression> nested(); // So we can reverse it to traverse the AST in the right order
}
