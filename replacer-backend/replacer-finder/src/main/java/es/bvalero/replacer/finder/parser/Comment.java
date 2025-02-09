package es.bvalero.replacer.finder.parser;

public record Comment(int start, Statement content) implements Expression {
    @Override
    public int end() {
        return content.end() + "-->".length();
    }

    @Override
    public String text() {
        return "<!--" + content.text() + "-->";
    }

    @Override
    public ExpressionType type() {
        return ExpressionType.COMMENT;
    }
}
