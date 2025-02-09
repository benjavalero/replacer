package es.bvalero.replacer.finder.parser;

record Token(TokenType type, int start, String text) {
    int end() {
        return start + text.length();
    }
}
