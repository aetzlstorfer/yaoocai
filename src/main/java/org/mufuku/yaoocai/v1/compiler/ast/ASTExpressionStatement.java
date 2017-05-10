package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTExpressionStatement extends ASTStatement {

    private final ASTExpression expression;

    public ASTExpressionStatement(ASTExpression expression) {
        this.expression = expression;
    }

    public ASTExpression getExpression() {
        return expression;
    }
}
