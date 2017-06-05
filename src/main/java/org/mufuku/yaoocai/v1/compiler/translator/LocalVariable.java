package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTType;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class LocalVariable {

    private final String name;
    private final ASTType type;
    private final short index;

    LocalVariable(String name, ASTType type, short index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    String getName() {
        return name;
    }

    ASTType getType() {
        return type;
    }

    short getIndex() {
        return index;
    }
}
