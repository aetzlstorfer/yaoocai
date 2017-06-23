package org.mufuku.yaoocai.v1.bytecode;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.junit.Test;
import org.mufuku.yaoocai.v1.BaseLangTest;
import org.mufuku.yaoocai.v1.bytecode.data.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mufuku.test.matchers.OwnMatchers.constantPoolItem;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ByteCodeReaderTest extends BaseLangTest {

    @Test
    public void test_compileEmptyFunction_checkByteCodeElementsPresent() throws IOException {
        ByteOutputStream out = new ByteOutputStream();
        compile("/test-bytecode/empty.yaoocai", out);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(out.getBytes(), 0, out.getCount());
        ByteCodeReader reader = new ByteCodeReader(byteIn);

        BCFile file = reader.readByteCode();

        short expectedLocalVariableIndex = 3;
        short expectedIntegerConstantIndex = 2;

        // check constant pool
        BCConstantPool constantPool = file.getConstantPool();

        assertThat(constantPool.getItems().size(), is(equalTo(4)));
        assertThat(constantPool.getItems().get(0),
                constantPoolItem(BCConstantPoolItemType.SYMBOL, "main_unit"));
        assertThat(constantPool.getItems().get(1),
                constantPoolItem(BCConstantPoolItemType.SYMBOL, "main"));
        assertThat(constantPool.getItems().get(expectedIntegerConstantIndex),
                constantPoolItem(BCConstantPoolItemType.INTEGER, 55));
        assertThat(constantPool.getItems().get(expectedLocalVariableIndex),
                constantPoolItem(BCConstantPoolItemType.SYMBOL, "a"));

        // check functions
        BCUnit unit = file.getUnits().getUnits().iterator().next();
        assertThat(unit.getItems().size(), is(equalTo(1)));
        BCUnitItem firstItem = unit.getItems().iterator().next();
        assertThat(firstItem, is(instanceOf(BCUnitItemFunction.class)));

        BCUnitItemFunction function = (BCUnitItemFunction) firstItem;
        assertThat(function.getReturnType().getType(), equalTo(BCTypeType.NO));
        assertThat(function.getLocalVariableTable().getNameAndTypes().size(), equalTo(1));

        // check local variable within function
        BCNameAndType localVariable = function.getLocalVariableTable().getNameAndTypes().get(0);
        assertThat(localVariable.getNameIndex(), is(equalTo(expectedLocalVariableIndex)));

        // check parameters for function
        assertThat(function.getParameters().getNameAndTypes(), is(empty()));

        // check function code
        assertThat(function.getCode().getCode().length, is(equalTo(5)));
        assertThat(function.getCode().getCode(), equalTo(new byte[]{
                InstructionSet.OpCodes.CONST_P1B.code,
                (byte) expectedIntegerConstantIndex,
                InstructionSet.OpCodes.STORE.code,
                0,
                InstructionSet.OpCodes.RETURN.code
        }));
    }

    @Test(expected = IOException.class)
    public void test_invalidPreamble_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeChars(new StringBuilder(InstructionSet.PREAMBLE).reverse().toString());
        new ByteCodeReader(new ByteArrayInputStream(out.toByteArray())).readByteCode();
    }

    @Test(expected = IOException.class)
    public void test_invalidConstantPoolType_error() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);

        // header
        dataOut.writeChars(InstructionSet.PREAMBLE);
        dataOut.writeByte(1);
        dataOut.writeByte(0);

        // length of constant pool
        dataOut.writeShort(1);
        dataOut.writeByte(0xff); // invalid constant pool type

        new ByteCodeReader(new ByteArrayInputStream(out.toByteArray())).readByteCode();
    }

    @Test(expected = IOException.class)
    public void test_invalidCodeLength_error() throws IOException {

        byte[] code = new byte[]{
                (byte) 0xff,
        };

        byte[] version1 = createTestData(code);
        code[0] = (byte) 0xfe; // create a second file with changed code, to see where count index is
        byte[] version2 = createTestData(code);

        int sizeByte = checkDiff(version1, version2) - 1;

        version1[sizeByte] = 99;

        ByteCodeReader reader = new ByteCodeReader(new ByteArrayInputStream(version1));
        reader.readByteCode();
    }

    private byte[] createTestData(byte[] code) throws IOException {
        BCUnitItemFunction function = new BCUnitItemFunction((short) -1);
        function.setParameters(new BCParameters(Collections.emptyList()));
        function.setReturnType(BCType.NO_TYPE);
        function.setCode(new BCCode(code));

        Collection<BCUnitItem> items = Collections.singletonList(function);

        BCUnit unit = new BCUnit((short) -1);
        unit.setItems(items);

        BCUnits units = new BCUnits(Collections.singletonList(unit));

        BCFile file = new BCFile();
        file.setPreamble(InstructionSet.PREAMBLE);
        file.setMinorVersion((byte) 10);
        file.setMajorVersion((byte) 99);

        file.setUnits(units);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteCodeWriter byteCodeWriter = new ByteCodeWriter(out);
        byteCodeWriter.writeByteCode(file);
        return out.toByteArray();
    }

    private int checkDiff(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    @Test(expected = IOException.class)
    public void test_trailingContent_error() throws IOException {
        ByteOutputStream out = new ByteOutputStream();
        compile("/test-bytecode/empty.yaoocai", out);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(out.getBytes(), 0, out.getCount() + 1); // evil, added + 1
        ByteCodeReader reader = new ByteCodeReader(byteIn);
        reader.readByteCode();
    }
}