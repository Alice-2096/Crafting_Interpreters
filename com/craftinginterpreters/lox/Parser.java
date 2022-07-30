package com.craftinginterpreters.lox;
import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;

// a parser maps tokens in the string to terminals in the grammar to figure out which rules could have generated that string.
public class Parser {
    private final List<Token> tokens;   //a string is a sequence of tokens 
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    } 

    //translate grammatical rules into java code 
    private Expr expression() {
        return equality();
    }
    //parsing a grammatical rule produces a syntax tree for that rule and return it to the caller 
    //equality
    private Expr equality() {
        Expr expr = comparison();
    
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
          Token operator = previous();
          Expr right = comparison();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    private Expr comparison() {
        Expr expr = term();
    
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
          Token operator = previous();
          Expr right = term();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }
    
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
    
        if (match(NUMBER, STRING)) {
          return new Expr.Literal(previous().literal);
        }
    
        if (match(LEFT_PAREN)) {
          Expr expr = expression();
          consume(RIGHT_PAREN, "Expect ')' after expression.");
          return new Expr.Grouping(expr);
        }
      }
}
