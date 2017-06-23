package org.mufuku.yaoocai.v1.bytecode;

import com.google.common.base.Preconditions;
import org.mufuku.yaoocai.v1.bytecode.data.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ByteCodeReader {

    private final DataInputStream in;

    public ByteCodeReader(InputStream in) {
        this.in = new DataInputStream(in);
    }

    public static String getConstantPoolString(BCConstantPool constantPool, short index) {
        return (String) constantPool.getItems().get(index).getValue();
    }

    public BCFile readByteCode() throws IOException {
        BCFile file = new BCFile();
        readHeader(file);
        readConstantPool(file);
        readUnits(file);
        validateNoTrailingContent();
        return file;
    }

    private void readHeader(BCFile file) throws IOException {
        String preamble = readString(InstructionSet.PREAMBLE.length());
        if (!preamble.equals(InstructionSet.PREAMBLE)) {
            throw new IOException("Preamble didn't match");
        }
        file.setPreamble(preamble);
        file.setMajorVersion(in.readByte());
        file.setMinorVersion(in.readByte());
    }

    private void readConstantPool(BCFile file) throws IOException {
        List<BCConstantPoolItem> constantPoolItems = new ArrayList<>();
        short length = in.readShort();
        for (short index = 0; index < length; index++) {
            BCConstantPoolItem item = readConstantPoolItem();
            item.setIndex(index);
            constantPoolItems.add(item);
        }
        BCConstantPool constantPool = new BCConstantPool(constantPoolItems);
        file.setConstantPool(constantPool);
    }

    private BCConstantPoolItem readConstantPoolItem() throws IOException {
        byte type = in.readByte();
        if (type == BCConstantPoolItemType.INTEGER.getValue()) {
            BCConstantPoolItem<Integer> item = new BCConstantPoolItem<>();
            item.setType(BCConstantPoolItemType.INTEGER);
            item.setValue(in.readInt());
            return item;
        } else if (type == BCConstantPoolItemType.STRING.getValue()) {
            return convertStringyItem(BCConstantPoolItemType.STRING);
        } else if (type == BCConstantPoolItemType.SYMBOL.getValue()) {
            return convertStringyItem(BCConstantPoolItemType.SYMBOL);
        }
        throw new IOException("Invalid constant pool item type: " + Byte.toString(type));
    }

    private BCConstantPoolItem convertStringyItem(BCConstantPoolItemType type) throws IOException {
        short length = in.readShort();
        String string = readString(length);
        BCConstantPoolItem<String> item = new BCConstantPoolItem<>();
        item.setType(type);
        item.setValue(string);
        return item;
    }

    private void readUnits(BCFile file) throws IOException {
        byte unitsSize = in.readByte();
        BCUnits units = new BCUnits(new ArrayList<>(unitsSize));
        for (byte index = 0; index < unitsSize; index++) {
            BCUnit unit = readUnit();
            units.getUnits().add(unit);
        }
        file.setUnits(units);
    }

    private BCUnit readUnit() throws IOException {
        short unitNameIndex = in.readShort();
        short unitItemsSize = in.readShort();

        BCUnit unit = new BCUnit(unitNameIndex);
        unit.setItems(new ArrayList<>(unitItemsSize));

        for (short i = 0; i < unitItemsSize; i++) {
            BCUnitItem item = readUnitItem();
            unit.getItems().add(item);
        }

        return unit;
    }

    private BCUnitItem readUnitItem() throws IOException {
        byte type = in.readByte();
        Preconditions.checkArgument(type == BCUnitItemType.FUNCTION.getType());
        return readUnitItemFunction();
    }

    private BCUnitItemFunction readUnitItemFunction() throws IOException {
        short functionNameIndex = in.readShort();
        BCParameters parameters = readParameters();
        BCType returnType = readType();
        BCLocalVariableTable localVariableTable = readLocalVariableTable();
        BCCode code = readCode();

        BCUnitItemFunction function = new BCUnitItemFunction(functionNameIndex);
        function.setParameters(parameters);
        function.setReturnType(returnType);
        function.setLocalVariableTable(localVariableTable);
        function.setCode(code);

        return function;
    }

    private BCParameters readParameters() throws IOException {
        return new BCParameters(readNameAndTypes());
    }

    private BCLocalVariableTable readLocalVariableTable() throws IOException {
        return new BCLocalVariableTable(readNameAndTypes());
    }

    private List<BCNameAndType> readNameAndTypes() throws IOException {
        byte count = in.readByte();
        List<BCNameAndType> result = new ArrayList<>();
        for (byte i = 0; i < count; i++) {
            BCType type = readType();
            short typeNameIndex = in.readShort();
            BCNameAndType nameAndType = new BCNameAndType(type, typeNameIndex);
            result.add(nameAndType);
        }
        return result;
    }

    private BCType readType() throws IOException {
        byte type = in.readByte();
        BCTypeType typeType = BCTypeType.getByType(type);
        BCType bcType = new BCType(typeType);
        if (typeType == BCTypeType.REFERENCE_TYPE) {
            short referenceNameIndex = in.readShort();
            bcType.setReferenceNameIndex(referenceNameIndex);
        }
        return bcType;
    }

    private BCCode readCode() throws IOException {
        short length = in.readShort();
        byte[] code = new byte[length];
        int realLength = in.read(code);
        if (realLength != length) {
            throw new IOException("Expected " + length + " bytes for code, but got only " + realLength);
        }
        return new BCCode(code);
    }

    private String readString(int length) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(in.readChar());
        }
        return sb.toString();
    }

    private void validateNoTrailingContent() throws IOException {
        if (in.read() != -1) {
            throw new IOException("Trailing content");
        }
    }
}
