package org.mufuku.yaoocai.v1.vm;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class CallStackElement {

    private final byte[] code;

    private int codePointer = 0;

    CallStackElement(byte[] code) {
        this.code = code;
    }

    byte getCode() {
        return code[codePointer];
    }

    void move() {
        codePointer++;
    }

    void move(byte offset) {
        codePointer += offset;
    }

    int getCodePointer() {
        return codePointer;
    }
}
