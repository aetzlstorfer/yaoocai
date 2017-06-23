package org.mufuku.yaoocai.v1.bytecode.viewer;

import org.mufuku.yaoocai.v1.bytecode.ByteCodeReader;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.data.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ByteCodeViewer {

    private final ByteCodeReader byteCodeReader;
    private final PrintStream out;
    private BCFile file;

    public ByteCodeViewer(InputStream in, PrintStream out) {
        this.byteCodeReader = new ByteCodeReader(in);
        this.out = out;
    }

    public void convert() throws IOException {
        this.file = byteCodeReader.readByteCode();
        printConstantPool();
        printUnits(file.getUnits());
    }

    private void printConstantPool() {
        out.println("constant_pool {");
        List<BCConstantPoolItem> items = file.getConstantPool().getItems();
        for (BCConstantPoolItem item : items) {
            String cpValue = MessageFormat.format("{0,number,00}: {1} -> {2}",
                    item.getIndex(),
                    item.getType().getDisplayName(),
                    getConstantPoolValueString(item));
            out.println("  " + cpValue);
        }
        out.println("}");
    }

    private String getConstantPoolValueString(BCConstantPoolItem item) {
        return item.getValue().toString();
    }

    private void printUnits(BCUnits units) {
        for (BCUnit unit : units.getUnits()) {
            printUnit(unit);
        }
    }

    private void printUnit(BCUnit unit) {
        String unitName = ByteCodeReader.getConstantPoolString(file.getConstantPool(), unit.getNameIndex());
        out.println("unit " + unitName + " {");
        for (BCUnitItem unitItem : unit.getItems()) {
            printUnitItem(unitItem);
        }
        out.println("}");
    }

    private void printUnitItem(BCUnitItem unitItem) {
        if (unitItem.getType() == BCUnitItemType.FUNCTION) {
            printUnitItemFunction((BCUnitItemFunction) unitItem);
        }
    }

    private void printUnitItemFunction(BCUnitItemFunction function) {
        String functionName = ByteCodeReader.getConstantPoolString(file.getConstantPool(), function.getFunctionNameIndex());
        String parameterString = getParameterString(function.getParameters());
        String returnTypeString = getTypeString(function.getReturnType());
        out.println("  function " + functionName + "(" + parameterString + ")" + (returnTypeString != null ? ": " + returnTypeString : "") + " {");
        printCode(function);
        out.println("  }");
    }

    private String getParameterString(BCParameters parameters) {
        List<BCNameAndType> nameAndTypes = parameters.getNameAndTypes();
        StringBuilder sb = new StringBuilder(parameters.getNameAndTypes().size() * 7);
        for (int i = 0; i < nameAndTypes.size(); i++) {
            BCNameAndType nameAndType = nameAndTypes.get(i);
            String type = getTypeString(nameAndType.getType());
            String parameterName = ByteCodeReader.getConstantPoolString(file.getConstantPool(), nameAndType.getNameIndex());
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterName).append(": ").append(type);
        }
        return sb.toString();
    }

    private String getTypeString(BCType type) {
        if (type.getType() == BCTypeType.NO) {
            return null;
        } else {
            return type.getType().getDisplayName();
        }
    }

    private void printCode(BCUnitItemFunction function) {
        byte[] code = function.getCode().getCode();
        int codeIndex = 0;
        while (codeIndex < code.length) {
            codeIndex = printOpCode(code, codeIndex, function);
        }
    }

    private int printOpCode(byte[] code, int codeIndex, BCUnitItemFunction function) {
        int newCodeIndex = codeIndex;
        byte currentOpCode = code[newCodeIndex];
        InstructionSet.OpCodes opCode = InstructionSet.OpCodes.get(currentOpCode);
        if (opCode != null) {
            String byteCodeIndex = MessageFormat.format("    {0,number,00}: {1}",
                    newCodeIndex++,
                    opCode.disassembleCode()
            );
            out.print(byteCodeIndex);
            if (opCode.opCodeParam() > 0) {
                newCodeIndex = printOpCodeParams(opCode, code, newCodeIndex, function);
            }
            out.println();
        }
        return newCodeIndex;
    }

    private int printOpCodeParams(InstructionSet.OpCodes opCode, byte[] code, int codeIndex, BCUnitItemFunction function) {
        int newCodeIndex = codeIndex;
        out.print(" [");
        for (int i = 0; i < opCode.opCodeParam(); i++) {
            if (i > 0) {
                out.print(", ");
            }
            if (opCode.isAddressOpCode()) {
                out.print(toAddress(code[newCodeIndex]));
            } else {
                out.print(toHex(code[newCodeIndex]));
            }
            newCodeIndex++;
        }
        out.print("]");

        if (InstructionSet.OpCodes.LOCAL_VARIABLES_TABLE_AWARE.contains(opCode)) {
            BCNameAndType nameAndType = function.getLocalVariableTable().getNameAndTypes().get(code[codeIndex]);
            short localVariableNameIndex = nameAndType.getNameIndex();
            String localVariableName = ByteCodeReader.getConstantPoolString(file.getConstantPool(), localVariableNameIndex);
            String localVariableType = getTypeString(nameAndType.getType());
            out.print("\t\t// " + localVariableName + ": " + localVariableType);
        } else if (InstructionSet.OpCodes.CONSTANT_POOL_SINGLE.contains(opCode)) {
            BCConstantPoolItem constantPoolItem = file.getConstantPool().getItems().get(code[codeIndex]);
            String value = getConstantPoolString(constantPoolItem);
            out.print(value);
        } else if (InstructionSet.OpCodes.CONSTANT_POOL_WIDE.contains(opCode)) {
            byte highByte = code[codeIndex];
            byte lowByte = code[codeIndex + 1];
            int index = (highByte << 8) | lowByte & 0xff;
            BCConstantPoolItem constantPoolItem = file.getConstantPool().getItems().get(index);
            String value = getConstantPoolString(constantPoolItem);
            out.print(value);
        }

        return newCodeIndex;
    }

    private String getConstantPoolString(BCConstantPoolItem constantPoolItem) {
        return MessageFormat.format("\t\t// {0}: {1}",
                constantPoolItem.getType().getDisplayName(),
                constantPoolItem.getValue());
    }

    private String toHex(short opCode) {
        return String.format("0x%02x", opCode);
    }

    private String toAddress(byte address) {
        if (address >= 0) {
            return "+" + address;
        } else {
            return Byte.toString(address);
        }
    }
}
