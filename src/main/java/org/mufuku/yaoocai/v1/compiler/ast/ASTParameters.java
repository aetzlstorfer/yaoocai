package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTParameters implements Iterable<ASTParameter> {

    private final List<ASTParameter> parameters = new ArrayList<>();

    public void addParameter(ASTParameter astParameter) {
        this.parameters.add(astParameter);
    }

    public int getParameterSize() {
        return parameters.size();
    }

    @Override
    public Iterator<ASTParameter> iterator() {
        return parameters.iterator();
    }
}
