package org.mufuku.yaoocai.v1.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class BasicByteCodeConsumer {

    protected final DataInputStream in;
    protected final short expectedMajorVersion;
    protected final short expectedMinorVersion;

    protected short mainFunctionIndex;

    public BasicByteCodeConsumer(InputStream in, short expectedMajorVersion, short expectedMinorVersion) {
        this.in = new DataInputStream(in);
        this.expectedMajorVersion = expectedMajorVersion;
        this.expectedMinorVersion = expectedMinorVersion;
    }

    protected void readHeader() throws IOException {
        String preamble = readString(InstructionSet.PREAMBLE.length());

        if (!preamble.equals(InstructionSet.PREAMBLE)) {
            throw new IllegalStateException("Preamble missing. Invalid byte code.");
        }

        short majorVersion = getNext();
        short minorVersion = getNext();

        if (majorVersion != expectedMajorVersion || minorVersion != expectedMinorVersion) {
            throw new IllegalStateException("Invalid byte code version: " + majorVersion + "." + minorVersion);
        }

        this.mainFunctionIndex = getNext();
        if (mainFunctionIndex < 0) {
            throw new IllegalStateException("No main function pointer");
        }
    }

    protected Short getNext() throws IOException {
        if (in.available() > 0) {
            return in.readShort();
        } else {
            return null;
        }
    }

    protected String readString(int length) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(in.readChar());
        }
        return sb.toString();
    }
}
