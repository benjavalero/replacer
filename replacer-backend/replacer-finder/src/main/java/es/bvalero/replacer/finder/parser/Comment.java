package es.bvalero.replacer.finder.parser;

record Comment(int start, int end, Iterable<Expression> content) implements Expression {
    @Override
    public ExpressionType type() {
        return ExpressionType.COMMENT;
    }

    @Override
    public Iterable<Expression> nested() {
        return content;
    }
}
