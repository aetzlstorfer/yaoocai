package org.mufuku.yaoocai.v1.bytecode.data;

import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCParameters {

    private final List<BCNameAndType> nameAndTypes;

    public BCParameters(List<BCNameAndType> nameAndTypes) {
        this.nameAndTypes = nameAndTypes;
    }

    public List<BCNameAndType> getNameAndTypes() {
        return nameAndTypes;
    }
}
