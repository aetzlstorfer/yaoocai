package org.mufuku.yaoocai.v1.vm.builtins;

import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.vm.VM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class PrintIntegerTest extends BaseLangTest {

    @Test
    public void test_testProgram_expectedOutput() throws IOException {
        VM vm = compileAndGetTestVM("/test-sources/positive/printInteger-test.yaoocai");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printOut = new PrintStream(out);

        vm.setOut(printOut);
        vm.execute();

        String nl = System.lineSeparator();
        String consoleOutput = out.toString("UTF-8");
        assertThat(consoleOutput, is(equalTo("123" + nl + "666" + nl)));
    }
}