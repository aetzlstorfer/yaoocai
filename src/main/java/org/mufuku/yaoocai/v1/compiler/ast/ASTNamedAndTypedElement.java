package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTNamedAndTypedElement extends ASTNamedElement {

    private final ASTType type;

    ASTNamedAndTypedElement(Integer lineNumber, String identifier, ASTType type) {
        super(lineNumber, identifier);
        this.type = type;
    }

    public ASTType getType() {
        return type;
    }
}
