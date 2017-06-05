package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTType {

    private final String typeName;

    private final boolean primitive;

    public ASTType(String typeName, boolean primitive) {
        this.typeName = typeName;
        this.primitive = primitive;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isPrimitive() {
        return primitive;
    }
}
