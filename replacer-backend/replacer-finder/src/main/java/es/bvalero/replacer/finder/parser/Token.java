package es.bvalero.replacer.finder.parser;

class Token {
    final TokenType type;
    final String lexeme;
    final int start;

    Token(TokenType type, String lexeme, int start) {
        this.type = type;
        this.lexeme = lexeme;
        this.start = start;
    }

    int end() {
        return start + lexeme.length();
    }

    public String toString() {
        return type + "+++" + lexeme + "+++";
    }
}
