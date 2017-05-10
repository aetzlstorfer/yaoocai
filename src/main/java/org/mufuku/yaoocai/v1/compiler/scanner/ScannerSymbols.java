package org.mufuku.yaoocai.v1.compiler.scanner;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum ScannerSymbols {

    UNKNOWN("unknown character"),
    EOI("end of input"),

    PLUS_OPERATOR("Plus(+) symbol"),
    MINUS_OPERATOR("Minus(-) symbol"),
    DIVISION_OPERATOR("Division(/) symbol"),
    MULTIPLICATION_OPERATOR("Multiplication(*) symbol"),

    COMMA("Comma(,) symbol"),
    SEMICOLON("Semi(;) colon symbol"),
    COLON("Colon(:) symbol"),

    ASSIGNMENT_OPERATOR("Assignment (=) symbol"),

    EQUALITY_OPERATOR("Equality(==) symbol"),
    INEQUALITY_OPERATOR("Inequality(!=) symbol"),
    GREATER_OPERATOR("Greater(>) symbol"),
    GREATER_OR_EQUAL_OPERATOR("Equal or greater (>=) symbol"),
    LESS_OPERATOR("Less(<) symbol"),
    LESS_OR_EQUAL_OPERATOR("Less or greater (<=) symbol"),

    BLOCK_START("Block start({) symbol"),
    BLOCK_END("Block end(}) symbol"),
    PAR_START("Parentheses start ( symbol"),
    PAR_END("Parentheses end ) symbol"),

    BUILTIN("builtin statement"),
    BUILTIN_ASSIGNMENT("builtin assignment (->)"),

    FUNCTION("Function statement"),
    VARIABLE("Variable declaration statement"),
    IF("If statement"),
    ELSE("Else statement"),
    WHILE("While statement"),
    RETURN("Return statement"),
    //    NULL("Null literal"),
    INTEGER("Integer basic type"),
    BOOLEAN("Boolean basic type"),

    TRUE("True literal"),
    FALSE("False literal"),
    INTEGER_LITERAL("Integer literal"),
    STRING_LITERAL("String literal"),

    IDENTIFIER("Identifier");

    private final String symbolDescription;

    ScannerSymbols() {
        this.symbolDescription = null;
    }

    ScannerSymbols(String symbolDescription) {
        this.symbolDescription = symbolDescription;
    }

    @Override
    public String toString() {
        return symbolDescription != null ? symbolDescription : this.name();
    }
}
