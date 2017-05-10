package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTParameter {

    private final String identifier;

    private final ASTType type;

    public ASTParameter(String identifier, ASTType type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ASTType getType() {
        return type;
    }

    @Override
    public String toString() {
        return identifier + ":" + type;
    }
}
