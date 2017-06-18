package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCType {

    public static final BCType NO_TYPE = new BCType(BCTypeType.NO);

    private final BCTypeType type;
    private Short referenceNameIndex;

    public BCType(BCTypeType type) {
        this.type = type;
    }

    public BCTypeType getType() {
        return type;
    }

    public Short getReferenceNameIndex() {
        return referenceNameIndex;
    }

    public void setReferenceNameIndex(Short referenceNameIndex) {
        this.referenceNameIndex = referenceNameIndex;
    }
}
