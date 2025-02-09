package es.bvalero.replacer.finder.parser;

public record Token(TokenType type, int start, String text) {
    int end() {
        return start + text.length();
    }
}
