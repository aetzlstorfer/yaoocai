package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTType;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class LocalVariableStorage {

    private final Map<String, LocalVariable> localVariables;
    private byte counter = 0;

    LocalVariableStorage() {
        this.localVariables = new LinkedHashMap<>();
    }

    byte addVariable(String name, ASTType type) {
        if (localVariables.containsKey(name)) {
            throw new ParsingException("Duplicate variable: " + name);
        }
        LocalVariable localVariable = new LocalVariable(type, counter++);
        localVariables.put(name, localVariable);
        return localVariable.getIndex();
    }

    byte getVariableIndex(String name) {
        LocalVariable localVariable = getLocalVariable(name);
        if (!localVariable.isInitialized()) {
            throw new ParsingException("Variable " + name + " not initialized");
        }
        return localVariable.getIndex();
    }

    ASTType getVariableType(String name) {
        return getLocalVariable(name).getType();
    }

    private LocalVariable getLocalVariable(String name) {
        LocalVariable localVariable = localVariables.get(name);
        if (localVariable == null) {
            throw new ParsingException("Invalid variable " + name + " used");
        }
        return localVariable;
    }

    void markInitialized(String variableName) {
        localVariables.get(variableName).setInitialized();
    }

    Collection<Map.Entry<String, LocalVariable>> getRealLocalVariables() {
        return localVariables.entrySet();
    }
}
