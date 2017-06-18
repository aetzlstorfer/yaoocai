package org.mufuku.yaoocai.v1.vm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class LocalVariableStack {
    private final Map<Byte, Object> variables = new HashMap<>();

    void setValue(byte index, Object value) {
        variables.put(index, value);
    }

    Object getValue(byte index) {
        return variables.get(index);
    }
}
