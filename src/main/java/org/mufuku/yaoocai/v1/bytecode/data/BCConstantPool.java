package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCConstantPool {

    private final List<BCConstantPoolItem> items;

    public BCConstantPool(List<BCConstantPoolItem> items) {
        this.items = items;
    }

    public List<BCConstantPoolItem> getItems() {
        return items;
    }
}
