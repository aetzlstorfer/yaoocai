package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTBuiltinFunction;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class FunctionStorage {

    private final Map<String, ASTBuiltinFunction> builtinIndex = new HashMap<>();
    private final Map<String, Short> functionIndex = new LinkedHashMap<>();
    private short functionCount = 0;

    public void addBuiltinFunction(ASTBuiltinFunction function) {
        builtinIndex.put(function.getIdentifier(), function);
    }

    public ASTBuiltinFunction getBuiltinFunction(String name) {
        return builtinIndex.get(name);
    }

    public void addFunction(String name) {
        functionIndex.put(name, functionCount++);
    }

    public Short getFunctionIndex(String name) {
        return functionIndex.get(name);
    }
}