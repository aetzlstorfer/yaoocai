package org.mufuku.yaoocai.v1.bytecode.data;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCFile {

    private String preamble;
    private byte minorVersion;
    private byte majorVersion;

    private BCConstantPool constantPool;
    private BCUnits units;

    public String getPreamble() {
        return preamble;
    }

    public void setPreamble(String preamble) {
        this.preamble = preamble;
    }

    public byte getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(byte minorVersion) {
        this.minorVersion = minorVersion;
    }

    public byte getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(byte majorVersion) {
        this.majorVersion = majorVersion;
    }

    public BCConstantPool getConstantPool() {
        return constantPool;
    }

    public void setConstantPool(BCConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    public BCUnits getUnits() {
        return units;
    }

    public void setUnits(BCUnits units) {
        this.units = units;
    }
}
