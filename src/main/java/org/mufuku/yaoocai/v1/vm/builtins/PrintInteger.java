package org.mufuku.yaoocai.v1.vm.builtins;

import org.mufuku.yaoocai.v1.vm.BuiltInVMFunction;
import org.mufuku.yaoocai.v1.vm.VirtualMachine;

import java.util.Stack;

/**
 * @author andreas.etzlstorfer@ecx.io
 */
public class PrintInteger implements BuiltInVMFunction {
    @Override
    public void handle(Stack<Object> stack, VirtualMachine vm) {
        Object value = stack.pop();
        // TODO check for integer
        System.out.println(value);
    }
}
