package org.mufuku.yaoocai.v1.compiler;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;
import org.mufuku.yaoocai.v1.vm.VM;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class LanguageIntegrationTest extends BaseLangTest {

    @BeforeClass
    public static void init() {
        BaseLangTest.PRINT_BYTECODE = true;
        BaseLangTest.PERFORM_STATISTICS = true;
    }

    @Test(expected = ParsingException.class)
    public void test_invalidSyntax1_failWithParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalid-syntax-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidSyntax2_failWithParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalid-syntax-02-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validComparisonExpressions_noFailCall() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/comparison-test.yaoocai");
        inputFunction.setValue((short) 1);
        vm.execute();
    }

    @Test
    public void test_validCodeWithIfBranch_noFailCall() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/if-branches-test.yaoocai");
        inputFunction.setValue((short) 1);
        vm.execute();
    }

    @Test
    public void test_validCodeHavingLocalVariables_correctReturnValues() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/local-variable-test.yaoocai");
        vm.execute();

        List<Object> reportResult = outputFunction.getValues();
        assertThat(reportResult.get(0), Matchers.equalTo((short) 5));
        assertThat(reportResult.get(1), Matchers.equalTo((short) 5));
        assertThat(reportResult.get(2), Matchers.equalTo((short) 7));
        assertThat(reportResult.get(3), Matchers.equalTo((short) 6));
    }

    @Test
    public void test_validExpressionStatements_correctReturnValues() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/arithmeticExpression-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validPreDeAndIncrementalExpressionStatements_correctReturnValues() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/pre-incremental-test.yaoocai");
        vm.execute();
        vm = compileAndGetTestVM("/test-sources/positive/pre-decremental-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validPostDeAndIncrementalExpressionStatements_correctReturnValues() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/post-incremental-test.yaoocai");
        vm.execute();
        vm = compileAndGetTestVM("/test-sources/positive/post-decremental-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validBitwiseOperations_correctAsserts() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/bitwiseOperations-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_validAssignmentStatements_correctChanges() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/assignments-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_conditionalOrExpressions_NoFail() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/conditional-or-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_conditionalAndExpressions_NoFail() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/conditional-and-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_combinedConditionalExpressions_NoFail() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/conditional-combination-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_combinedConditionalExpressions_CorrectPrecedence() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/conditional-precedence-test.yaoocai");
        vm.execute();

        assertThat(outputFunction.getValues(), is(equalTo(
                Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6))));
    }

    @Test
    public void test_codeWithValidComments_normalRun() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/commenting-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments1_exception() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/commenting-02-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments2_exception() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/commenting-03-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_codeWithWhile_normalRun() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/while-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_codeWithElseIfBranches_normalRun() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/if-elseif-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_subBlock_normalRun() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/subBlock-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_declaringIntegersWithGoodRange_normalRun() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/goodIntegerRanges-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_declaringIntegersWithBadRange_parsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/badIntegerRanges-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_declareBuiltInFunctionWithNegativeIndex_parsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/badFunctionIndex-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_declareBuiltInFunctionWithOutOfRangeIndex_parsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/badFunctionIndex-02-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_stackPollutingStatements_ClearStack() throws IOException {
        inputFunction.setValue(1);
        VM vm = compileAndGetTestVM("/test-sources/positive/clearStack-test.yaoocai");
        vm.execute();
    }

    @Test
    public void test_advancedIdentifiers_ValidCompilation() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/advancedIdentifiers-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_duplicateIdentifier_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/duplicate-function-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_duplicateIdentifierOneFunctionOtherBuiltIn_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/duplicate-function-02-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_duplicateParameters_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/duplicate-variables-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_duplicateParameterAndVariable_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/duplicate-variables-02-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_duplicateVariables_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/duplicate-variables-03-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_useInvalidVariable_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalidVariable-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement01_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalid-statement-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement02_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalid-statement-02-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement03_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalid-statement-03-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement04_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalid-statement-04-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement05_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/invalid-statement-05-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes01_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-01-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes02_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-02-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes03_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-03-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes04_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-04-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes05_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-05-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes06_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-06-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes07_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-07-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes08_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-08-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes09_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-09-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes10_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-10-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes11_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-11-test.yaoocai");
        vm.execute();
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes12_ParsingException() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/negative/incompatible-types-12-test.yaoocai");
        vm.execute();
    }
}
