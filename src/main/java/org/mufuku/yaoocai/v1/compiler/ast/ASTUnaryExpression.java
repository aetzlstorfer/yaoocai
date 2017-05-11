package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTUnaryExpression extends ASTExpression {

    private final ASTExpression subExpression;

    private final ASTUnaryOperator unaryOperator;

    public ASTUnaryExpression(ASTExpression subExpression, ASTUnaryOperator unaryOperator) {
        this.subExpression = subExpression;
        this.unaryOperator = unaryOperator;
    }

    public ASTExpression getSubExpression() {
        return subExpression;
    }

    public ASTUnaryOperator getUnaryOperator() {
        return unaryOperator;
    }
}
