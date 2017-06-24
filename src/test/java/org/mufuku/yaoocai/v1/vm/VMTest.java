package org.mufuku.yaoocai.v1.vm;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mufuku.yaoocai.v1.bytecode.ByteCodeWriter;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.data.*;
import org.mufuku.yaoocai.v1.compiler.Compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class VMTest {

    @Test
    public void test_noMainMethod_exception() throws IOException {
        Assertions.assertThatThrownBy(() -> {
                    InputStream in = new ByteArrayInputStream("".getBytes());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Compiler compiler = new Compiler(in, out);
                    compiler.compile();
                    InputStream byteIn = new ByteArrayInputStream(out.toByteArray());
                    VM vm = new VM(byteIn);
                    vm.execute();
                }
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find function: main");
    }

    @Test
    public void test_InvalidOpCode_exception() throws IOException {
        Assertions.assertThatThrownBy(this::performExecutionWithInvalidOpCode)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid op code: ");
    }

    private void performExecutionWithInvalidOpCode() throws IOException {
        BCConstantPoolItem<String> item = new BCConstantPoolItem<>();
        item.setIndex((short) 0);
        item.setType(BCConstantPoolItemType.SYMBOL);
        item.setValue("main");

        BCConstantPool constantPool = new BCConstantPool(Collections.singletonList(item));

        byte[] defunctCode = new byte[]{
                Byte.MAX_VALUE
        };

        BCUnitItemFunction function = new BCUnitItemFunction((short) 0);
        function.setCode(new BCCode(defunctCode));
        function.setParameters(new BCParameters(Collections.emptyList()));
        function.setReturnType(BCType.NO_TYPE);

        Collection<BCUnitItem> items = Collections.singletonList(function);

        BCUnit unit = new BCUnit((short) -1);
        unit.setItems(items);

        Collection<BCUnit> units = Collections.singletonList(unit);

        BCFile file = new BCFile();
        file.setUnits(new BCUnits(units));
        file.setPreamble(InstructionSet.PREAMBLE);
        file.setMajorVersion((byte) 1);
        file.setMinorVersion((byte) 2);
        file.setConstantPool(constantPool);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteCodeWriter writer = new ByteCodeWriter(out);
        writer.writeByteCode(file);

        InputStream byteCodeIn = new ByteArrayInputStream(out.toByteArray());
        VM vm = new VM(byteCodeIn);
        vm.execute();
    }

}