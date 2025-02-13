package es.bvalero.replacer.finder.parser;

interface Expression {
    int start();
    int end();
    ExpressionType type();
    Iterable<Expression> nested();
}
