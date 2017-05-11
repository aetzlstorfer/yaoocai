package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTUnaryExpression extends ASTExpression {

    private final ASTExpression subExpression;

    private final ASTOperator unaryOperator;

    public ASTUnaryExpression(ASTExpression subExpression, ASTOperator unaryOperator) {
        this.subExpression = subExpression;
        this.unaryOperator = unaryOperator;
    }

    public ASTExpression getSubExpression() {
        return subExpression;
    }

    public ASTOperator getUnaryOperator() {
        return unaryOperator;
    }
}
