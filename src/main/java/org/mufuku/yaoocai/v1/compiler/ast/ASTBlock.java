package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTBlock extends ASTStatement implements Iterable<ASTStatement> {

    private final List<ASTStatement> statements = new ArrayList<>();

    public void addStatement(ASTStatement astStatement) {
        statements.add(astStatement);
    }

    @Override
    public Iterator<ASTStatement> iterator() {
        return statements.iterator();
    }
}
