package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTType;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class LocalVariable {

    private final String name;
    private final ASTType type;
    private final short index;

    public LocalVariable(String name, ASTType type, short index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public ASTType getType() {
        return type;
    }

    public short getIndex() {
        return index;
    }
}
