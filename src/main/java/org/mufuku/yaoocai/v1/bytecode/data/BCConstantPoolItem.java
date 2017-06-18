package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCConstantPoolItem<T> {
    private short index;
    private BCConstantPoolItemType type;
    private T value;

    public short getIndex() {
        return index;
    }

    public void setIndex(short index) {
        this.index = index;
    }

    public BCConstantPoolItemType getType() {
        return type;
    }

    public void setType(BCConstantPoolItemType type) {
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
