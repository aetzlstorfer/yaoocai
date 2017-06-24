package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTNamedAndTypedElement;
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
    private short counter = 0;

    LocalVariableStorage() {
        this.localVariables = new LinkedHashMap<>();
    }

    byte addVariable(ASTNamedAndTypedElement localVariable) {
        String identifier = localVariable.getIdentifier();
        if (localVariables.containsKey(identifier)) {
            throw new ParsingException("Duplicate variable: " + identifier, localVariable.getLineNumber());
        }

        LocalVariable localVariableWrapper = new LocalVariable(localVariable.getType(), counter++);
        localVariables.put(identifier, localVariableWrapper);
        return (byte) localVariableWrapper.getIndex();
    }

    byte getVariableIndex(String name) {
        LocalVariable localVariable = getLocalVariable(name);
        if (!localVariable.isInitialized()) {
            throw new ParsingException("Variable " + name + " not initialized");
        }
        return (byte) localVariable.getIndex();
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
