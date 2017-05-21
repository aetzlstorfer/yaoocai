package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTIfStatement extends ASTBaseIfStatement {

    private List<ASTBaseIfStatement> elseIfStatements = new ArrayList<>();

    private ASTBaseIfStatement elseBlockStatement;

    public ASTIfStatement(ASTExpression conditionExpression, ASTBlock block) {
        super(conditionExpression, block);
    }

    public void setElseBlockStatement(ASTBaseIfStatement elseBlockStatement) {
        this.elseBlockStatement = elseBlockStatement;
    }

    public void addElseIfBllock(ASTBaseIfStatement elseIfStatement) {
        elseIfStatements.add(elseIfStatement);
    }

    public List<ASTBaseIfStatement> getStatements() {
        List<ASTBaseIfStatement> statements = new ArrayList<>();
        statements.add(this);
        statements.addAll(elseIfStatements);
        if (elseBlockStatement != null) {
            statements.add(elseBlockStatement);
        }
        return statements;
    }
}
