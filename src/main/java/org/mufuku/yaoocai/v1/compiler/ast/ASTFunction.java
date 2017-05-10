package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTFunction extends ASTBasicFunction {

    private ASTBlock block;

    public ASTFunction(String identifier) {
        super(identifier);
    }

    public ASTBlock getBlock() {
        return block;
    }

    public void setBlock(ASTBlock block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "<Function>: " + identifier + "(" + parameters + ")" + (returnType != null ? " : " + returnType : "");
    }
}
