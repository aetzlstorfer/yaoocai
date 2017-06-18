package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCCode {

    private final byte[] code;

    public BCCode(byte[] code) {
        this.code = code;
    }

    public byte[] getCode() {
        return code;
    }
}
