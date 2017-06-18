package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCLocalVariableTable {

    private final List<BCNameAndType> nameAndTypes;

    public BCLocalVariableTable(List<BCNameAndType> nameAndTypes) {
        this.nameAndTypes = nameAndTypes;
    }

    public List<BCNameAndType> getNameAndTypes() {
        return nameAndTypes;
    }
}
