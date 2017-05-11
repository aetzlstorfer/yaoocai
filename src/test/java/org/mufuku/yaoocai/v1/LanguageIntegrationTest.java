package org.mufuku.yaoocai.v1;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;
import org.mufuku.yaoocai.v1.vm.YAOOCAI_VM;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertThat;

/**
 * @author andreas.etzlstorfer@ecx.io
 */
public class LanguageIntegrationTest extends BaseLangTest {

    @Test(expected = ParsingException.class)
    public void test_invalidSyntax1_failWithParsingException() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/invalid-syntax-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidSyntax2_failWithParsingException() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/invalid-syntax-02-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validComparisonExpressions_noFailCall() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/comparison-test.yaoocai");
        inputFunction.setValue((short) 1);
        vm.execute();
    }

    @Test
    public void test_validCodeHavingLocalVariables_correctReturnValues() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/local-variable-test.yaoocai");
        vm.execute();

        List<Object> reportResult = outputFunction.getValues();
        assertThat(reportResult.get(0), Matchers.equalTo((short) 5));
        assertThat(reportResult.get(1), Matchers.equalTo((short) 5));
        assertThat(reportResult.get(2), Matchers.equalTo((short) 7));
        assertThat(reportResult.get(3), Matchers.equalTo((short) 6));
    }

    @Test
    public void test_validExpressionStatements_correctReturnValues() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/arithmeticExpression-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validDeAndIncrementalExpressionStatements_correctReturnValues() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/incremental-test.yaoocai");
        vm.execute();
        vm = compileAndGetTestVM("/test-sources/decremental-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validBitwiseOperations_correctAsserts() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/bitwiseOperations-test.yaoocai");
        vm.execute();
    }
}
