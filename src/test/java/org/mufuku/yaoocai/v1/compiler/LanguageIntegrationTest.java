package org.mufuku.yaoocai.v1.compiler;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;
import org.mufuku.yaoocai.v1.vm.VM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @AfterClass
    public static void afterChecks_checkEverythingExecuted() throws IOException {
        List<String> targetFiles = new ArrayList<>();
        targetFiles.addAll(getTestFiles("/test-language-integration/negative"));
        targetFiles.addAll(getTestFiles("/test-language-integration/positive"));
        assertTrue(targetFiles.containsAll(executedFiles));
    }


    @Test(expected = ParsingException.class)
    public void test_invalidSyntax1_failWithParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-syntax-01-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidSyntax2_failWithParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-syntax-02-test.yaoocai");
    }

    @Test
    public void test_validComparisonExpressions_noFailCall() throws IOException {
        VM vm = compileAndGetTestVM("/test-language-integration/positive/comparison-test.yaoocai");
        inputFunction.setValue(1);
        vm.execute();
    }

    @Test
    public void test_validCodeWithIfBranch_noFailCall() throws IOException {
        VM vm = compileAndGetTestVM("/test-language-integration/positive/if-branches-test.yaoocai");
        inputFunction.setValue(1);
        vm.execute();
    }

    @Test
    public void test_validCodeHavingLocalVariables_correctReturnValues() throws IOException {
        execute("/test-language-integration/positive/local-variable-test.yaoocai");

        List<Object> reportResult = outputFunction.getValues();
        assertThat(reportResult.get(0), Matchers.equalTo(5));
        assertThat(reportResult.get(1), Matchers.equalTo(5));
        assertThat(reportResult.get(2), Matchers.equalTo(7));
        assertThat(reportResult.get(3), Matchers.equalTo(6));
    }

    @Test
    public void test_validExpressionStatements_correctReturnValues() throws IOException {
        execute("/test-language-integration/positive/arithmeticExpression-test.yaoocai");
    }

    @Test
    public void test_validPreDeAndIncrementalExpressionStatements_correctReturnValues() throws IOException {
        execute("/test-language-integration/positive/pre-incremental-test.yaoocai");
        execute("/test-language-integration/positive/pre-decremental-test.yaoocai");
    }

    @Test
    public void test_validPostDeAndIncrementalExpressionStatements_correctReturnValues() throws IOException {
        execute("/test-language-integration/positive/post-incremental-test.yaoocai");
        execute("/test-language-integration/positive/post-decremental-test.yaoocai");
    }

    @Test
    public void test_validBitwiseOperations_correctAsserts() throws IOException {
        execute("/test-language-integration/positive/bitwiseOperations-test.yaoocai");
    }

    @Test
    public void test_validAssignmentStatements_correctChanges() throws IOException {
        execute("/test-language-integration/positive/assignments-test.yaoocai");
    }

    @Test
    public void test_conditionalOrExpressions_NoFail() throws IOException {
        execute("/test-language-integration/positive/conditional-or-test.yaoocai");
    }

    @Test
    public void test_conditionalAndExpressions_NoFail() throws IOException {
        execute("/test-language-integration/positive/conditional-and-test.yaoocai");
    }

    @Test
    public void test_combinedConditionalExpressions_NoFail() throws IOException {
        execute("/test-language-integration/positive/conditional-combination-test.yaoocai");
    }

    @Test
    public void test_combinedConditionalExpressions_CorrectPrecedence() throws IOException {
        execute("/test-language-integration/positive/conditional-precedence-test.yaoocai");

        assertThat(outputFunction.getValues(), is(equalTo(
                Arrays.asList(1, 2, 3, 4, 5, 6))));
    }

    @Test
    public void test_codeWithValidComments_normalRun() throws IOException {
        execute("/test-language-integration/positive/commenting-01-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments1_exception() throws IOException {
        execute("/test-language-integration/negative/commenting-02-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_codeWithInvalidComments2_exception() throws IOException {
        execute("/test-language-integration/negative/commenting-03-test.yaoocai");
    }

    @Test
    public void test_codeWithWhile_normalRun() throws IOException {
        execute("/test-language-integration/positive/while-test.yaoocai");
    }

    @Test
    public void test_codeWithElseIfBranches_normalRun() throws IOException {
        execute("/test-language-integration/positive/if-elseif-test.yaoocai");
    }

    @Test
    public void test_subBlock_normalRun() throws IOException {
        execute("/test-language-integration/positive/subBlock-test.yaoocai");
    }

    @Test
    public void test_declaringIntegersWithGoodRange_normalRun() throws IOException {
        execute("/test-language-integration/positive/goodIntegerRanges-test.yaoocai");
    }

    @Test
    public void test_declaringIntegersWithBadRange_01_parsingException() throws IOException {
        assertThatThrownBy(() ->
                execute("/test-language-integration/negative/badIntegerRanges-01-test.yaoocai"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("Integer must be in the range")
                .hasFieldOrPropertyWithValue("lineNumber", 3);
    }

    @Test(expected = ParsingException.class)
    public void test_declaringIntegersWithBadRange_02_parsingException() throws IOException {
        execute("/test-language-integration/negative/badIntegerRanges-02-test.yaoocai");
    }

    @Test
    public void test_stackPollutingStatements_ClearStack() throws IOException {
        inputFunction.setValue(1);
        execute("/test-language-integration/positive/clearStack-test.yaoocai");
    }

    @Test
    public void test_advancedIdentifiers_ValidCompilation() throws IOException {
        execute("/test-language-integration/positive/advancedIdentifiers-test.yaoocai");
    }

    @Test
    public void test_duplicateIdentifier_ParsingException() throws IOException {
        assertThatThrownBy(() ->
                execute("/test-language-integration/negative/duplicate-function-01-test.yaoocai")
        )
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("Already defined function: ")
                .hasFieldOrPropertyWithValue("lineNumber", 5);
    }

    @Test
    public void test_duplicateIdentifierOneFunctionOtherBuiltIn_ParsingException() throws IOException {
        assertThatThrownBy(() ->
                execute("/test-language-integration/negative/duplicate-function-02-test.yaoocai")
        )
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("Already defined function: ")
                .hasFieldOrPropertyWithValue("lineNumber", 4);
    }

    @Test
    public void test_duplicateParameters_ParsingException() throws IOException {
        assertThatThrownBy(() ->
                execute("/test-language-integration/negative/duplicate-variables-01-test.yaoocai")
        )
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("Duplicate variable")
                .hasFieldOrPropertyWithValue("lineNumber", 2);
    }

    @Test
    public void test_duplicateParameterAndVariable_ParsingException() throws IOException {
        assertThatThrownBy(() ->
                execute("/test-language-integration/negative/duplicate-variables-02-test.yaoocai")
        )
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("Duplicate variable")
                .hasFieldOrPropertyWithValue("lineNumber", 3);
    }

    @Test
    public void test_duplicateVariables_ParsingException() throws IOException {
        assertThatThrownBy(() ->
                execute("/test-language-integration/negative/duplicate-variables-03-test.yaoocai")
        )
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("Duplicate variable")
                .hasFieldOrPropertyWithValue("lineNumber", 4);
    }

    @Test(expected = ParsingException.class)
    public void test_useInvalidVariable_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalidVariable-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement01_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-statement-01-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement02_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-statement-02-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement03_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-statement-03-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement04_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-statement-04-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement05_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-statement-05-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement06_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-statement-06-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_invalidStatement07_ParsingException() throws IOException {
        execute("/test-language-integration/negative/invalid-statement-07-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes01_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-01-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes02_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-02-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes03_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-03-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes04_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-04-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes05_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-05-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes06_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-06-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes07_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-07-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes08_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-08-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes09_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-09-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes10_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-10-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes11_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-11-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_incompatibleTypes12_ParsingException() throws IOException {
        execute("/test-language-integration/negative/incompatible-types-12-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_flowReturn01_ParsingException() throws IOException {
        execute("/test-language-integration/negative/flow-return-01-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_flowReturn02_ParsingException() throws IOException {
        execute("/test-language-integration/negative/flow-return-02-test.yaoocai");
    }

    @Test
    public void test_flowReturn03_validRun() throws IOException {
        execute("/test-language-integration/positive/flow-return-03-test.yaoocai");
    }

    @Test(expected = ParsingException.class)
    public void test_flowReturn04_validRun() throws IOException {
        execute("/test-language-integration/negative/flow-return-04-test.yaoocai");
    }

    @Test
    public void test_constantPoolWideAreaTest_validRun() throws IOException {
        execute("/test-language-integration/positive/bigConstantPoolArea.yaoocai");
    }
}
