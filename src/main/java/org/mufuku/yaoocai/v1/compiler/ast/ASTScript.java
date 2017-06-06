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

    private final short majorVersion;

    private final short minorVersion;

    public ASTScript(short majorVersion, short minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public void addDeclaredFunction(ASTBasicFunction declaredFunction) {
        if (declaredFunctions.containsKey(declaredFunction.getIdentifier())) {
            throw new ParsingException("Already defined function: " + declaredFunction.getIdentifier());
        }
        declaredFunctions.put(declaredFunction.getIdentifier(), declaredFunction);
    }

    public Collection<ASTBasicFunction> declaredFunctions() {
        return declaredFunctions.values();
    }

    public short getMajorVersion() {
        return majorVersion;
    }

    public short getMinorVersion() {
        return minorVersion;
    }
}
