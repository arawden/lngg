package jlox;

import java.util.HashMap;
import java.util.Map;

// Environment: Variables and the scope they inhabit
class Environment {
  // Variables in this scope
  private final Map<String, Object> values = new HashMap<>();

  // Enclosing scope for this environment
  final Environment enclosing;

  // Global scope
  Environment() {
    enclosing = null;
  }

  // Local scope nested in given outer scope
  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  // Walks chain and returns specified environment
  Environment ancestor(int distance) {
    Environment environment = this;

    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }

    return environment;
  }

  // Variable definition
  void define(String name, Object value) {
    values.put(name, value);
  }

  // Assign a value to a variable
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);

      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value); // Variable exists in a higher scope

      return;
    }

    // Token does not exist
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  // Assign a variable within a specified environment
  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

  // Retrieve value from existing variable
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    if (enclosing != null) {
      return enclosing.get(name); // Walk the chain of enclosing environments
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  // Retrieve a variable from a specified environment
  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }
}
