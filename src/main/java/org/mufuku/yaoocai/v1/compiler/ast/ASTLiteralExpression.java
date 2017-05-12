package org.mufuku.yaoocai.v1.compiler.ast;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTLiteralExpression<T> extends ASTExpression {

    private final T value;

    public ASTLiteralExpression(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "<Literal>: " + value;
    }
}
