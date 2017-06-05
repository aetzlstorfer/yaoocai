package org.mufuku.yaoocai.v1.vm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class LocalVariableStack
{
    private final Map<Short, Object> variables = new HashMap<>();

    void setValue(short index, Object value)
    {
        variables.put(index, value);
    }

    Object getValue(short index)
    {
        return variables.get(index);
    }
}
