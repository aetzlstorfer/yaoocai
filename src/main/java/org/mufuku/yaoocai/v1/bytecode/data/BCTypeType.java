package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum BCTypeType {
    NO((byte) 0, "no"),
    BOOLEAN((byte) 1, "boolean"),
    BYTE((byte) 2, "byte"),
    CHARACTER((byte) 3, "character"),
    INTEGER((byte) 4, "integer"),
    DECIMAL((byte) 5, "decimal"),
    LONG_INTEGER((byte) 6, "long integer"),
    LONG_DECIMAL((byte) 7, "long decimal"),
    REFERENCE_TYPE((byte) 8, "reference type");

    private static final Map<Byte, BCTypeType> mapping = Arrays.stream(BCTypeType.values())
            .collect(Collectors
                    .toMap(
                            item -> item.code,
                            item -> item));
    private final byte code;
    private final String displayName;

    BCTypeType(byte code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static BCTypeType getByType(byte type) {
        return mapping.get(type);
    }

    public byte getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
