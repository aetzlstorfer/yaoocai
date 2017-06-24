package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTFunction extends ASTBasicFunction {

    private ASTBlock block;

    public ASTFunction(String identifier, int lineNumber) {
        super(identifier, lineNumber);
    }

    public ASTBlock getBlock() {
        return block;
    }

    public void setBlock(ASTBlock block) {
        this.block = block;
    }
}
