package org.mufuku.yaoocai.v1.vm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class LocalVariableStack {

    private final List<Object> variables = new ArrayList<>();

    public void setValue(short index, Object value) {
        variables.add(index, value);
    }

    public Object getValue(short index) {
        return variables.get(index);
    }

}
