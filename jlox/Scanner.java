package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

// Scanner: Takes in raw source code and groups into Tokens
class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  // Reserved words
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

  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // Beginning of next lexeme
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  // Scan an individual token
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;

      // Equality
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

      // Comments
      case '/':
        if (match('/')) {
          // Comment goes until end of line
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;

      // Whitespace
      case ' ':
      case '\r':
      case '\t':
        break; // Ignore whitespace (fall through)
      case '\n':
        line++;
        break;

      case '"':
        string();
        break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier(); // Assume any lexeme with number or _ is an identifier
        } else {
          Lox.error(line, "Unexpected character."); // Character is not valid
        }
        break;
    }
  }

  private void identifier() {
    while (isAlphanumeric(peek())) {
      advance();
    }

    // Is it a reserved word?
    String text = source.substring(start, current);

    TokenType type = keywords.get(text);
    if (type == null) {
      type = IDENTIFIER;
    }

    addToken(type);
  }

  private void number() {
    while (isDigit(peek())) {
      advance();
    }

    // Look for decimal
    if (peek() == '.' && isDigit(peekNext())) {
      advance(); // Consume the '.'

      while (isDigit(peek())) {
        advance();
      }
    }

    // Numbers are represented as Java doubles
    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string() {
    // Multi-line strings
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // Closing "
    advance();

    // Trim surrounding quotes
    String value = source.substring(start+1, current-1);

    addToken(STRING, value);
  }

  // Does this character match what we expected?
  private boolean match(char expected) {
    if (isAtEnd()) {
      return false;
    }

    if (source.charAt(current) != expected) {
      return false;
    }

    current++;
    return true;
  }

  // Lookahead
  private char peek() {
    if (isAtEnd()) {
      return '\0';
    }

    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()){
      return '\0';
    }

    return source.charAt(current+1);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
           c == '_';
  }

  private boolean isAlphanumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  // Have we consumed all characters?
  private boolean isAtEnd() {
    return current >= source.length();
  }

  // Read next charactrer
  private char advance() {
    current++;
    return source.charAt(current-1);
  }

  // Create a token
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  // Create a token with a literal value
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
