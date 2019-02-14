package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Interpreter: Evaluates syntax tree nodes into values
//
// We use the Visitor abstraction, computing the value for each expression and statement
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  // Fixed reference to outer environment
  final Environment globals = new Environment();

  // Current environment
  private Environment environment = globals;

  // Associate AST node w/ resolved data
  private final Map<Expr, Integer> locals = new HashMap<>();

  Interpreter() {
    // clock() native function
    globals.define("clock", new LoxCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
      }
    });
  }


  // Each visit function evaluates a node of the given type (I think)
  //
  // Starting with Statements

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));

    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    // Declare the classes name in the current environment
    environment.define(stmt.name.lexeme, null);

    Object superclass = null;

    if (stmt.superclass != null) {
      superclass = evaluate(stmt.superclass);

      // Make sure the superclass is actually a class
      if (!(superclass instanceof LoxClass)) {
        throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
      }

      // Create a new environment and store the superclass in it
      environment = new Environment(environment);
      environment.define("super", superclass);
    }

    // Iterate over methods and turn them into LoxFunction objects
    Map<String, LoxFunction> methods = new HashMap<>();

    for (Stmt.Function method : stmt.methods) {
      // Check method name to determine if it is an initializer
      LoxFunction function = new LoxFunction(method, environment, method.name.lexeme.equals("init"));

      methods.put(method.name.lexeme, function);
    }

    // Convert syntax node into runtime interpretation
    LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass) superclass, methods);

    // Pop the superclass environment which now contains all the functions
    if (superclass != null) {
      environment = environment.enclosing;
    }

    environment.assign(stmt.name, klass);

    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);

    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    LoxFunction function = new LoxFunction(stmt, environment, false);
    environment.define(stmt.name.lexeme, function);

    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if(stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }

    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));

    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;

    if (stmt.value != null) {
      value = evaluate(stmt.value);
    }

    throw new Return(value);
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;

    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);

    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }

    return null;
  }

  //
  // Expressions
  //

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    Integer distance = locals.get(expr);

    if (distance != null) {
      // Assign to specified environment
      environment.assignAt(distance, expr.name, value);
    } else {
      globals.assign(expr.name, value);
    }

    return value;
  }

  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case GREATER:
        checkNumberOperands(expr.operator, left, right);

        return (double) left > (double) right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator, left, right);

        return (double) left >= (double) right;
      case LESS:
        checkNumberOperands(expr.operator, left, right);

        return (double) left < (double) right;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator, left, right);

        return (double) left <= (double) right;

      case MINUS:
        checkNumberOperands(expr.operator, left, right);

        return (double) left - (double) right;
      case PLUS:
        // Overloaded for numbers and string concatentation
        if (left instanceof Double && right instanceof Double) {
          return (double) left + (double) right;
        }

        if (left instanceof String && right instanceof String) {
          return (String) left + (String) right;
        }

        throw new RuntimeError(expr.operator,
            "Operands must be two numbers or two strings.");

      case SLASH:
        checkNumberOperands(expr.operator, left, right);

        return (double) left / (double) right;
      case STAR:
        checkNumberOperands(expr.operator, left, right);

        return (double) left * (double) right;

      case BANG_EQUAL:
        return !isEqual(left, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);
    }

    // Unreachable (?)
    return null;
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    // Thing being called
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();

    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.paren, "Can only call functions and classes");
    }

    LoxCallable function = (LoxCallable) callee;

    // Make sure argument count matches declared function arity
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " + function.arity() +
          " arguments but got " + arguments.size() + ".");
    }

    return function.call(this, arguments);
  }

  @Override
  public Object visitGetExpr(Expr.Get expr) {
    Object object = evaluate(expr.object);

    if (object instanceof LoxInstance) {
      return ((LoxInstance) object).get(expr.name);
    }

    throw new RuntimeError(expr.name, "Only instances have properties.");
  }


  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);

    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) {
        return left;
      }
    } else {
      if (!isTruthy(left)) {
        return left;
      }
    }

    return evaluate(expr.right);
  }

  @Override
  public Object visitSetExpr(Expr.Set expr) {
    // Object whose value is being set
    Object object = evaluate(expr.object);

    if (!(object instanceof LoxInstance)) {
      throw new RuntimeError(expr.name, "Only instances have fields.");
    }

    Object value = evaluate(expr.value);
    ((LoxInstance) object).set(expr.name, value);

    return value;
  }

  @Override
  public Object visitSuperExpr(Expr.Super expr) {
    int distance = locals.get(expr);

    // Find surrounding superclass
    LoxClass superclass = (LoxClass) environment.getAt(distance, "super");

    // `this` is one level nearer than `super` environment
    LoxInstance object = (LoxInstance) environment.getAt(distance - 1, "this");

    LoxFunction method = superclass.findMethod(object, expr.method.lexeme);

    if (method == null) {
      throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
    }

    return method;
  }

  @Override
  public Object visitThisExpr(Expr.This expr) {
    return lookUpVariable(expr.keyword, expr);
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -(double)right;
    }

    // Unreachable (?)
    return null;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return lookUpVariable(expr.name, expr);
  }

  //
  // Helpers
  //

  // Send the Expression back to the visitor implementation
  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  // Statement version of evaluate()
  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  // Validate operands for binary expressions
  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) {
      return;
    }

    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  // Check for single operand in unary expression
  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double){
      return;
    }

    throw new RuntimeError(operator, "Operand must be a number.");
  }

  // Lox equality
  private boolean isEqual(Object a, Object b) {
    // nil is only equal to nil
    if (a == null && b == null) {
      return true;
    }
    if (a == null) {
      return false;
    }

    return a.equals(b); // Java Object equals
  }

  // False & nil are false, everything else is true
  private boolean isTruthy(Object object) {
    if(object == null) {
      return false;
    }
    if(object instanceof Boolean) {
      return (boolean) object;
    }

    return true;
  }

  // Finds a variable's value and returns it
  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);

    if (distance != null) {
      // Local variables have been resolved
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name); // Globals haven't been resolved and don't end up in the map
    }
  }

  // Convert a Lox value to a string for printing
  private String stringify(Object object) {
    if (object == null) {
      return "nil";
    }

    // Work around doubles showing .0 for integers
    if (object instanceof Double) {
      String text = object.toString();

      if (text.endsWith(".0")) {
        text = text.substring(0, text.length()-2);
      }

      return text;
    }

    return object.toString();
  }

  //
  // Execution functions
  //

  // Execute statements in a block within the context of the given environment
  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;

    try {
      this.environment = environment;

      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  // Store number of environments between current environment and scope where variable is defined
  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  // Executes a set of statements
  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }
}
