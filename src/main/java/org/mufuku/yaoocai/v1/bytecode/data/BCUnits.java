package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.Collection;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCUnits {
    private final Collection<BCUnit> units;

    public BCUnits(Collection<BCUnit> units) {
        this.units = units;
    }

    public Collection<BCUnit> getUnits() {
        return units;
    }
}
