package es.bvalero.replacer.finder.parser;

import org.jetbrains.annotations.NotNull;

record Token(TokenType type, int start, int end) implements Comparable<Token> {
    @Override
    public int compareTo(@NotNull Token o) {
        return this.start - o.start;
    }
}
