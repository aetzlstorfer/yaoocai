package org.mufuku.yaoocai.v1.compiler.translator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class IfJumpTable
{

    private final List<Short> blockSizes = new ArrayList<>();

    private final List<Short> expressionSizes = new ArrayList<>();

    void addEntry(short blockSize, short expressionSize)
    {
        this.blockSizes.add(blockSize);
        this.expressionSizes.add(expressionSize);
    }

    short getIfJumpOffset(int index)
    {
        short jumpOffset = 1;
        jumpOffset += blockSizes.get(index);
        return jumpOffset;
    }

    short getEndJumpOffset(int index)
    {
        short jumpOffset = 1;
        for (int j = index + 1; j < blockSizes.size(); j++)
        {
            jumpOffset += blockSizes.get(j);
            jumpOffset += expressionSizes.get(j);
        }
        return jumpOffset;
    }
}
