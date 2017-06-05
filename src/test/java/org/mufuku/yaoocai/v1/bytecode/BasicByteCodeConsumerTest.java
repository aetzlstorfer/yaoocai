package org.mufuku.yaoocai.v1.bytecode;

import org.junit.Test;

import java.io.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BasicByteCodeConsumerTest
{

    @Test(expected = IOException.class)
    public void test_emptyInput_fail() throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        TestByteCodeConsumer test = new TestByteCodeConsumer(in, (short) 99, (short) 115);
        test.readHeader();
    }

    @Test(expected = IllegalStateException.class)
    public void test_invalidPreamble_fail() throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream("y a o o c a i - invalid".getBytes());
        TestByteCodeConsumer test = new TestByteCodeConsumer(in, (short) 99, (short) 115);
        test.readHeader();
    }

    @Test(expected = IllegalStateException.class)
    public void test_incompatibleMajorVersion_fail() throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TestByteCodeProducer producer = new TestByteCodeProducer(out);
        producer.emitHeader(InstructionSet.PREAMBLE, (short) 1100, (short) 3100, (short) 0);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TestByteCodeConsumer test = new TestByteCodeConsumer(in, (short) 1000, (short) 3000);
        test.readHeader();
    }

    @Test(expected = IllegalStateException.class)
    public void test_incompatibleMinorVersion_fail() throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TestByteCodeProducer producer = new TestByteCodeProducer(out);
        producer.emitHeader(InstructionSet.PREAMBLE, (short) 1100, (short) 3100, (short) 0);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TestByteCodeConsumer test = new TestByteCodeConsumer(in, (short) 1100, (short) 3000);
        test.readHeader();
    }

    private static class TestByteCodeConsumer extends BasicByteCodeConsumer
    {
        TestByteCodeConsumer(InputStream in, short expectedMajorVersion, short expectedMinorVersion)
        {
            super(in, expectedMajorVersion, expectedMinorVersion);
        }
    }

    private static class TestByteCodeProducer extends BasicByteCodeProducer
    {
        public TestByteCodeProducer(OutputStream out)
        {
            super(out);
        }
    }
}