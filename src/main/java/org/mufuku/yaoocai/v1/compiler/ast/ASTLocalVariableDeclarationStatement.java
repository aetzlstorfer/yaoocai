package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTLocalVariableDeclarationStatement implements ASTStatement {

    private final String identifier;

    private final ASTType type;

    private ASTExpression initializationExpression;

    public ASTLocalVariableDeclarationStatement(String identifier, ASTType type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ASTType getType() {
        return type;
    }

    public ASTExpression getInitializationExpression() {
        return initializationExpression;
    }

    public void setInitializationExpression(ASTExpression initializationExpression) {
        this.initializationExpression = initializationExpression;
    }
}
