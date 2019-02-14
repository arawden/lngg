package jlox;

import java.util.List;

// LoxCallable: Java representation of any Lox objct that can be called like a function
interface LoxCallable {
  int arity();

  Object call(Interpreter interpreter, List<Object> arguments);
}
