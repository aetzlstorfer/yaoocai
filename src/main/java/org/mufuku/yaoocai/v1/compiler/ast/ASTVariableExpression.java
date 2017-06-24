package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTVariableExpression extends ASTExpression {

    private final String identifier;

    public ASTVariableExpression(String identifier, int lineNumber) {
        super(lineNumber);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
