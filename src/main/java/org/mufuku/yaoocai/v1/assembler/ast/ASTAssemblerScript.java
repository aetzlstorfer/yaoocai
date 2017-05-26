package org.mufuku.yaoocai.v1.assembler.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTAssemblerScript implements Iterable<ASTAssemblerFunction> {

    private final List<ASTAssemblerFunction> functions = new ArrayList<>();
    private short mainFunctionIndex;
    private short minorVersion;
    private short majorVersion;

    public short getMainFunctionIndex() {
        return mainFunctionIndex;
    }

    public void setMainFunctionIndex(short mainFunctionIndex) {
        this.mainFunctionIndex = mainFunctionIndex;
    }

    public short getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(short minorVersion) {
        this.minorVersion = minorVersion;
    }

    public short getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(short majorVersion) {
        this.majorVersion = majorVersion;
    }

    public void addFunction(ASTAssemblerFunction function) {
        functions.add(function);
    }

    @Override
    public Iterator<ASTAssemblerFunction> iterator() {
        return functions.iterator();
    }
}
