package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTFunctionCallExpression extends ASTExpression {

    private final String functionName;
    private ASTArguments arguments;

    public ASTFunctionCallExpression(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public ASTArguments getArguments() {
        return arguments;
    }

    public void setArguments(ASTArguments arguments) {
        this.arguments = arguments;
    }
}
