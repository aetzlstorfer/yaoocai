package org.mufuku.yaoocai.v1.bytecode;

import org.mufuku.yaoocai.v1.bytecode.data.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ByteCodeWriter {

    private final DataOutputStream out;

    public ByteCodeWriter(OutputStream out) {
        this.out = new DataOutputStream(out);
    }

    public void writeByteCode(BCFile bcFile) throws IOException {
        writeHeader(bcFile);
        writeConstantPool(bcFile.getConstantPool());
        writeUnits(bcFile.getUnits());
    }

    private void writeHeader(BCFile bcFile) throws IOException {
        out.writeChars(bcFile.getPreamble());
        out.writeByte(bcFile.getMajorVersion());
        out.writeByte(bcFile.getMinorVersion());
    }

    private void writeConstantPool(BCConstantPool constantPool) throws IOException {
        out.writeShort(constantPool.getItems().size());
        for (BCConstantPoolItem constantPoolItem : constantPool.getItems()) {
            writeConstantPoolItem(constantPoolItem);
        }
    }

    private void writeConstantPoolItem(BCConstantPoolItem constantPoolItem) throws IOException {
        out.writeByte(constantPoolItem.getType().getValue());
        if (constantPoolItem.getType() == BCConstantPoolItemType.STRING || constantPoolItem.getType() == BCConstantPoolItemType.SYMBOL) {
            String value = (String) constantPoolItem.getValue();
            out.writeShort(value.length());
            out.writeChars(value);
        } else if (constantPoolItem.getType() == BCConstantPoolItemType.INTEGER) {
            Integer value = (Integer) constantPoolItem.getValue();
            out.writeInt(value);
        }
    }

    private void writeUnits(BCUnits bcUnits) throws IOException {
        Collection<BCUnit> units = bcUnits.getUnits();
        out.writeByte(units.size());
        for (BCUnit unit : units) {
            writeUnit(unit);
        }
    }

    private void writeUnit(BCUnit unit) throws IOException {
        out.writeShort(unit.getNameIndex());
        Collection<BCUnitItem> items = unit.getItems();
        out.writeShort(items.size());
        for (BCUnitItem unitItem : items) {
            writeUnitItem(unitItem);
        }
    }

    private void writeUnitItem(BCUnitItem unitItemItem) throws IOException {
        out.writeByte(unitItemItem.getType().getType());
        if (unitItemItem.getType() == BCUnitItemType.FUNCTION) {
            writeUnitItemFunction((BCUnitItemFunction) unitItemItem);
        }
    }

    private void writeUnitItemFunction(BCUnitItemFunction function) throws IOException {
        out.writeShort(function.getFunctionNameIndex());
        writeParameters(function.getParameters());
        writeType(function.getReturnType());
        writeLocalVariableTable(function.getLocalVariableTable());
        writeCode(function.getCode());
    }

    private void writeParameters(BCParameters parameters) throws IOException {
        writeNameAndTypes(parameters.getNameAndTypes());
    }

    private void writeNameAndTypes(List<BCNameAndType> nameAndTypes) throws IOException {
        out.writeByte(nameAndTypes.size());
        for (BCNameAndType nameAndType : nameAndTypes) {
            writeType(nameAndType.getType());
            out.writeShort(nameAndType.getNameIndex());
        }
    }

    private void writeLocalVariableTable(BCLocalVariableTable localVariableTable) throws IOException {
        if (localVariableTable == null) {
            out.writeByte(0);
        } else {
            writeNameAndTypes(localVariableTable.getNameAndTypes());
        }
    }

    private void writeType(BCType type) throws IOException {
        out.writeByte(type.getType().getCode());
        if (type.getType() == BCTypeType.REFERENCE_TYPE) {
            out.writeShort(type.getReferenceNameIndex());
        }
    }

    private void writeCode(BCCode code) throws IOException {
        out.writeShort(code.getCode().length);
        out.write(code.getCode());
    }
}
