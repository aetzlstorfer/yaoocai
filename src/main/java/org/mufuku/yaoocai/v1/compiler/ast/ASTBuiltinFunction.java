package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTBuiltinFunction extends ASTBasicFunction {

    private final String bindName;

    public ASTBuiltinFunction(String identifier, String bindName, int lineNumber) {
        super(identifier, lineNumber);
        this.bindName = bindName;
    }

    public String getBindName() {
        return bindName;
    }
}
