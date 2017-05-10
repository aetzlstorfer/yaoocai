package org.mufuku.yaoocai.v1;

import org.junit.Assert;
import org.mufuku.yaoocai.v1.compiler.YAOOCAI_Compiler;
import org.mufuku.yaoocai.v1.vm.BuiltInVMFunction;
import org.mufuku.yaoocai.v1.vm.VirtualMachine;
import org.mufuku.yaoocai.v1.vm.YAOOCAI_VM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class BaseLangTest {

    private String lastFile;

    public static class Test_Input implements BuiltInVMFunction {
        private Object value;

        @Override
        public void handle(Stack<Object> stack, VirtualMachine vm) {
            stack.push(value);
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static class Test_Output implements BuiltInVMFunction {
        private final LinkedList<Object> values = new LinkedList<>();

        @Override
        public void handle(Stack<Object> stack, VirtualMachine vm) {
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
        public void handle(Stack<Object> stack, VirtualMachine vm) {
            Assert.fail();
        }
    }

    public static class AssertEquals implements BuiltInVMFunction {
        @Override
        public void handle(Stack<Object> stack, VirtualMachine vm) {
            Object v1 = stack.pop();
            Object v2 = stack.pop();
            Assert.assertEquals("Fail within the code", v2, v1);
        }
    }

    protected final Test_Input inputFunction = new Test_Input();
    protected final Test_Output outputFunction = new Test_Output();
    protected final BuiltInVMFunction fail = new Fail();
    protected final AssertEquals equals = new AssertEquals();

    protected YAOOCAI_VM compileAndGetTestVM(String source) throws IOException {
        InputStream sourceIn = LanguageIntegrationTest.class.getResourceAsStream(source);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        YAOOCAI_Compiler compiler = new YAOOCAI_Compiler(sourceIn, byteOut);
        compiler.compile();

        Map<Short, BuiltInVMFunction> testBuiltIns = new HashMap<>();
        testBuiltIns.put((short) 32000, inputFunction);
        testBuiltIns.put((short) 32001, outputFunction);
        testBuiltIns.put((short) 32002, fail);
        testBuiltIns.put((short) 32003, equals);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        this.lastFile = source;
        return new YAOOCAI_VM(byteIn, testBuiltIns);
    }

    public String getLastFile() {
        return lastFile;
    }
}
