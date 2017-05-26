package org.mufuku.yaoocai.v1.assembler;

import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.bytecode.viewer.ByteCodeViewer;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class AssemblerIntegrationTest extends BaseLangTest {

    @Test
    public void test_validAssemblerCode_equalToCompiledSource() throws IOException {
        ByteArrayOutputStream sourceOut = new ByteArrayOutputStream();
        ByteArrayOutputStream assembleOut = new ByteArrayOutputStream();

        compile("/test-assembler-sources/test-01-source.yaoocai", sourceOut);

        ByteArrayInputStream in = new ByteArrayInputStream(sourceOut.toByteArray());
        ByteCodeViewer codeViewer = new ByteCodeViewer(in, (short) 1, (short) 0, System.out);
        codeViewer.convert();

        assemble("/test-assembler-sources/test-01-asm.yaoocaia", assembleOut);

        assertArrayEquals(sourceOut.toByteArray(), assembleOut.toByteArray());
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeNoMain_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-02-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeMultipleMain_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-03-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeInvalidFunctionParams_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-04-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeInvalidStructure1_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-05-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeInvalidStructure2_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-06-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeInvalidStructure3_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-07-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeInvalidLineNumber_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-08-asm.yaoocaia", out);
    }


}
