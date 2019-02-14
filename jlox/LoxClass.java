package jlox;

import java.util.List;
import java.util.Map;

// LoxClass: Runtime representation of a class
class LoxClass implements LoxCallable {
  final String name;
  final LoxClass superclass;

  private final Map<String, LoxFunction> methods;

  LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
    this.superclass = superclass;
    this.name = name;
    this.methods = methods;
  }

  // Create a new instance of a class by using a class expression on the class object
  public Object call(Interpreter interpreter, List<Object> arguments) {
    // Instantiates a new LoxInstance for called class
    LoxInstance instance = new LoxInstance(this);

    LoxFunction initializer = methods.get("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments); // Bind and invoke initializer
    }

    return instance;
  }
  // Find a method attached to a class
  LoxFunction findMethod(LoxInstance instance, String name) {
    if (methods.containsKey(name)) {
      return methods.get(name).bind(instance); // Bind method to `this` instance
    }

    // If we don't find a method in an instance, check the superclass
    if (superclass != null) {
      return superclass.findMethod(instance, name);
    }

    return null;
  }

  @Override
  public int arity() {
    LoxFunction initializer = methods.get("init");
    if (initializer == null) {
      return 0;
    }

    return initializer.arity(); // How many arguments to pass when you call the class itself
  }

  @Override
  public String toString() {
    return name;
  }
}
