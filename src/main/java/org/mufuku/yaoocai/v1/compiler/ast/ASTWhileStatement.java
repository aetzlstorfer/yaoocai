package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTWhileStatement extends ASTStatement {

    private final ASTExpression conditionExpression;

    private final ASTBlock block;

    public ASTWhileStatement(ASTExpression conditionExpression, ASTBlock block) {
        this.conditionExpression = conditionExpression;
        this.block = block;
    }
}
