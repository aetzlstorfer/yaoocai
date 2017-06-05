package org.mufuku.yaoocai.v1.bytecode;

import org.junit.Test;

import java.io.IOException;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BasicByteCodeProducerTest {
    private final TestByteCodeProducer producer = new TestByteCodeProducer();

    @Test(expected = IllegalStateException.class)
    public void test_opCodeWithDifferentParams_fail() throws IOException {
        producer.writeOpCode(InstructionSet.OpCodes.ADD, (short) 1);
    }

    private static class TestByteCodeProducer extends BasicByteCodeProducer {

        public TestByteCodeProducer() {
            super(System.out);
        }
    }

}