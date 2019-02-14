package jlox;

// RuntimeError: Identifies tokens where a runtime error occurred
class RuntimeError extends RuntimeException {
  final Token token;

  RuntimeError(Token token, String message) {
    super(message);
    this.token = token;
  }
}
