package org.mufuku.yaoocai.v1.compiler.scanner;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum ScannerSymbols {

    UNKNOWN("unknown character"),
    EOI("end of input"),

    ADDITION_OPERATOR("Plus(+) symbol"),
    SUBTRACTION_OPERATOR("Minus(-) symbol"),
    DIVISION_OPERATOR("Division(/) symbol"),
    MULTIPLICATION_OPERATOR("Multiplication(*) symbol"),
    MODULO_OPERATOR("Modulo(%) symbol"),

    COMMA("Comma(,) symbol"),
    SEMICOLON("Semi(;) colon symbol"),
    COLON("Colon(:) symbol"),

    ASSIGNMENT_OPERATOR("Assignment (=) symbol"),
    ADDITION_ASSIGNMENT_OPERATOR("Addition assignment (+=) symbol"),
    SUBTRACTION_ASSIGNMENT_OPERATOR("Subtraction assignment (-=) symbol"),
    MULTIPLICATION_ASSIGNMENT_OPERATOR("Multiplication assignment (*=) symbol"),
    DIVISION_ASSIGNMENT_OPERATOR("Division assignment (/=) symbol"),

    INCREMENT_OPERATOR("Increment (++) symbol"),
    DECREMENT_OPERATOR("Increment (++) symbol"),

    EQUALITY_OPERATOR("Equality(==) symbol"),
    INEQUALITY_OPERATOR("Inequality(!=) symbol"),
    GREATER_OPERATOR("Greater(>) symbol"),
    GREATER_OR_EQUAL_OPERATOR("Equal or greater (>=) symbol"),
    LESS_OPERATOR("Less(<) symbol"),
    LESS_OR_EQUAL_OPERATOR("Less or greater (<=) symbol"),

    BITWISE_NEGATION_OPERATOR("Bitwise negation(!) symbol"),
    BITWISE_OR_OPERATOR("Bitwise or (|) symbol"),
    BITWISE_AND_OPERATOR("Bitwise and (&) symbol"),

    CONDITIONAL_OR_OPERATOR("Conditional or (||) symbol"),
    CONDITIONAL_AND_OPERATOR("Conditional and (&&) symbol"),

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
