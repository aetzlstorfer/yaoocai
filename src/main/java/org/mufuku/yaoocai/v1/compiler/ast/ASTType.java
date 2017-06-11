package org.mufuku.yaoocai.v1.compiler.ast;

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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ASTType)) return false;
        ASTType type = (ASTType) o;
        return primitive == type.primitive && typeName.equals(type.typeName);
    }

    @Override
    public int hashCode() {
        int result = typeName.hashCode();
        result = 31 * result + (primitive ? 1 : 0);
        return result;
    }
}
