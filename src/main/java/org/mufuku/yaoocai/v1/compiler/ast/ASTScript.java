package org.mufuku.yaoocai.v1.compiler.ast;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTScript {

    private final Map<String, ASTBuiltinFunction> builtinFunctions = new LinkedHashMap<>();

    private final Map<String, ASTFunction> functions = new LinkedHashMap<>();

    private final short majorVersion;

    private final short minorVersion;

    public ASTScript(short majorVersion, short minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public void addBuiltInFunction(ASTBuiltinFunction builtinFunction) {
        builtinFunctions.put(builtinFunction.getIdentifier(), builtinFunction);
    }

    public void addFunction(ASTFunction function) {
        functions.put(function.getIdentifier(), function);
    }

    public Iterator<ASTBuiltinFunction> builtInFunctions() {
        return builtinFunctions.values().iterator();
    }

    public Iterator<ASTFunction> functions() {
        return functions.values().iterator();
    }

    public short getMajorVersion() {
        return majorVersion;
    }

    public short getMinorVersion() {
        return minorVersion;
    }
}
