package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.Collection;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCUnit {

    private final short nameIndex;

    private Collection<BCUnitItem> items;

    public BCUnit(short nameIndex) {
        this.nameIndex = nameIndex;
    }

    public short getNameIndex() {
        return nameIndex;
    }

    public Collection<BCUnitItem> getItems() {
        return items;
    }

    public void setItems(Collection<BCUnitItem> items) {
        this.items = items;
    }
}
