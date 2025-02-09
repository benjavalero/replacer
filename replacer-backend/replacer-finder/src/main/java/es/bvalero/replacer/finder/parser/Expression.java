package es.bvalero.replacer.finder.parser;

import java.util.List;

interface Expression {
    int start();
    int end();
}

record Text(int start, String text) implements Expression {
    @Override
    public int end() {
        return start + text.length();
    }
}

record Statement(int start, List<Expression> expressions) implements Expression {
    @Override
    public int end() {
        return this.expressions.get(this.expressions.size() - 1).end();
    }
}

record Comment(int start, Statement content) implements Expression {
    @Override
    public int end() {
        return content.end() + "-->".length();
    }
}
