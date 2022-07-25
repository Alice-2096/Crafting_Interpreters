package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*; 

class Scanner {
    //our source code is stored in the form of a string.
    private final String source;
    //a container is needed to store our tokens 
    private final List<Token> tokens = new ArrayList<>();
    //help us keep track of where the scanner is in the source code.
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source; 
    }

    //the list of all keyword-tokenType pairs  
    private static final Map<String, TokenType> keywords;
    
    static {
      keywords = new HashMap<>();
      keywords.put("and",    AND);
      keywords.put("class",  CLASS);
      keywords.put("else",   ELSE);
      keywords.put("false",  FALSE);
      keywords.put("for",    FOR);
      keywords.put("fun",    FUN);
      keywords.put("if",     IF);
      keywords.put("nil",    NIL);
      keywords.put("or",     OR);
      keywords.put("print",  PRINT);
      keywords.put("return", RETURN);
      keywords.put("super",  SUPER);
      keywords.put("this",   THIS);
      keywords.put("true",   TRUE);
      keywords.put("var",    VAR);
      keywords.put("while",  WHILE);
    }

    private char advance() {
        return source.charAt(current++);
    }
    
    // consume the current character if it’s what we’re looking for.
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    //creates a token for each lexeme 
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
    // look ahead but does not consume the character
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
          if (peek() == '\n') line++;
          advance();
        }
    
        if (isAtEnd()) {
          Lox.error(line, "Unterminated string.");
          return;
        }

        // The closing ".
        advance();
    
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    } 
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    } 
    //consume a number 
    private void number() {
        while (isDigit(peek())) advance();
    
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            while (isDigit(peek())) advance();
        }
        //using Java’s own parsing method to convert the lexeme to a real Java double. 
        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
    }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
      }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        //check if the token is a keyword 
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER; //token is an identifier if not found in the list of keywords
        addToken(type);
    }

    // consume the next lexeme and pick a token type for it. 
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break; 
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
            break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                  // A comment goes until the end of the line.
                  while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                  addToken(SLASH);
                }
                break;
            //string literals always start with "
            case '"': string(); break;
            //do nothing to the following.. we want to ignore them 
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            default: 
                if (isDigit(c)){
                    number(); 
                } else if (isAlpha(c)){
                    identifier(); 
                } else {
                    //report error if we read in an unrecognized token 
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }

    //scan a token in each iteration, add it to the list; add EOF when we reach the end
    List<Token> scanTokens() {
        while (!isAtEnd()) {
        // We are at the beginning of the next lexeme.
        start = current;
        scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

}

