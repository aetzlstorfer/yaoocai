package org.mufuku.yaoocai.v1;

import org.mufuku.yaoocai.v1.bytecode.viewer.ByteCodeViewer;
import org.mufuku.yaoocai.v1.compiler.YAOOCAI_Compiler;
import org.mufuku.yaoocai.v1.vm.BuiltInVMFunction;
import org.mufuku.yaoocai.v1.vm.VirtualMachine;
import org.mufuku.yaoocai.v1.vm.YAOOCAI_VM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public abstract class BaseLangTest {

    final Test_Input inputFunction = new Test_Input();
    final Test_Output outputFunction = new Test_Output();
    private final BuiltInVMFunction fail = new Fail();
    private final AssertEquals equals = new AssertEquals();
    private final AssertTrue assertTrue = new AssertTrue();
    private final AssertFalse assertFalse = new AssertFalse();
    private String lastFile;
    private YAOOCAI_VM lastVM;

    protected YAOOCAI_VM compileAndGetTestVM(String source) throws IOException {

        this.lastFile = source;

        InputStream sourceIn = LanguageIntegrationTest.class.getResourceAsStream(source);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        int fileSize = sourceIn.available();

        YAOOCAI_Compiler compiler = new YAOOCAI_Compiler(sourceIn, byteOut);
        compiler.compile();

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());

        if (TestConstants.DEBUG_PRINT_BYTE_CODE) {
            System.out.println("handling file: " + source);

            if (TestConstants.DEBUG_PRINT_BYTE_CODE_BYTES_RATIO) {
                NumberFormat dfmt = NumberFormat.getNumberInstance(Locale.ENGLISH);
                dfmt.setMaximumFractionDigits(2);
                System.out.println("source file size / byte code size / ratio: " + fileSize + " / " + byteOut.toByteArray().length + " / " +
                        dfmt.format((double) byteOut.toByteArray().length / (double) fileSize)
                );
            }

            System.out.println("byte code:");
            ByteCodeViewer byteCodeViewer = new ByteCodeViewer(byteIn, Constants.MAJOR_VERSION, Constants.MINOR_VERSION, System.out);
            byteCodeViewer.convert();
            byteIn.reset();
        }

        Map<Short, BuiltInVMFunction> testBuiltIns = new HashMap<>();
        testBuiltIns.put((short) 32000, inputFunction);
        testBuiltIns.put((short) 32001, outputFunction);
        testBuiltIns.put((short) 32002, fail);
        testBuiltIns.put((short) 32003, equals);
        testBuiltIns.put((short) 32004, assertTrue);
        testBuiltIns.put((short) 32005, assertFalse);

        lastVM = new YAOOCAI_VM(byteIn, testBuiltIns);
        return lastVM;
    }

    public String getLastFile() {
        return lastFile;
    }

    public YAOOCAI_VM getLastVM() {
        return lastVM;
    }

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
            fail();
        }
    }

    public static class AssertEquals implements BuiltInVMFunction {
        @Override
        public void handle(Stack<Object> stack, VirtualMachine vm) {
            Object v1 = stack.pop();
            Object v2 = stack.pop();
            assertEquals("Fail within the code", v2, v1);
        }
    }

    // FOR #8
//    @After
//    @SuppressWarnings("unchecked")
//    public void afterTest() throws ReflectiveOperationException {
//        // look inside the vm and check if stack is cleared properly
//        Field stackField = lastVM.getClass().getDeclaredField("stack");
//        stackField.setAccessible(true);
//        Stack<Object> stack = (Stack<Object>) stackField.get(lastVM);
//        assertThat(stack, empty());
//
//        Field localVariableStackField = lastVM.getClass().getDeclaredField("localVariableStack");
//        localVariableStackField.setAccessible(true);
//
//        Field callStackField = lastVM.getClass().getDeclaredField("callStack");
//        callStackField.setAccessible(true);
//    }

    public static class AssertTrue implements BuiltInVMFunction {
        @Override
        public void handle(Stack<Object> stack, VirtualMachine vm) {
            Object v1 = stack.pop();
            assertTrue((Boolean) v1);
        }
    }

    public static class AssertFalse implements BuiltInVMFunction {
        @Override
        public void handle(Stack<Object> stack, VirtualMachine vm) {
            Object v1 = stack.pop();
            assertFalse((Boolean) v1);
        }
    }
}
