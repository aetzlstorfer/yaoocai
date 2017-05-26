package org.mufuku.yaoocai.v1.assembler;

import org.junit.Assert;
import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.Constants;
import org.mufuku.yaoocai.v1.bytecode.viewer.ByteCodeViewer;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void massTest_compiledByteCodesConvertedWithByteCodeViewerConvertedBack_noError() throws IOException {

        Map<String, Boolean> results = new HashMap<>();
        boolean success = true;

        List<String> sourceFiles = getTestFiles("/test-sources/positive");
        for (String sourceFile : sourceFiles) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            compile(sourceFile, byteOut);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ByteArrayOutputStream byteCodeHumanReadableOut = new ByteArrayOutputStream();
            ByteCodeViewer byteCodeViewer = new ByteCodeViewer(byteIn, Constants.MAJOR_VERSION, Constants.MINOR_VERSION, new PrintStream(byteCodeHumanReadableOut));
            byteCodeViewer.convert();

            ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
            ByteArrayInputStream byteCodeHumanReadableIn = new ByteArrayInputStream(byteCodeHumanReadableOut.toByteArray());
            YAOOCAI_AssemblerCompiler assemblerCompiler = new YAOOCAI_AssemblerCompiler(byteCodeHumanReadableIn, byteOut2);

            boolean equals;
            try {
                assemblerCompiler.compile();
                equals = Arrays.equals(byteOut.toByteArray(), byteOut2.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
                equals = false;
            }

            success = success && equals;
            results.put(sourceFile, equals);
        }
        Assert.assertTrue(success);
    }
}
