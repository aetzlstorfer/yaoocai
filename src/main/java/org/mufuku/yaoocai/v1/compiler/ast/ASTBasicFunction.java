package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class ASTBasicFunction extends ASTElement {

    private final String identifier;

    private ASTType returnType;

    private ASTParameters parameters;


    ASTBasicFunction(String identifier, int lineNumber) {
        super(lineNumber);
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
