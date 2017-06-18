package org.mufuku.yaoocai.v1.bytecode.data;

import org.mufuku.yaoocai.v1.bytecode.InstructionSet;

import java.io.ByteArrayOutputStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCCodeBuilder {

    private final ByteArrayOutputStream codeContent = new ByteArrayOutputStream();

    public void writeOpCode(InstructionSet.OpCodes opCode, byte... params) {
        codeContent.write(opCode.code());
        if (opCode.opCodeParam() != params.length) {
            throw new IllegalArgumentException("Invalid number of params");
        }
        codeContent.write(params, 0, params.length);
    }

    public BCCode build() {
        return new BCCode(this.codeContent.toByteArray());
    }

    public byte getSize() {
        return (byte) codeContent.size();
    }
}
