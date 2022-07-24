class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;  // needed for error reporting and handling
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}

