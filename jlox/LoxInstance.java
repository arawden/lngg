package jlox;

import java.util.HashMap;
import java.util.Map;

// LoxInstance: Runtime representation of an instance of a Lox class
class LoxInstance {
  private LoxClass klass;

  private final Map<String, Object> fields = new HashMap<>();

  LoxInstance(LoxClass klass) {
    this.klass = klass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    // If we don't find a field, find a method
    LoxFunction method = klass.findMethod(this, name.lexeme);
    if (method != null) {
      return method;
    }

    // Name refers to niether a field or method
    throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return klass.name + " instance";
  }
}
