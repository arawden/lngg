package jlox;

import java.util.List;

// LoxFunction: Representing a Lox function in Java
class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  private final boolean isInitializer;

  LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.isInitializer = isInitializer;
    this.closure = closure;
    this.declaration = declaration;
  }

  // Create an environment containing `this` and bind a function to it
  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);

    return new LoxFunction(declaration, environment, isInitializer);
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);

    for (int i = 0; i < declaration.parameters.size(); i++) {
      environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
    }

    // Try the function statement and catch any early returns
    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      if (isInitializer) {
        return closure.getAt(0, "this"); // Init returns `this` instead of `nil`
      }

      return returnValue.value;
    }

    // Init returns `this`
    if (isInitializer) {
      return closure.getAt(0, "this");
    }

    return null;
  }

  @Override
  public int arity() {
    return declaration.parameters.size();
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
