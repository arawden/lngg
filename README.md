# LNGG
http://craftinginterpreters.com/introduction.html

Up to part [III](http://craftinginterpreters.com/a-bytecode-virtual-machine.html)

## Notes
I do not use the standard package name conventions, I just used the name of the directory. Everything was done in
vim/terminal, there was no IDE use.

I try to follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- I strongly agree with section 4.1, Braces
- 4.6.2 item 1, separating reserve words. [@j-haines](https://github.com/j-haines) explained this in
a way which helped: "It distinguishes `if` as a control flow keyword rather than a function call, and
is more semantically consistent in languages where parens are optional"
- I try to follow 4.8.7
- I don't follow any of the naming conventions; I want to avoid mismatches between my code and the
code in the book
- I don't use Javadoc

At first I wanted to create a copy of the grammar, and then match things like the GenerateAST to the
grammar order. It makes way more sense to leave them as they are, in alphabetical order.

I think the most difficult part of understanding this is the Visitor pattern.

## AST types
["Also, as we add new syntax tree types, I won’t bother showing the necessary visit methods for them in AstPrinter. If you want to (and you want the Java compiler to not yell at you), go ahead and add them yourself."](https://github.com/munificent/craftinginterpreters/blob/master/java/com/craftinginterpreters/lox/AstPrinter.java)

Use `diff <(grep -o 'visit.[a-z]*' AstPrinter.java) <(grep -o 'visit.[a-z]*' Interpreter.java)` to check which functions haven't been covered in `AstPrinter.java`

## Running the Interpreter
From `.`:
- `javac jlox/*.java`
- `java jlox.Lox`

## Generating Syntax Tree Classes
From `jlox/`:
- `javac ./tool/GenerateAst.java`
- `java tool.GenerateAst .`

## TODO:
[x] compile a copy of the grammar
[x] clean up & comment https://google.github.io/styleguide/javaguide.html
