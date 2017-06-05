package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class LocalVariableStorage
{

    private final Map<String, LocalVariable> localVariables = new LinkedHashMap<>();
    private short counter = 0;

    short addVariable(String name, ASTType type)
    {
        LocalVariable localVariable = new LocalVariable(name, type, counter++);
        localVariables.put(name, localVariable);
        return localVariable.getIndex();
    }

    short getVariableIndex(String name)
    {
        LocalVariable localVariable = localVariables.get(name);
        return localVariable.getIndex();
    }
}
