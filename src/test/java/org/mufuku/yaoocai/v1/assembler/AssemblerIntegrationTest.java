package org.mufuku.yaoocai.v1.assembler;

import org.junit.Assert;
import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.bytecode.ByteCodeReader;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.data.*;
import org.mufuku.yaoocai.v1.bytecode.viewer.ByteCodeViewer;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class AssemblerIntegrationTest extends BaseLangTest {

    @Test
    public void test_validAssemblerCode_equalToCompiledSource() throws IOException {
        ByteArrayOutputStream sourceOut = new ByteArrayOutputStream();
        ByteArrayOutputStream assembleOut = new ByteArrayOutputStream();
        compile("/test-assembler-sources/test-01-source.yaoocai", sourceOut);
        assemble("/test-assembler-sources/test-01-asm.yaoocaia", assembleOut);

        ByteCodeReader assembleCodeReader = new ByteCodeReader(new ByteArrayInputStream(assembleOut.toByteArray()));
        ByteCodeReader sourceCodeReader = new ByteCodeReader(new ByteArrayInputStream(sourceOut.toByteArray()));

        BCFile assembleByteCode = assembleCodeReader.readByteCode();
        BCFile sourceByteCode = sourceCodeReader.readByteCode();

        assertFunctionsMatches(sourceByteCode, assembleByteCode);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCode_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-02-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidAssemblerCodeInvalidStructure_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-03-asm.yaoocaia", out);
    }

    @Test
    public void test_functionWithReferenceType_correctByteCode() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-04-asm.yaoocaia", out);
        ByteCodeReader byteCodeReader = new ByteCodeReader(new ByteArrayInputStream(out.toByteArray()));

        BCFile file = byteCodeReader.readByteCode();
        Map<String, BCUnitItemFunction> functionMap = getFunctionMap(file);
        BCUnitItemFunction testFunction = functionMap.get("test");

        BCType returnType = testFunction.getReturnType();
        BCConstantPoolItem returnTypeSymbol = file.getConstantPool().getItems().get(returnType.getReferenceNameIndex());

        assertThat(testFunction, notNullValue());
        assertThat(returnTypeSymbol, notNullValue());
        assertThat(returnTypeSymbol.getType(), equalTo(BCConstantPoolItemType.SYMBOL));
        assertThat(returnTypeSymbol.getValue(), equalTo("String"));
        assertThat(returnType.getType(), equalTo(BCTypeType.REFERENCE_TYPE));
    }

    @Test(expected = ParsingException.class)
    public void test_incompleteConstantPoolString_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-05-asm.yaoocaia", out);
    }

    @Test(expected = ParsingException.class)
    public void test_invalidSymbols_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assemble("/test-assembler-sources/test-06-asm.yaoocaia", out);
    }

    @Test
    public void massTest_compiledByteCodesConvertedWithByteCodeViewerConvertedBack_noError() throws IOException {
        List<String> sourceFiles = getTestFiles("/test-language-integration/positive");
        for (String sourceFile : sourceFiles) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            compile(sourceFile, byteOut);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ByteArrayOutputStream byteCodeHumanReadableOut = new ByteArrayOutputStream();
            ByteCodeViewer byteCodeViewer = new ByteCodeViewer(byteIn, new PrintStream(byteCodeHumanReadableOut));
            byteCodeViewer.convert();

            ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
            ByteArrayInputStream byteCodeHumanReadableIn = new ByteArrayInputStream(byteCodeHumanReadableOut.toByteArray());
            AssemblerCompiler assemblerCompiler = new AssemblerCompiler(byteCodeHumanReadableIn, byteOut2);
            assemblerCompiler.compile();


            ByteCodeReader assembleCodeReader = new ByteCodeReader(new ByteArrayInputStream(byteOut.toByteArray()));
            ByteCodeReader sourceCodeReader = new ByteCodeReader(new ByteArrayInputStream(byteOut2.toByteArray()));

            BCFile assembleByteCode = assembleCodeReader.readByteCode();
            BCFile sourceByteCode = sourceCodeReader.readByteCode();

            assertFunctionsMatches(sourceByteCode, assembleByteCode);
        }
    }

    private void assertFunctionsMatches(BCFile a, BCFile b) {
        Map<String, BCUnitItemFunction> functionMapA = getFunctionMap(a);
        Map<String, BCUnitItemFunction> functionMapB = getFunctionMap(b);
        assertTrue(functionMapA.size() == functionMapB.size());
        for (String functionName : functionMapA.keySet()) {
            BCUnitItemFunction functionA = functionMapA.get(functionName);
            BCUnitItemFunction functionB = functionMapB.get(functionName);
            assertOpCodeMatches(functionA, functionB);
        }
    }

    private void assertOpCodeMatches(BCUnitItemFunction functionA, BCUnitItemFunction functionB) {
        List<InstructionSet.OpCodes> codesA = consumeOpCodes(functionA.getCode());
        List<InstructionSet.OpCodes> codesB = consumeOpCodes(functionB.getCode());
        Assert.assertEquals(codesA, codesB);
    }

    private List<InstructionSet.OpCodes> consumeOpCodes(BCCode bcCode) {
        int index = 0;
        byte[] code = bcCode.getCode();
        List<InstructionSet.OpCodes> result = new ArrayList<>();
        while (index < code.length) {
            InstructionSet.OpCodes instruction = InstructionSet.OpCodes.get(code[index]);
            result.add(instruction);
            index += (instruction.opCodeParam() + 1);
        }
        return result;
    }

    private Map<String, BCUnitItemFunction> getFunctionMap(BCFile file) {
        return file.getUnits().getUnits().iterator().next().getItems().stream()
                .map(i -> (BCUnitItemFunction) i)
                .collect(Collectors.toMap(
                        f -> (String) file.getConstantPool().getItems().get(f.getFunctionNameIndex()).getValue(),
                        f -> f
                ));
    }
}
