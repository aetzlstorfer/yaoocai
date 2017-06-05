package org.mufuku.yaoocai.v1;

import org.junit.Assert;
import org.mufuku.yaoocai.v1.assembler.YAOOCAI_AssemblerCompiler;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.viewer.ByteCodeViewer;
import org.mufuku.yaoocai.v1.compiler.LanguageIntegrationTest;
import org.mufuku.yaoocai.v1.compiler.YAOOCAI_Compiler;
import org.mufuku.yaoocai.v1.vm.VirtualMachine;
import org.mufuku.yaoocai.v1.vm.YAOOCAI_VM;
import org.mufuku.yaoocai.v1.vm.builtins.BuiltInVMFunction;
import org.mufuku.yaoocai.v1.vm.builtins.DefaultBuiltIns;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class BaseLangTest {

    protected final Test_Input inputFunction = new Test_Input();
    protected final Test_Output outputFunction = new Test_Output();

    private final BuiltInVMFunction fail = new Fail();

    private final AssertEquals equals = new AssertEquals();

    private final AssertTrue assertTrue = new AssertTrue();

    private final AssertFalse assertFalse = new AssertFalse();

    private String lastFile;

    protected YAOOCAI_Compiler compile(String source, OutputStream byteOut) throws IOException {
        this.lastFile = source;
        InputStream sourceIn = LanguageIntegrationTest.class.getResourceAsStream(source);
        YAOOCAI_Compiler compiler = new YAOOCAI_Compiler(sourceIn, byteOut);
        compiler.compile();
        return compiler;
    }

    protected YAOOCAI_AssemblerCompiler assemble(String source, OutputStream byteOut) throws IOException {
        this.lastFile = source;

        InputStream sourceIn = LanguageIntegrationTest.class.getResourceAsStream(source);
        YAOOCAI_AssemblerCompiler compiler = new YAOOCAI_AssemblerCompiler(sourceIn, byteOut);
        compiler.compile();
        return compiler;
    }

    protected YAOOCAI_VM compileAndGetTestVM(String source) throws IOException {

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        compile(source, byteOut);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ByteCodeViewer byteCodeViewer = new ByteCodeViewer(byteIn, InstructionSet.MAJOR_VERSION, InstructionSet.MINOR_VERSION, System.out);
        byteCodeViewer.convert();
        byteIn.reset();

        Map<Short, BuiltInVMFunction> testBuiltIns = new HashMap<>();

        testBuiltIns.putAll(DefaultBuiltIns.getBuiltIns());

        testBuiltIns.put((short) 32000, inputFunction);
        testBuiltIns.put((short) 32001, outputFunction);
        testBuiltIns.put((short) 32002, fail);
        testBuiltIns.put((short) 32003, equals);
        testBuiltIns.put((short) 32004, assertTrue);
        testBuiltIns.put((short) 32005, assertFalse);

        return new YAOOCAI_VM(byteIn, testBuiltIns);
    }

    public String getLastFile() {
        return lastFile;
    }

    protected List<String> getTestFiles(String dir) throws IOException {
        try {
            File rootDir = new File(BaseLangTest.class.getResource("/").toURI());
            File baseDir = new File(BaseLangTest.class.getResource(dir).toURI());
            File[] files = baseDir.listFiles(f -> f.getName().endsWith(".yaoocai"));
            if (files == null) {
                return Collections.emptyList();
            } else {
                return Arrays.stream(files).map(f -> "/" + rootDir.toPath().relativize(f.toPath()).toString()).collect(Collectors.toList());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static class Test_Input implements BuiltInVMFunction {
        private Object value;

        @Override
        public void handle(Deque<Object> stack, VirtualMachine vm) {
            stack.push(value);
        }

        public void setValue(Object value) {
            this.value = value;
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

        public Object getLast() {
            return values.getLast();
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
