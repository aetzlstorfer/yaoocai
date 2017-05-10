package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTIfStatement extends ASTStatement {

    private final ASTExpression conditionExpression;

    private final ASTBlock block;

    private ASTBlock elseBlock;

    public ASTIfStatement(ASTExpression conditionExpression, ASTBlock block) {
        this.conditionExpression = conditionExpression;
        this.block = block;
    }

    public ASTExpression getConditionExpression() {
        return conditionExpression;
    }

    public ASTBlock getBlock() {
        return block;
    }

    public ASTBlock getElseBlock() {
        return elseBlock;
    }

    public void setElseBlock(ASTBlock elseBlock) {
        this.elseBlock = elseBlock;
    }
}
