package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class BCUnitItem {

    private final BCUnitItemType type;

    BCUnitItem(BCUnitItemType type) {
        this.type = type;
    }

    public BCUnitItemType getType() {
        return type;
    }
}
