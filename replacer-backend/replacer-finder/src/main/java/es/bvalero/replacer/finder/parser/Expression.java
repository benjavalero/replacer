package es.bvalero.replacer.finder.parser;

public interface Expression {
    int start();
    int end();
    String text();
    ExpressionType type();
}
