package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.Objects;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTType {

    public static final ASTType BOOLEAN = new ASTType("boolean", true);
    public static final ASTType INTEGER = new ASTType("integer", true);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASTType type = (ASTType) o;
        return primitive == type.primitive &&
                Objects.equals(typeName, type.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, primitive);
    }
}
