package org.mufuku.yaoocai.v1.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class BasicByteCodeProducer {

    protected final DataOutputStream out;

    public BasicByteCodeProducer(OutputStream out) {
        this.out = new DataOutputStream(out);
    }

    protected void emitHeader(String preamble, short majorVersion, short minorVersion, short mainFunctionIndex) throws IOException
    {
        out.writeChars(preamble);
        out.writeShort(majorVersion);
        out.writeShort(minorVersion);
        out.writeShort(mainFunctionIndex);
    }

    protected void writeOpCode(InstructionSet.OpCodes opCode, short... params) throws IOException {
        out.writeShort(opCode.code());
        if (opCode.opCodeParam() != params.length) {
            throw new IllegalStateException("Invalid number of params");
        }
        for (short param : params) {
            out.writeShort(param);
        }
    }
}
