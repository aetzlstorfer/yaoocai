package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTReturnStatement implements ASTStatement {

    private final ASTExpression expression;

    public ASTReturnStatement(ASTExpression expression) {
        this.expression = expression;
    }

    public ASTExpression getExpression() {
        return expression;
    }
}
