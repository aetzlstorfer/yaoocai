package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTBlock implements ASTStatement, Iterable<ASTStatement> {

    private final List<ASTStatement> statements = new ArrayList<>();

    public void addStatement(ASTStatement astStatement) {
        statements.add(astStatement);
    }

    @Override
    public Iterator<ASTStatement> iterator() {
        return statements.iterator();
    }

    public boolean isEmpty() {
        return statements.isEmpty();
    }

    public ASTStatement getLastStatement() {
        return statements.get(statements.size() - 1);
    }
}
