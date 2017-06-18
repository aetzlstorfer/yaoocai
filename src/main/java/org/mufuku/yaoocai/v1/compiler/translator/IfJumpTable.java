package org.mufuku.yaoocai.v1.compiler.translator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class IfJumpTable {

    private final List<Byte> blockSizes = new ArrayList<>();

    private final List<Byte> expressionSizes = new ArrayList<>();

    void addEntry(byte blockSize, byte expressionSize) {
        this.blockSizes.add(blockSize);
        this.expressionSizes.add(expressionSize);
    }

    byte getIfJumpOffset(int index) {
        byte jumpOffset = 1;
        jumpOffset += blockSizes.get(index);
        return jumpOffset;
    }

    byte getEndJumpOffset(int index) {
        byte jumpOffset = 1;
        for (int j = index + 1; j < blockSizes.size(); j++) {
            jumpOffset += blockSizes.get(j);
            jumpOffset += expressionSizes.get(j);
        }
        return jumpOffset;
    }
}
