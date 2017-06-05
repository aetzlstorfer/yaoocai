package org.mufuku.yaoocai.v1.vm;

import org.mufuku.yaoocai.v1.vm.builtins.BuiltInVMFunction;

import java.io.InputStream;
import java.util.Deque;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class TestVM extends YAOOCAI_VM {
    public TestVM(InputStream in, Map<Short, BuiltInVMFunction> builtIns) {
        super(in, builtIns);
    }

    public Deque<Object> getStack() {
        return this.stack;
    }

    public Deque<LocalVariableStack> getLocalVariableStack() {
        return this.localVariableStack;
    }
}
