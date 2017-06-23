package org.mufuku.yaoocai.v1.bytecode;

import org.junit.Test;
import org.mufuku.yaoocai.v1.bytecode.data.BCConstantPoolBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCConstantPoolBuilderTest {

    private BCConstantPoolBuilder constantPoolBuilder = new BCConstantPoolBuilder();

    @Test
    public void test_differentItems_correctIndices() {

        String symbolValue = "integer";
        int integerValue = 99;
        String stringValue = "another string";

        short integerSymbolIndex1 = constantPoolBuilder.getSymbolIndex(symbolValue);
        short integerValueIndex1 = constantPoolBuilder.getIntegerIndex(integerValue);
        short stringValueIndex1 = constantPoolBuilder.getStringIndex(stringValue);

        assertThat(integerSymbolIndex1, equalTo((short) 0));
        assertThat(integerValueIndex1, equalTo((short) 1));
        assertThat(stringValueIndex1, equalTo((short) 2));

        short integerSymbolIndex2 = constantPoolBuilder.getSymbolIndex(symbolValue);
        short integerValueIndex2 = constantPoolBuilder.getIntegerIndex(integerValue);
        short stringValueIndex2 = constantPoolBuilder.getStringIndex(stringValue);

        assertThat(integerSymbolIndex2, equalTo((short) 0));
        assertThat(integerValueIndex2, equalTo((short) 1));
        assertThat(stringValueIndex2, equalTo((short) 2));
    }

}