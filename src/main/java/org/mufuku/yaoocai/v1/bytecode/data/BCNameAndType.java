package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCNameAndType {

    private final BCType type;
    private final short nameIndex;

    public BCNameAndType(BCType type, short nameIndex) {
        this.type = type;
        this.nameIndex = nameIndex;
    }

    public BCType getType() {
        return type;
    }

    public short getNameIndex() {
        return nameIndex;
    }
}
