package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTNamedElement extends ASTElement {

    private final String identifier;

    ASTNamedElement(Integer lineNumber, String identifier) {
        super(lineNumber);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
