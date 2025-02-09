package es.bvalero.replacer.finder.parser;

import java.util.ArrayList;
import java.util.List;

abstract class Expression {

}

class Statement extends Expression {
    List<Expression> expressions = new ArrayList<>();

    public String toString() {
        return String.join("\n", expressions.stream().map(Object::toString).toList());
    }
}

class Text extends Expression {
    String text;

    public String toString() {
        return "TEXT " + text;
    }
}

class Comment extends Expression {
    Statement content;

    public String toString() {
        return "COMMENT " + content;
    }
}
