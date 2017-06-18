package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCConstantPoolBuilder {

    private final List<BCConstantPoolItem> items;
    private final Map<Integer, BCConstantPoolItem<Integer>> integerItemsIndex = new HashMap<>();
    private final Map<String, BCConstantPoolItem<String>> stringItemsIndex = new HashMap<>();
    private final Map<String, BCConstantPoolItem<String>> symbolItemsIndex = new HashMap<>();
    private short index = 0;

    public BCConstantPoolBuilder(List<BCConstantPoolItem> items, short index) {
        this.items = items;
        this.index = index;
    }

    public BCConstantPoolBuilder() {
        this.items = new ArrayList<>();
    }

    public short getIntegerIndex(int value) {
        return createOrGetIndex(value, integerItemsIndex, BCConstantPoolItemType.INTEGER);
    }

    public short getStringIndex(String value) {
        return createOrGetIndex(value, stringItemsIndex, BCConstantPoolItemType.STRING);
    }

    public short getSymbolIndex(String value) {
        return createOrGetIndex(value, symbolItemsIndex, BCConstantPoolItemType.SYMBOL);
    }

    private <T> short createOrGetIndex(T value, Map<T, BCConstantPoolItem<T>> map, BCConstantPoolItemType type) {
        if (map.containsKey(value)) {
            return map.get(value).getIndex();
        } else {
            BCConstantPoolItem<T> item = new BCConstantPoolItem<>();
            item.setValue(value);
            item.setType(type);
            item.setIndex(index++);
            map.put(value, item);
            items.add(item);
            return item.getIndex();
        }
    }

    public BCConstantPool build() {
        return new BCConstantPool(items);
    }
}
