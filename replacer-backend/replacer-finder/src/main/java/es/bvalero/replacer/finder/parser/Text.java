package es.bvalero.replacer.finder.parser;

public record Text(int start, String text) implements Expression {
    @Override
    public int end() {
        return start + text.length();
    }

    @Override
    public ExpressionType type() {
        return ExpressionType.TEXT;
    }
}
