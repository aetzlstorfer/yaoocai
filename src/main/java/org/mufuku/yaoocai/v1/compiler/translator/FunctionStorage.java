package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.ASTBasicFunction;
import org.mufuku.yaoocai.v1.compiler.ast.ASTBuiltinFunction;
import org.mufuku.yaoocai.v1.compiler.ast.ASTFunction;
import org.mufuku.yaoocai.v1.compiler.ast.ASTType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class FunctionStorage {

    private final Map<String, ASTBuiltinFunction> builtinIndex = new HashMap<>();
    private final Map<String, Short> functionIndex = new LinkedHashMap<>();

    private final Map<String, ASTFunction> functions = new LinkedHashMap<>();

    private short functionCount = 0;

    public void addBuiltinFunction(ASTBuiltinFunction function) {
        builtinIndex.put(function.getIdentifier(), function);
    }

    public ASTBuiltinFunction getBuiltinFunction(String name) {
        return builtinIndex.get(name);
    }

    public void addFunction(ASTFunction astFunction) {
        String name = astFunction.getIdentifier();
        functionIndex.put(name, functionCount++);
        functions.put(name, astFunction);
    }

    public Short getFunctionIndex(String name) {
        return functionIndex.get(name);
    }

    @SuppressWarnings("unchecked")
    public ASTType getFunctionReturnType(String functionName) {
        Map<String, ASTBasicFunction> localFunctions = (Map<String, ASTBasicFunction>) (Map<String, ?>) this.functions;
        Map<String, ASTBasicFunction> builtInFunctions = (Map<String, ASTBasicFunction>) (Map<String, ?>) this.builtinIndex;

        ASTBasicFunction function = localFunctions.getOrDefault(functionName, builtInFunctions.get(functionName));
        ASTType returnType = null;
        if (function != null) {
            returnType = function.getReturnType();
        }
        return returnType;
    }
}