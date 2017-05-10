package org.mufuku.yaoocai.v1.vm;

import java.util.Stack;

/**
 * @author andreas.etzlstorfer@ecx.io
 */
public interface BuiltInVMFunction {

    void handle(Stack<Object> stack, VirtualMachine vm);

}
