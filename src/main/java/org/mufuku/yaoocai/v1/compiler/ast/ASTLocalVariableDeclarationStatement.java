package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTLocalVariableDeclarationStatement extends ASTNamedAndTypedElement implements ASTStatement {

    private ASTExpression initializationExpression;

    public ASTLocalVariableDeclarationStatement(Integer lineNumber, String identifier, ASTType type) {
        super(lineNumber, identifier, type);
    }

    public ASTExpression getInitializationExpression() {
        return initializationExpression;
    }

    public void setInitializationExpression(ASTExpression initializationExpression) {
        this.initializationExpression = initializationExpression;
    }
}
