package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum BCConstantPoolItemType {

    EMPTY((byte) 0, "empty"),
    INTEGER((byte) 1, "integer"),
    DECIMAL((byte) 2, "decimal"),
    LONG_INTEGER((byte) 3, "long integer"),
    LONG_DECIMAL((byte) 4, "long decimal"),
    STRING((byte) 5, "string"),
    SYMBOL((byte) 6, "symbol");

    private static final Map<String, BCConstantPoolItemType> byDisplayName =
            Arrays.stream(BCConstantPoolItemType.values())
                    .collect(Collectors
                            .toMap(BCConstantPoolItemType::getDisplayName, e -> e));
    private final byte value;
    private final String displayName;
    BCConstantPoolItemType(byte value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public static BCConstantPoolItemType getByDisplayName(String displayName) {
        return byDisplayName.get(displayName);
    }

    public byte getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
