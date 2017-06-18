package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum BCConstantPoolItemType {
    CHARACTER((byte) 1, "character"),
    INTEGER((byte) 2, "integer"),
    DECIMAL((byte) 3, "decimal"),
    LONG_INTEGER((byte) 4, "long integer"),
    LONG_DECIMAL((byte) 5, "long decimal"),
    STRING((byte) 6, "string"),
    SYMBOL((byte) 7, "symbol");

    private final byte value;

    private final String displayName;

    BCConstantPoolItemType(byte value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public byte getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
