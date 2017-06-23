package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTType;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class LocalVariable {

    private final ASTType type;
    private final short index;
    private boolean initialized;

    LocalVariable(ASTType type, short index) {
        this.type = type;
        this.index = index;
    }

    ASTType getType() {
        return type;
    }

    short getIndex() {
        return index;
    }

    boolean isInitialized() {
        return initialized;
    }

    void setInitialized() {
        this.initialized = true;
    }
}
