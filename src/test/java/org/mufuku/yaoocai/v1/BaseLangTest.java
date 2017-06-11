package org.mufuku.yaoocai.v1;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.mufuku.yaoocai.v1.assembler.AssemblerCompiler;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.viewer.ByteCodeViewer;
import org.mufuku.yaoocai.v1.compiler.Compiler;
import org.mufuku.yaoocai.v1.compiler.LanguageIntegrationTest;
import org.mufuku.yaoocai.v1.vm.TestVM;
import org.mufuku.yaoocai.v1.vm.VM;
import org.mufuku.yaoocai.v1.vm.VirtualMachine;
import org.mufuku.yaoocai.v1.vm.builtins.BuiltInVMFunction;
import org.mufuku.yaoocai.v1.vm.builtins.DefaultBuiltIns;

import java.io.*;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class BaseLangTest {

    protected static boolean PERFORM_STATISTICS = false;
    protected static boolean PRINT_BYTECODE = false;
    protected static Set<String> executedFiles;

    private static long totalFileSize = 0;
    private static long totalByteCodeSize = 0;
    private static ZipOutputStream zipOutputStream;
    private static ByteArrayOutputStream outputStream;

    protected final Test_Input inputFunction = new Test_Input();
    protected final Test_Output outputFunction = new Test_Output();

    private final BuiltInVMFunction fail = new Fail();
    private final AssertEquals equals = new AssertEquals();
    private final AssertTrue assertTrue = new AssertTrue();
    private final AssertFalse assertFalse = new AssertFalse();

    private TestVM lastVM;

    @BeforeClass
    public static void startCollectingStatistics() throws FileNotFoundException {
        outputStream = new ByteArrayOutputStream();
        zipOutputStream = new ZipOutputStream(outputStream);
        totalByteCodeSize = 0;
        totalFileSize = 0;
        executedFiles = new HashSet<>();
    }

    @AfterClass
    public static void stopCollectingStatistics() throws IOException {
        if (PERFORM_STATISTICS) {
            long compressedByteCodeSize = outputStream.toByteArray().length;

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
            formatter.setMaximumFractionDigits(0);

            System.out.println("******* compilation result ******* ");
            System.out.println("total source code size compiled : " + FileUtils.byteCountToDisplaySize(totalFileSize) + " (" + totalFileSize + ")");
            System.out.println("total byte code size produced   : " + FileUtils.byteCountToDisplaySize(totalByteCodeSize) + " (" + totalByteCodeSize + ")");
            System.out.println("ratio (source/byte code)        : " + formatter.format((double) totalByteCodeSize / totalFileSize * 100) + "%");
            System.out.println();
            System.out.println("compressed byte code size       : " + FileUtils.byteCountToDisplaySize(compressedByteCodeSize) + " (" + compressedByteCodeSize + ")");
            System.out.println("ratio (compression)             : " + formatter.format((double) compressedByteCodeSize / totalByteCodeSize * 100) + "%");
            System.out.println();
            System.out.println("ratio (source/compr. byte code) : " + formatter.format((double) compressedByteCodeSize / totalFileSize * 100) + "%");

            PERFORM_STATISTICS = false;
        }

        IOUtils.closeQuietly(zipOutputStream);

        if (PRINT_BYTECODE) {
            PRINT_BYTECODE = false;
        }

        executedFiles.clear();
    }

    protected static List<String> getTestFiles(String dir) throws IOException {
        try {
            File rootDir = new File(BaseLangTest.class.getResource("/").toURI());
            File baseDir = new File(BaseLangTest.class.getResource(dir).toURI());
            File[] files = baseDir.listFiles(f -> f.getName().endsWith(".yaoocai"));
            if (files == null) {
                return Collections.emptyList();
            } else {
                return Arrays.stream(files).map(f -> "/" + rootDir.toPath().relativize(f.toPath()).toString().replace('\\', '/')).collect(Collectors.toList());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @After
    public void afterChecks() {
        checkIfVmWasClearedUpCorrectly();
        checkIfInputFunctionWasUsed();
        printTestCoverage();
    }

    private void checkIfVmWasClearedUpCorrectly() {
        if (lastVM != null) {
            assertThat(lastVM.getStack(), is(empty()));
            assertThat(lastVM.getLocalVariableStack(), is(empty()));
        }
    }

    private void checkIfInputFunctionWasUsed() {
        assertTrue(inputFunction.valueStack.isEmpty());
    }

    private void printTestCoverage() {
        if (lastVM != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
            formatter.setMaximumFractionDigits(0);
            System.out.println("code coverage (%): " + formatter.format(lastVM.getPercentOfInstructionsCalled()));
            assertThat("Tested code must have at least 80% of coverage", lastVM.getPercentOfInstructionsCalled(), is(greaterThan(70.d)));
        }
    }

    protected void compile(String source, OutputStream byteOut) throws IOException {
        InputStream sourceIn = LanguageIntegrationTest.class.getResourceAsStream(source);
        Compiler compiler = new Compiler(sourceIn, byteOut);
        compiler.compile();
    }

    protected void assemble(String source, OutputStream byteOut) throws IOException {
        InputStream sourceIn = LanguageIntegrationTest.class.getResourceAsStream(source);
        AssemblerCompiler compiler = new AssemblerCompiler(sourceIn, byteOut);
        compiler.compile();
    }

    protected VM compileAndGetTestVM(String source) throws IOException {
        executedFiles.add(source);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        compile(source, byteOut);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        if (PERFORM_STATISTICS) {
            System.out.println("handling file: " + source);

            long fileSize = new File(BaseLangTest.class.getResource(source).getFile()).length();
            totalFileSize += fileSize;

            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
            numberFormat.setMaximumFractionDigits(2);
            System.out.println("source file size / byte code size / ratio: " + fileSize + " / " + byteOut.toByteArray().length + " / " +
                    numberFormat.format((double) byteOut.toByteArray().length / (double) fileSize)
            );

            ZipEntry entry = new ZipEntry("/" + source);
            zipOutputStream.putNextEntry(entry);
            int byteCodeSize = IOUtils.copy(byteIn, zipOutputStream);
            totalByteCodeSize += byteCodeSize;
            byteIn.reset();
        }

        if (PRINT_BYTECODE) {
            ByteCodeViewer byteCodeViewer = new ByteCodeViewer(byteIn, InstructionSet.MAJOR_VERSION, InstructionSet.MINOR_VERSION, System.out);
            byteCodeViewer.convert();
            byteIn.reset();
        }

        Map<Short, BuiltInVMFunction> testBuiltIns = new HashMap<>();

        testBuiltIns.putAll(DefaultBuiltIns.STANDARD_BUILT_INS);

        testBuiltIns.put((short) 32000, inputFunction);
        testBuiltIns.put((short) 32001, outputFunction);
        testBuiltIns.put((short) 32002, fail);
        testBuiltIns.put((short) 32003, equals);
        testBuiltIns.put((short) 32004, assertTrue);
        testBuiltIns.put((short) 32005, assertFalse);

        this.lastVM = new TestVM(byteIn, testBuiltIns);
        return this.lastVM;
    }

    protected void execute(String source) throws IOException {
        VM vm = compileAndGetTestVM(source);
        vm.execute();
    }

    public static class Test_Input implements BuiltInVMFunction {
        private final Deque<Object> valueStack = new ArrayDeque<>();

        @Override
        public void handle(Deque<Object> stack, VirtualMachine vm) {
            Object lastValue = valueStack.pop();
            stack.push(lastValue);
        }

        public void setValue(Object value) {
            this.valueStack.push(value);
        }
    }

    public static class Test_Output implements BuiltInVMFunction {
        private final LinkedList<Object> values = new LinkedList<>();

        @Override
        public void handle(Deque<Object> stack, VirtualMachine vm) {
            Object value = stack.pop();
            values.add(value);
        }

        public List<Object> getValues() {
            return values;
        }
    }

    public static class Fail implements BuiltInVMFunction {
        @Override
        public void handle(Deque<Object> stack, VirtualMachine vm) {
            Assert.fail();
        }
    }

    public static class AssertEquals implements BuiltInVMFunction {
        @Override
        public void handle(Deque<Object> stack, VirtualMachine vm) {
            Object v1 = stack.pop();
            Object v2 = stack.pop();
            Assert.assertEquals("Fail within the code", v2, v1);
        }
    }

    public static class AssertTrue implements BuiltInVMFunction {
        @Override
        public void handle(Deque<Object> stack, VirtualMachine vm) {
            Object v1 = stack.pop();
            assertTrue((Boolean) v1);
        }
    }

    public static class AssertFalse implements BuiltInVMFunction {
        @Override
        public void handle(Deque<Object> stack, VirtualMachine vm) {
            Object v1 = stack.pop();
            assertFalse((Boolean) v1);
        }
    }
}
