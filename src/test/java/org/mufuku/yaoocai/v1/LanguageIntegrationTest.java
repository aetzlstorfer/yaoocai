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
    public void test_validCodeWithIfBranch_noFailCall() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/if-branches-test.yaoocai");
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
    public void test_codeWithValidComments_normalRun() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/commenting-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments1_exception() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/commenting-02-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments2_exception() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/commenting-03-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_codeWithWhile_normalRun() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/while-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_codeWithElseIfBranches_normalRun() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/if-elseif-test.yaoocai");
        vm.execute();
    }
}
