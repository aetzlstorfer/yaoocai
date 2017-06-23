package org.mufuku.yaoocai.v1.bytecode;

import org.junit.Test;
import org.mufuku.yaoocai.v1.bytecode.data.BCCodeBuilder;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCCodeBuilderTest {

    private final BCCodeBuilder codeBuilder = new BCCodeBuilder();

    @Test(expected = IllegalArgumentException.class)
    public void test_invalidArguments_exception() {
        codeBuilder.writeOpCode(InstructionSet.OpCodes.AND, (byte) 1);
    }

    @Test
    public void test_opCodeWithParams_correctBytes() {
        codeBuilder.writeOpCode(InstructionSet.OpCodes.LOAD, (byte) 2);
        byte[] code = codeBuilder.build().getCode();
        byte[] expectedCode = new byte[]{
                InstructionSet.OpCodes.LOAD.code(),
                2
        };
        assertThat(code, equalTo(expectedCode));
    }
}