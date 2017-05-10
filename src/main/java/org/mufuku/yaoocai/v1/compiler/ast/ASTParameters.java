package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTParameters implements Iterable<ASTParameter> {

    private final List<ASTParameter> astParameters = new ArrayList<>();

    public void addParameter(ASTParameter astParameter) {
        this.astParameters.add(astParameter);
    }

    public ASTParameter getParameter(String identifier) {
        return astParameters.stream().filter(p -> p.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    public int getParameterSize() {
        return astParameters.size();
    }

    @Override
    public Iterator<ASTParameter> iterator() {
        return astParameters.iterator();
    }

    @Override
    public String toString() {
        return astParameters.toString();
    }
}
