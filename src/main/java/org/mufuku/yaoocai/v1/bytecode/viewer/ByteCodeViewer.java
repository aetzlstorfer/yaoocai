package org.mufuku.yaoocai.v1.bytecode.viewer;

import org.mufuku.yaoocai.v1.bytecode.BasicByteCodeConsumer;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ByteCodeViewer extends BasicByteCodeConsumer {

    private final PrintStream out;
    private int instructionIndex = 0;

    public ByteCodeViewer(InputStream in, short expectedMajorVersion, short expectedMinorVersion, PrintStream out) {
        super(in, expectedMajorVersion, expectedMinorVersion);
        this.out = new PrintStream(out);
    }

    public void convert() throws IOException {
        this.instructionIndex = 0;
        readHeader();
        int functionIndex = 0;
        Short currentOpCode = getNext();
        while (in.available() > 0 && currentOpCode != null && currentOpCode == InstructionSet.OpCodes.FUNCTION.code()) {
            out.println("Function: #" + functionIndex + (mainFunctionIndex == functionIndex ? " (main)" : ""));
            currentOpCode = getNext();
            while (currentOpCode != null && currentOpCode != InstructionSet.OpCodes.FUNCTION.code()) {
                checkOpCode(currentOpCode);
                currentOpCode = getNext();
            }
            functionIndex++;
        }
    }


    private void checkOpCode(short currentOpCode) throws IOException {
        InstructionSet.OpCodes opCode = InstructionSet.OpCodes.get(currentOpCode);
        if (opCode != null) {
            out.print("  ");
            out.print(this.instructionIndex++);
            out.print(": " + opCode.disassembleCode());
            if (opCode.opCodeParam() > 0) {
                out.print(" [");
                for (int i = 0; i < opCode.opCodeParam(); i++) {
                    if (i > 0) {
                        out.print(", ");
                    }
                    short opCodeOtherByte = in.readShort();
                    if (opCode.isAddressOpCode()) {
                        out.print(toAddress(opCodeOtherByte));
                    } else {
                        out.print(toHex(opCodeOtherByte));
                    }
                    this.instructionIndex++;
                }
                out.print("]");
            }
            out.println();
        }
    }

    private String toHex(short opCode) {
        return String.format("0x%04x", (int) opCode);
    }

    private String toAddress(short address) {
        if (address >= 0) {
            return "+" + address;
        } else {
            return Short.toString(address);
        }
    }
}
