package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTArguments implements Iterable<ASTExpression> {

    private final List<ASTExpression> expressions = new ArrayList<>();

    public void addArgument(ASTExpression expression) {
        expressions.add(expression);
    }

    public int getArgumentsSize() {
        return expressions.size();
    }

    @Override
    public Iterator<ASTExpression> iterator() {
        return expressions.iterator();
    }
}
