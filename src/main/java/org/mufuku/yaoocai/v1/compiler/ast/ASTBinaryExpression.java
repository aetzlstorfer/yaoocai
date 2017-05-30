package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTBinaryExpression extends ASTExpression {

    private final ASTExpression left;

    private ASTExpression right;

    private ASTOperator operator;

    public ASTBinaryExpression(ASTExpression left) {
        this.left = left;
    }

    public ASTExpression getLeft() {
        return left;
    }

    public ASTExpression getRight() {
        return right;
    }

    public void setRight(ASTExpression right) {
        this.right = right;
    }

    public ASTOperator getOperator() {
        return operator;
    }

    public void setOperator(ASTOperator operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "<Binary Expr>: " + left.toString() + (right != null ? " " + operator.name() + " " + right.toString() : "");
    }
}
