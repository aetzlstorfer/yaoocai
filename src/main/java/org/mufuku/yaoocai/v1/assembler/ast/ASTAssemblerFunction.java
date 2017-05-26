package org.mufuku.yaoocai.v1.assembler.ast;

import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTAssemblerFunction {

    private final List<Short> instructions;

    public ASTAssemblerFunction(List<Short> instructions) {
        this.instructions = instructions;
    }

    public List<Short> getInstructions() {
        return instructions;
    }
}
