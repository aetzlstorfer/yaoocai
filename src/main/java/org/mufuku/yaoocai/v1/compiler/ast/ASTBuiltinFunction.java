package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTBuiltinFunction extends ASTBasicFunction {

    private final short functionCode;

    public ASTBuiltinFunction(String identifier, short functionCode) {
        super(identifier);
        this.functionCode = functionCode;
    }

    public short getFunctionCode() {
        return functionCode;
    }
}
