package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum ASTOperator {

    // comparison
    EQUAL,
    NOT_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,

    // assignments
    ASSIGNMENT,
    ADDITION_ASSIGNMENT,
    SUBTRACTION_ASSIGNMENT,
    MULTIPLICATION_ASSIGNMENT,
    DIVISION_ASSIGNMENT,

    // arithmetic
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    MODULO,

    // arithmetic unary
    NEGATE,

    // unary arithmetic
    PRE_INCREMENT,
    PRE_DECREMENT,

    // conditional
    CONDITIONAL_OR,
    CONDITIONAL_AND,

    // bitwise
    BITWISE_OR,
    BITWISE_AND,

    // bitwise unary
    BITWISE_NEGATE

}
