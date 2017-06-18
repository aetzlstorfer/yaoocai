package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCUnitItemFunction extends BCUnitItem {

    private final short functionNameIndex;
    private BCParameters parameters;
    private BCType returnType;
    private BCLocalVariableTable localVariableTable;
    private BCCode code;

    public BCUnitItemFunction(short functionNameIndex) {
        super(BCUnitItemType.FUNCTION);
        this.functionNameIndex = functionNameIndex;
    }

    public short getFunctionNameIndex() {
        return functionNameIndex;
    }

    public BCParameters getParameters() {
        return parameters;
    }

    public void setParameters(BCParameters parameters) {
        this.parameters = parameters;
    }

    public BCType getReturnType() {
        return returnType;
    }

    public void setReturnType(BCType returnType) {
        this.returnType = returnType;
    }

    public BCLocalVariableTable getLocalVariableTable() {
        return localVariableTable;
    }

    public void setLocalVariableTable(BCLocalVariableTable localVariableTable) {
        this.localVariableTable = localVariableTable;
    }

    public BCCode getCode() {
        return code;
    }

    public void setCode(BCCode code) {
        this.code = code;
    }
}
