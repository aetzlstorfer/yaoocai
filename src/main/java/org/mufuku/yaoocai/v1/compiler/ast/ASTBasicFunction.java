package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class ASTBasicFunction {

    protected final String identifier;

    protected ASTType returnType;

    protected ASTParameters parameters;

    public ASTBasicFunction(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ASTType getReturnType() {
        return returnType;
    }

    public void setReturnType(ASTType returnType) {
        this.returnType = returnType;
    }

    public ASTParameters getParameters() {
        return parameters;
    }

    public void setParameters(ASTParameters parameters) {
        this.parameters = parameters;
    }
}
