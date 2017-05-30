package org.mufuku.yaoocai.v1.compiler;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;
import org.mufuku.yaoocai.v1.vm.YAOOCAI_VM;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author andreas.etzlstorfer@ecx.io
 */
public class LanguageIntegrationTest extends BaseLangTest {

    @Test(expected = ParsingException.class)
    public void test_invalidSyntax1_failWithParsingException() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/negative/invalid-syntax-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidSyntax2_failWithParsingException() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/negative/invalid-syntax-02-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validComparisonExpressions_noFailCall() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/comparison-test.yaoocai");
        inputFunction.setValue((short) 1);
        vm.execute();
    }

    @Test
    public void test_validCodeWithIfBranch_noFailCall() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/if-branches-test.yaoocai");
        inputFunction.setValue((short) 1);
        vm.execute();
    }

    @Test
    public void test_validCodeHavingLocalVariables_correctReturnValues() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/local-variable-test.yaoocai");
        vm.execute();

        List<Object> reportResult = outputFunction.getValues();
        assertThat(reportResult.get(0), Matchers.equalTo((short) 5));
        assertThat(reportResult.get(1), Matchers.equalTo((short) 5));
        assertThat(reportResult.get(2), Matchers.equalTo((short) 7));
        assertThat(reportResult.get(3), Matchers.equalTo((short) 6));
    }

    @Test
    public void test_validExpressionStatements_correctReturnValues() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/arithmeticExpression-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validPreDeAndIncrementalExpressionStatements_correctReturnValues() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/pre-incremental-test.yaoocai");
        vm.execute();
        vm = compileAndGetTestVM("/test-sources/positive/pre-decremental-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validPostDeAndIncrementalExpressionStatements_correctReturnValues() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/post-incremental-test.yaoocai");
        vm.execute();
        vm = compileAndGetTestVM("/test-sources/positive/post-decremental-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validBitwiseOperations_correctAsserts() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/bitwiseOperations-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validAssignmentStatements_correctChanges() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/assignments-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_conditionalOrExpressions_NoFail() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/conditional-or-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_conditionalAndExpressions_NoFail() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/conditional-and-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_combinedConditionalExpressions_NoFail() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/conditional-combination-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_combinedConditionalExpressions_CorrectPrecedence() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/conditional-precedence-test.yaoocai");
        vm.execute();

        assertThat(outputFunction.getValues(), is(equalTo(
                Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6))));
    }

    @Test
    public void test_codeWithValidComments_normalRun() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/commenting-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments1_exception() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/negative/commenting-02-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments2_exception() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/negative/commenting-03-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_codeWithWhile_normalRun() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/while-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_codeWithElseIfBranches_normalRun() throws IOException {
        YAOOCAI_VM vm = compileAndGetTestVM("/test-sources/positive/if-elseif-test.yaoocai");
        vm.execute();
    }
}
