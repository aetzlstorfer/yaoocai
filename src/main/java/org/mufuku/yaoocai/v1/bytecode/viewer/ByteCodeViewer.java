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

    public ByteCodeViewer(InputStream in, short expectedMajorVersion, short expectedMinorVersion, PrintStream out) {
        super(in, expectedMajorVersion, expectedMinorVersion);
        this.out = new PrintStream(out);
    }

    public void convert() throws IOException {
        readHeader();
        int functionIndex = 0;
        short currentOpCode = getNext();
        while (in.available() > 0 && currentOpCode == InstructionSet.OpCodes.FUNCTION.code()) {
            System.out.println("Function: #" + functionIndex + (mainFunctionIndex == functionIndex ? " (main)" : ""));
            currentOpCode = getNext();
            while (currentOpCode != InstructionSet.OpCodes.FUNCTION.code() && currentOpCode != -1) {
                checkOpCode(currentOpCode);
                currentOpCode = getNext();
            }
            functionIndex++;
        }
    }



    private void checkOpCode(short currentOpCode) throws IOException {
        InstructionSet.OpCodes opCode = InstructionSet.OpCodes.get(currentOpCode);
        if (opCode != null) {
            out.print("  " + opCode.disassembleCode());
            if (opCode.opCodeParam() > 0) {
                out.print(" [");
                for (int i = 0; i < opCode.opCodeParam(); i++) {
                    if (i > 0) {
                        out.print(", ");
                    }
                    out.print(toHex(in.readShort()));
                }
                out.print("]");
            }
            out.println();
        }
    }



    private String toHex(short opCode) {
        return String.format("0x%04x", (int) opCode);
    }
}
