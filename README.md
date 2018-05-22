# LNGG
http://craftinginterpreters.com/introduction.html

Up to 10 http://craftinginterpreters.com/functions.html

### AST types
["Also, as we add new syntax tree types, I won’t bother showing the necessary visit methods for them in AstPrinter. If you want to (and you want the Java compiler to not yell at you), go ahead and add them yourself."](https://github.com/munificent/craftinginterpreters/blob/master/java/com/craftinginterpreters/lox/AstPrinter.java)

### Running the Interpreter
From `.`:
- `javac jlox/*.java`
- `java jlox.Lox`

### Generating Syntax Tree Classes
From `jlox/`:
- `javac ./tool/GenerateAst.java`
- `java tool.GenerateAst .`

TODO:
- clean up & comment https://google.github.io/styleguide/javaguide.html
- compile a copy of the grammar rules
