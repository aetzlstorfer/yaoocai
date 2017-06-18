package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum BCUnitItemType {

    INTERFACE((byte) 0),
    CLASS((byte) 1),
    ENUM((byte) 2),
    FUNCTION((byte) 3);

    private final byte type;

    BCUnitItemType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
