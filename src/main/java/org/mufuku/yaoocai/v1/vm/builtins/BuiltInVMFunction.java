package org.mufuku.yaoocai.v1.vm.builtins;

import org.mufuku.yaoocai.v1.vm.VirtualMachine;

import java.util.Deque;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public interface BuiltInVMFunction {

    void handle(Deque<Object> stack, VirtualMachine vm);

}
