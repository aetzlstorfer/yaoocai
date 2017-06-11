# YAOOCAI Programming language

## Table of contents
* [1. Introduction](#1-introduction)
* [2. Language](#2-language)
* [3. Compiler](#3-compiler)
* [4. Runtime](#4-runtime)

## 1. Introduction
_YAOOCAI_ is the short form for **Y**et **a**nother **o**bject **o**riented **c**ompiler **a**nd **i**nterpreter.
This is just a fun and educational project where a small compiler and a _Virtual Machine_ is build without going
too big. The aim is to write code and execute it platform independently.

### 1.1 Project status
**Build**:
![Build status](https://api.travis-ci.org/aetzlstorfer/yaoocai.svg?branch=master)

**Language (high level)**:
 - \+ Functional language part completely implemented
 - \+ Validation on a higher level (Type safety, duplications, proper return flow, ...)
 - \- Object oriented part of the language is missing (no classes, no inheritance, no generics, ...)
 - \- No concept to organize code (modules, name spaces, packages) -\> necessary for runtime!
 - \- Advanced validation missing (Don't skip compilation on first error, bring proper error messages with line number, ...)
 
**Language (control structure)**
 - \+ Standard control structures are implemented (if/else/elseif/while)
 - \- Advanced control structures are missing (break/continue/for-loop)

**Data types and operations**
 - \+ Default data types (integer, boolean) are fully supported with all possible operators
 - \- Advanced data types aren't supported (decimal data type, string, array)

**Byte code**
 - \- No metadata available (e.g. line number, function name, variable name, script name)
 - \- No constant pool for integers

**Virtual Machine**:
 - \- No possibility to print stack trace (need byte code meta data)

**Byte Code Viewer**:
 - \- No function/variable names available (need byte code meta data)

**Runtime**:
 - \+ Built-In _Virtual Machine_ Functions are possible. exit/print is available.
 - \- Extended functionality (also written in _YAOOCAI_ is completely missing)

**Debugger**: completely missing.

**Optimizer**:
 - Compiler: Need for compiler optimization (e.g. short function inlining, short function parameters to not store
   variables in local variable stack, operator optimization, ...)
 - _Virtual Machine_: expressions to use java directly?

## 1.2. Big picture
![Screenshot](readme-files/bigpicture.png)

The object oriented compiler is analyzing .yaoocai source file and checking for
proper syntax. The compiled code is translated into a binary form which
can then be interpreted by a _Virtual Machine_. It is also planned to provide a short
runtime with extended libraries containing at least collections, IO operations and
many other stuff.

Currently everything is written in Java. VM might be a candidate for other languages like C++
(just for the fun)

## 2. Language
### 2.1. Language characteristics
#### 2.1.2. Keywords
These keywords are currently reserved for the language:
 - boolean
 - builtin
 - else
 - false
 - function
 - if
 - integer
 - return
 - true
 - var
 - while
#### 2.1.2. Comments
Two types of comments are supported:

**Line comment**:
```
  function main() {
    // implement code here
  }
```

**Block comment**:
```
  function main() {
    /*
      implement code
      here
    */
  }
```
#### 2.1.3. Control structure
Three control structure's are supported. For loop to be implemented in v1.2 or v1.3.

**If/Else/Else if**:
```
  function main() {
    if(condition) {
      // do something on condtion
    } else if(otherCondition) {
      // do something on the other condition
    } else {
      // do something if conditon and otherCondition do not met
    }
  }
```

**While loop**:
```
  function main() {
    while(conditon) {
      // loop while condition mets
    }
  }
```

#### 2.1.4. Data types and Variables
Type    | Size   | Range               | Supported operators | Operators to be implemented
--------|--------|---------------------|---------------------|-----
integer | 2 Byte | `-32768` to `32767` | `+`, `-`, `*`, `/`, `>`, `<`, `>=`, `<=`, `==`, `!=` | bitwise operators 
boolean | 1 Byte | `true` or `false`   | `&`, <code>&#124;</code>

To be implemented data types:
* String and character combination
* Integer's in different sizes (4byte, 8byte)
* Decimal data types

#### 2.1.5. Literals
Currently two literals are supported:

**Integer**: Just plain numbers
```
  function main() {
    var someValue: integer = 1234;
  }
```

**Boolean**: Just true or false
```
  functon main() {
    var bf: boolean = true;
    var bt: boolean = false;
  }
```

There is no escaping functionality like in other languages that
are aware of exponents or different decimal systems.

#### 2.1.6. Identifier
Valid identifiers are names with the following rules:
* Identifier must start with a *character*, *dollar* or *underscore*
* Followed by *characters*, *digits*, *dollar* or *underscores*
* Identifier cannot be same like a reserved keyword


#### 2.1.7. Operators
These are the supported operators and the operator precedence

| Name                      | Level | Example                                           |
|:--------------------------|------:|:-------------------------------------------------|
| Parentheses               | 1     | `(subexpression)` 
| Post increment/decrement  | 1     | `a++` `a--`  
| Unary plus/minus          | 2     | `b = -a`
| Pre increment             | 2     | `++a` `--a` 
| Negation                  | 2     | `!condition`
| Multiplicative            | 3     | `a * b` `c / d`  `e % 2` 
| Additive                  | 4     | `a + b` `c + d` 
| Comparison                | 5     | `<` `<=` `>` `>=` `!=` `==`
| Bitwise AND               | 6     | `a & b`
| Bitwise OR                | 7     | <code>a &#124; b</code> 
| Conditional AND           | 8     | `a && b`
| Conditional OR            | 9     | <code>a &#124;&#124; b</code>
| Assignment                | 10    | `x = 1` `x += 2` `x -= 3` `x *= 4` `x /= 5` 

Notes:
 * No support for unary plus operator as not needed.

#### 2.1.8. Functions and Procedures
It is possible to write functions and procedures (without return type).

**Function**: Functions have a return type and must have valid return statements.
```
  function min(a: integer, b: integer) {
    if(a < b) {
      return a;
    } else {
      return b;
    }
  }
  
  function main() {
    min(1, 2); // 2
  }
```
**Procedures**: Functions without a return type are called procedures and do not need a return statement. The main function is actually a procedure.
```
  function doSomething() {
    // do something, do not need a return
  }
  
  function main() {
    doSomething();
  }
```

#### 2.1.9. Built-in Functions and Procedures
Currently the connection to the outside can be done over so called *builtin* functions or procedures.
Built-In functions are declared with the `builtin` keyword, an `origin` and the `function index`.

**Declaration examples**:
```
  builtin function func(a: integer, b: boolean): integer -> vm_func(9123)
  builtin function proc(a: integer)                      -> vm_func(9124)
```

**Origin**:
The origin identifier clarifies where the built-in function is coming from.
Currently `vm_func` is the only one supported and means that the functions is coming from the
*Virtual machine*.

Note: It could be that this mechanism could be used for other origins in the future.

**Function index**: The function index is a reference to the function within the origin.

**VM-Funcs**

| Name           | Description | Declaration              |
|----------------|----------------------------------------|---------------------|
| `printInteger` | Prints an integer value to the console | `vm_func(1)`

#### 2.1.10 Runtime
The idea is to provide a runtime written in YAOOCAI language that internally
relies on the builtin/vm_func functions. So the idea is that builtin functions are
marked as deprecated at some time.

TODO document this

see [4. Runtime](#4-runtime)

### 2.2. Grammar
#### 2.2.1. Convention
BNF like description of grammar with these conventions:
* Literals are declared within single quotes. E.g.: `'+'`
* Rule names (left side) are written in diamond brackets. E.g. `<Rule>`
* Rules (right side) contains references to other rules, terminal symbols or non terminal symbols
* Rule names and rules are glued together with assignment operator `::=`
* Possible occurrences: One or zero -> `[Rule]`, Zero or more -> `{Rule}`
* Choices are made with brackets and pipe symbol `(Rule1 | Rule 2)`

#### 2.2.2. Grammar description
Parser Rules
```
<Script>               ::= {BuiltInFunctionDeclaration} {FunctionDeclaration}

<BuiltInFunctionDeclaration>
                       ::= 'builtin' 'function' Identifier Parameters [Colon Type]
<BuiltInFunctionDeclarationAssignment>
                       ::= BuiltInFunctionDeclarationOperator Identifier ParStart {Digit} ParEnd
<FunctionDeclaration>  ::= 'function' Identifier Parameters [Colon Type] Block
<Parameters>           ::= ParStart [ParameterDeclarations] ParEnd
<ParameterDeclarations>::= ParameterDeclaration {Comma ParameterDeclaration}
<ParameterDeclaration> ::= Identifier Colon Type

<Block>                ::= BlockStart {BlockStatement} BlockEnd
<BlockStatement>       ::= (LocalVariableDeclarationStatement | Statement)
<LocalVariableDeclarationStatement>
                       ::= 'var' Identifier Colon Type [Equals Expression] SemiColon
<Statement>            ::= (
                               Block |
                               IfStatement |
                               WhileStatement |
                               ReturnStatement |
                               ExpressionStatement
                           )
<ExpressionStatement>  ::= Expression SemiColon

<IfStatement>          ::= 'if' ParExpression Block {ElseIfBlock} [ElseBlock]
<ElseIfBlock>          ::= 'else' 'if' ParExpression Block
<ElseBlock>            ::= 'else' Block
<WhileStatement>       ::= 'while' ParExpression Block
<ReturnStatement>      ::= 'return' Expression SemiColon

<Expression>           ::= AssignmentExpression
<AssignmentExpression> ::= ConditionalOrExpression [(
                                                        AssignmentOperator |
                                                        AdditionAssignmentOperator |
                                                        SubtractionAssignmentOperator |
                                                        MultiplicationAssignmentOperator |
                                                        DivisionAssignmentOperator
                                                    ) ConditionalOrExpression]
<ConditionalOrExpression>
                       ::= ConditionalAndExpression {ConditionalOrOperator ConditionalAndExpression}
<ConditionalAndExpression>
                       ::= BitwiseOrExpression {ConditionalAndOperator BitwiseOrExpression}
<BitwiseOrExpression>
                       ::= BitwiseAndExpression {BitwiseOrOperator BitwiseAndExpression}
<BitwiseAndExpression>
                       ::= ComparisonExpression {BitwiseAndOperator BitwiseAndExpression}
<ComparisonExpression> ::= AdditiveExpression {(
                                                   EqualsOperator |
                                                   NotEqualsOperator |
                                                   GreaterOperator |
                                                   GreaterOrEqualOperator |
                                                   LessOrEqualOperator |
                                                   LessOperator
                                               ) AdditiveExpression}
<AdditiveExpression>   ::= MultiplicativeExpression {(
                                                         AdditionOperator |
                                                         SubtractionOperator
                                                     ) MultiplicativeExpression}
<MultiplicativeExpression>
                       ::= PreIncrementExpression {(
                                        MultiplicationOperator |
                                        DivisionOperator |
                                        ModuloOperator
                                    ) PreIncrementExpression}
<PreIncrementExpression>
                     ::= [(
                              IncrementOperator |
                              DecrementOperator
                           )] PrefixExpression
<PrefixExpression>   ::= {(
                              NegationOperator |
                              AdditionOperator |
                              SubtractionOperator
                          )} Primary
<Primary>              ::= (
                               Literal |
                               ParExpression |
                               Variable [PostfixOperations] |
                               FunctionCall
                           )
<PostfixOperations>    ::= (
                               IncrementOperator |
                               DecrementOperator
                           )

<ParExpression>        ::= ParStart Expression ParEnd
<Variable>             ::= Identifier
<FunctionCall>         ::= Identifier ParStart [Arguments] ParEnd
<Arguments>            ::= Expression {Comma Expression}
<Literal>              ::= (
                               IntegerLiteral |
                               BooleanLiteral
                           )
<Type>                 ::= (
                               BasicType |
                               ReferenceType
                           )
<ReferenceType>        ::= Identifier // TODO supported for later OO or String
```
Scanner Rules:
```
<Identifier>           ::= Character {Character | Digit}

<BasicType>            ::= 'integer' | 'boolean'
<IntegerLiteral>       ::= {Digit}
<BooleanLiteral>       ::= ('true' | 'false')

<PlusOperator>         ::= '+'
<MinusOperator>        ::= '-'
<MultiplicationOperator>
                       ::= '*'
<DivisionOperator>     ::= '/'
<ModuloOperator>       ::= '%'
<EqualsOperator>       ::= '=' '='
<NotEqualsOperator>    ::= '!' '='

<GreaterOperator>      ::= '>
<GreaterOrEqualOperator>
                       ::= '>' '='
<LessOperator>         ::= '<'
<LessOrEqualOperator>  ::= '<' '='

<AssignmentOperator>   ::= '='
<AdditionAssignmentOperator>
                       ::= '+' '='
<SubtractionAssignmentOperator>
                       ::= '-' '='
<MultiplicationAssignmentOperator>
                       ::= '*' '='
<DivisionAssignmentOperator>
                       ::= '/' '='

<IncrementOperator>    ::= '+' '+'
<DecrementOperator>    ::= '-' '-'

<NegationOperator>     ::= '!'
<BitwiseOrOperator>    ::= '|'
<BitwiseAndOperator>   ::= '&'
<ConditionalOrOperator>::= '|' '|'
<ConditionalAndOperator>
                       ::= '&' '&'

<BuiltInFunctionDeclarationOperator>
                       ::= '-' '>'

<Colon>                ::= ':'
<SemiColon>            ::= ';'
<Comma>                ::= ','

<BlockStart>           ::= '{'
<BlockEnd>             ::= '}'
<ParStart>             ::= '('
<ParEnd>               ::= ')'

<Digit>                ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
<Character>            ::= 'A' | 'B' | 'C' | ... | 'Z' | 'a' | 'b' | 'c' | ... | 'z'
```

### 2.3. Examples
TODO Add basic hello world example when strings are supported
TODO Add other examples as well

## 3. Compiler
Currently there is just an API to call the compiler. It is planned to
have a standalone tool that compiles single .yaoocai files or a whole directory.

## 4. Virtual machine
### 4.1 Introduction
The compiler reads the source code and translates it into the _YAOOCAI_ byte code. This sequence of binary
data can be executed with a _Virtual Machine_ which is a simple stack machine which follows a limited instruction
set (see following section). Each sequence is a Word (2 Bytes) long.

### 4.2 Instruction set
Mnemonic          | OpCode (hex)   | Params         | Stack change                        | Description
------------------|----------------|----------------|-------------------------------------|---
`function`        | `0x0000`       |                |                                     | Indicator for the _Virtual Machine_ to determine where a function starts within the sequence.
`i_const`         | `0x0100`       | `1: value`     | &rarr; `value`                      | Pushes constant `value` onto the stack
`b_const_true`    | `0x0101`       |                | &rarr; `true`                       | Pushes `true` onto the stack
`b_const_false`   | `0x0102`       |                | &rarr; `false`                      | Pushes `false` onto the stack
`store`           | `0x0200`       | `1: index`     | `value` &rarr;                      | Pops `value` and stores the value on local variable stack at position `index`
`load`            | `0x0201`       | `1: index`     | &rarr; `value`                      | Pushes `value` onto the stack from local variable stack from position `index`
`pop`             | `0x0202`       |                | `value` &rarr;                      | Pops the last `value` from the stack.
`invoke`          | `0x0300`       | `1: funcIndex` | `[arg1, arg2, ...]` &rarr; `result` | Invokes function from function index `funcIndex`. Pops the arguments `arg1, arg2, ...` with help of `pop_params`. Pushes the `result` onto the stack with help of the `return` instruction.
`invoke_builtin`  | `0x0301`       | `1: funcIndex` | `[arg1, arg2, ...]` &rarr; `result` | Invokes a built-in function from the _Virtual Machine_ with the index `funcIndex`. The _Virtual Machine_ takes care to pop the arguments `arg1, arg2, ...` and use it for the function call. The `result` internally is pushed onto the stack.
`add`             | `0x0400`       |                | `v1`, `v2` &rarr; `result`          | Adds two integers `v1` and `v2` and pushes the `result` onto the stack.
`sub`             | `0x0401`       |                | `v1`, `v2` &rarr; `result`          | Subtract two integers `v1` and `v2` and pushes the `result` onto the stack.
`mul`             | `0x0402`       |                | `v1`, `v2` &rarr; `result`          | Multiply two integers `v1` and `v2` and pushes the `result` onto the stack.
`div`             | `0x0403`       |                | `v1`, `v2` &rarr; `result`          | Divides two integers `v1` and `v2` and pushes the `result` onto the stack.
`mod`             | `0x0404`       |                | `v1`, `v2` &rarr; `result`          | Divides two integers `v1` and `v2` and pushes the remainder `result` onto the stack (aka modulo).
`neg`             | `0x0405`       |                | `value` &rarr; `result`             | Pops `value` from the stack and pushes the negated `result` onto the stack. 
`cmp_lt`          | `0x0500`       |                | `v1`, `v2` &rarr; `result`          | Pops two integers `v1` and `v2` and checks that that `v1` is less than `v2`. Pushes `true` or `false` onto the stack as `result`
`cmp_lte`         | `0x0501`       |                | `v1`, `v2` &rarr; `result`          | Pops two integers `v1` and `v2` and checks that that `v1` is less than or equal to `v2`. Pushes `true` or `false` onto the stack as `result`
`cmp_gt`          | `0x0502`       |                | `v1`, `v2` &rarr; `result`          | Pops two integers `v1` and `v2` and checks that that `v1` is greater than `v2`. Pushes `true` or `false` onto the stack as `result`
`cmp_gte`         | `0x0503`       |                | `v1`, `v2` &rarr; `result`          | Pops two integers `v1` and `v2` and checks that that `v1` is greater than or equal to `v2`. Pushes `true` or `false` onto the stack as `result`
`cmp_eq`          | `0x0504`       |                | `v1`, `v2` &rarr; `result`          | Pops two integers `v1` and `v2` and checks that that `v1` is equal to `v2`. Pushes `true` or `false` onto the stack as `result`
`cmp_ne`          | `0x0505`       |                | `v1`, `v2` &rarr; `result`          | Pops two integers `v1` and `v2` and checks that that `v1` is not equal to `v2`. Pushes `true` or `false` onto the stack as `result`
`if`              | `0x0600`       | `1: elseJump`  | `condition` &rarr;                  | Pops the `condition` from stack. If `condition` does met the _Virtual Machine_ jumps one instruction further. If the `condition` does not met the _Virtual Machine_ jumps `elseJump` instructions further or back (relative).
`goto`            | `0x0601`       | `1: jump`      |                                     | Jumps `jump` instructions further or back (ative).
`return`          | `0x0602`       |                |                                     | Indicator for the _Virtual Machine_ that the execution of current relfunction ends. Jumps back to last function or if not possible ends the execution of the code.
`pop_params`      | `0x0603`       | `1: params`    | `[arg1, arg2, ...]` &rarr;          | Pops `params` number of items (`arg1, arg2, ...`) from the stack and adds them to the local variable register.
`and`             | `0x0700`       |                | `v1`, `v2` &rarr; `result`          | Pops two booleans `v1` and `v2` performs bitwise *and* operation and pushes the `result` onto the stack.   
`or`              | `0x0701`       |                | `v1`, `v2` &rarr; `result`          | Pops two booleans `v1` and `v2` performs bitwise *or* operation and pushes the `result` onto the stack.
`not`             | `0x0702`       |                | `value` &rarr; `result`             | Pops boolean `value` from the stack performs bitwise *not* operation and pushes the `result` onto the stack.



### 3.3 Byte code grammar
**Byte code**

See [2.2.1. Convention](#221-convention) for convention styles.
```
<Script>              ::= 'yaoocai' ScriptHeader ScriptBody
<ScriptHeader>        ::= VersionData MainFunctionIndex
<VersionData>         ::= Word Word    // major/minor version
<MainFunctionIndex>   ::= Word
<ScriptBody>          ::= {Function}
<Function>            ::= FunctionOpCode {Instruction}
<Instruction>         ::= (
                              ConstantOperations |
                              StackOperations |
                              InvokeOperations |
                              ArithmeticalOperations |
                              CompareOperations |
                              ControlOperations |
                              BitwiseOperations
                          )

<ConstantOperations>  ::= (
                              IntegerConstant |
                              BooleanConstant
                          )
<IntegerConstant>     ::= IntegerConstOpCode value=Byte
<BooleanConstant>     ::= (
                              BooleanTrueOpCode |
                              BooleanFalseOpCode
                          )

<StackOperations>     ::= (
                              StoreOperation |
                              LoadOperation |
                              PopOperation
                          )
<StoreOperation>      ::= <StoreOpCode> index=Word
<LoadOperation>       ::= <LoadOpCode> index=Word
<PopOperation>        ::= <PopOpCode>

<InvokeOperations>    ::= (
                              InvokeOperation |
                              InvokeBuiltinOperation
                          )
<InvokeOperation>     ::= InvokeOpCode funcIndex=Word
<InvokeBuiltinOperation>
                      ::= InvokeBuiltInOpCode funcIndex=Word

<ArithmeticalOperations>
                      ::= (
                              AddOperation |
                              SubOperation |
                              MulOperation |
                              DivOperation |
                              ModOperation |
                              NegOperation
                          )
<AddOperation>        ::= AddOpCode
<SubOperation>        ::= SubOpCode
<MulOperation>        ::= MulOpCode
<DivOperation>        ::= DivOpCode
<ModOperation>        ::= ModOpCode
<NegOperation>        ::= NegOpCode

<CompareOperations>   ::= (
                              LTComparison |
                              LTEComparison |
                              GTComparison |
                              GTEComparison |
                              EQComparison |
                              NEQComparison
                          )
<LTComparison>        ::= LTOpCode
<LTEComparison>       ::= LTEOpCode
<GTComparison>        ::= GTOpCode
<GTEComparison>       ::= GTEOpCode
<EQComparison>        ::= EQOpCode
<NEQComparison>       ::= NEQOpCode

<ControlOperations>   ::= (
                              IfOperation |
                              GotoOperation |
                              ReturnOperation |
                              PopParamsOperation
                          )
<IfOperation>         ::= IfOpCode elseJump=Word
<GotoOperation>       ::= GotoOpCode jump=Word
<ReturnOperation>     ::= ReturnOpCode
<PopParamsOperation>  ::= PopParamsOpCode numParams=Word

<BitwiseOperations>   ::= (
                              BitwiseAndOperation |
                              BitwiseOrOperation |
                              BitwiseNotOperation
                          )
<BitwiseAndOperation> ::= BitwiseAndOpCode
<BitwiseOrOperation>  ::= BitwiseOrOpCode
<BitwiseNotOperation> ::= BitwiseNotOpCode

```
**OpCode-Level**
```
<FunctionOpCode>      ::= 0x0000

<IntegerConstOpCode>  ::= 0x0100
<BooleanTrueOpCode>   ::= 0x0101
<BooleanFalseOpCode>  ::= 0x0102

<StoreOpCode>         ::= 0x0200
<LoadOpCode>          ::= 0x0201
<PopOpCode>           ::= 0x0202

<InvokeOpCode>        ::= 0x0300
<InvokeBuiltInOpCode> ::= 0x0301

<AddOpCode>           ::= 0x0400
<SubOpCode>           ::= 0x0401
<MulOpCode>           ::= 0x0402
<DivOpCode>           ::= 0x0403
<ModOpCode>           ::= 0x0404
<NegOpCode>           ::= 0x0405

<LTOpCode>            ::= 0x0500
<LTEOpCode>           ::= 0x0501
<GTOpCode>            ::= 0x0502
<GTEOpCode>           ::= 0x0503
<EQOpCode>            ::= 0x0504
<NEQOpCode>           ::= 0x0505

<IfOpCode>            ::= 0x0600
<GotoOpCode>          ::= 0x0601
<ReturnOpCode>        ::= 0x0602
<PopParamsOpCode>     ::= 0x0603

<BitwiseAndOpCode>    ::= 0x0700
<BitwiseOrOpCode>     ::= 0x0701
<BitwiseNotOpCode>    ::= 0x0702

<Word>                ::= Byte Byte
```

## 4. Runtime
TODO This is completely open

