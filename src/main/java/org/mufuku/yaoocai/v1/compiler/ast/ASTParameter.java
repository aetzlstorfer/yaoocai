package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTParameter extends ASTNamedAndTypedElement {
    public ASTParameter(Integer lineNumber, String identifier, ASTType type) {
        super(lineNumber, identifier, type);
    }
}
