package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTLiteralExpression<T> extends ASTExpression {

    private final T value;

    private final ASTType type;

    public ASTLiteralExpression(T value, ASTType type, int lineNumber) {
        super(lineNumber);
        this.value = value;
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public ASTType getType() {
        return type;
    }
}
