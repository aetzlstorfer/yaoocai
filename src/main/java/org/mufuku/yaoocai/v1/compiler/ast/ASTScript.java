package org.mufuku.yaoocai.v1.compiler.ast;

import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTScript {

    private final Map<String, ASTBasicFunction> declaredFunctions = new LinkedHashMap<>();

    private final byte majorVersion;

    private final byte minorVersion;

    public ASTScript(byte majorVersion, byte minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public void addDeclaredFunction(ASTBasicFunction declaredFunction) {
        if (declaredFunctions.containsKey(declaredFunction.getIdentifier())) {
            throw new ParsingException(
                    "Already defined function: " + declaredFunction.getIdentifier(),
                    declaredFunction.getLineNumber());
        }
        declaredFunctions.put(declaredFunction.getIdentifier(), declaredFunction);
    }

    public Collection<ASTBasicFunction> declaredFunctions() {
        return declaredFunctions.values();
    }

    public byte getMajorVersion() {
        return majorVersion;
    }

    public byte getMinorVersion() {
        return minorVersion;
    }
}
