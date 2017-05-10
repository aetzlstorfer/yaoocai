package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTBuiltinFunction extends ASTBasicFunction {

    private final short functionCode;
    private final String type;

    public ASTBuiltinFunction(String identifier, short functionCode, String type) {
        super(identifier);
        this.functionCode = functionCode;
        this.type = type;
    }

    public short getFunctionCode() {
        return functionCode;
    }

    public String getType() {
        return type;
    }
}
