package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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

    // conditional
    CONDITIONAL_OR,
    CONDITIONAL_AND,

    // bitwise
    BITWISE_OR,
    BITWISE_AND;

    public static final Set<ASTOperator> ASSIGNMENT_OPERATORS = Collections.unmodifiableSet(EnumSet.of(
            ASSIGNMENT,
            ADDITION_ASSIGNMENT,
            SUBTRACTION_ASSIGNMENT,
            MULTIPLICATION_ASSIGNMENT,
            DIVISION_ASSIGNMENT));

    public static final Set<ASTOperator> COMPARISON_OPERATORS = Collections.unmodifiableSet(EnumSet.of(
            EQUAL,
            NOT_EQUAL,
            GREATER_THAN,
            GREATER_THAN_OR_EQUAL,
            LESS_THAN,
            LESS_THAN_OR_EQUAL));
}
