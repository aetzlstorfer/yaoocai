package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum ASTUnaryOperator {

    // arithmetic
    NEGATE,

    PRE_INCREMENT,
    PRE_DECREMENT,

    POST_DECREMENT,
    POST_INCREMENT,

    // bitwise
    BITWISE_NOT;

    public static final Set<ASTUnaryOperator> INCREMENT_AND_DECREMENT_OPERATORS = Collections.unmodifiableSet(EnumSet.of(
            PRE_INCREMENT,
            PRE_DECREMENT,
            POST_INCREMENT,
            POST_DECREMENT));

}
