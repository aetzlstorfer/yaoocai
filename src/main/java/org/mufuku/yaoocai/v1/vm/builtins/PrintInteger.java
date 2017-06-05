package org.mufuku.yaoocai.v1.vm.builtins;

import org.mufuku.yaoocai.v1.vm.VirtualMachine;

import java.util.Deque;

public class PrintInteger implements BuiltInVMFunction {

    @Override
    public void handle(Deque<Object> stack, VirtualMachine vm)
    {
        Object value = stack.pop();
        vm.getOut().println(value);
    }
}
