package org.mufuku.yaoocai.v1.vm;

import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.vm.builtins.BuiltInVMFunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Deque;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class TestVM extends YAOOCAI_VM {

    private BitSet executedOpCodes;

    public TestVM(InputStream in, Map<Short, BuiltInVMFunction> builtIns) {
        super(in, builtIns);
    }

    @Override
    protected void readCode() throws IOException {
        super.readCode();
        executedOpCodes = new BitSet(this.code.length);
    }

    @Override
    protected void executeNextInstruction() {
        short opCode = this.code[this.codePointer];
        InstructionSet.OpCodes opCodeInstruction = InstructionSet.OpCodes.get(opCode);
        executedOpCodes.set(codePointer, codePointer + 1 + opCodeInstruction.opCodeParam());
        super.executeNextInstruction();
    }

    public double getPercentOfInstructionsCalled() {
        return 100 * executedOpCodes.cardinality() / (double) executedOpCodes.length();
    }

    @Override
    public void execute() throws IOException {
        super.execute();
    }

    public Deque<Object> getStack() {
        return this.stack;
    }

    public Deque<LocalVariableStack> getLocalVariableStack() {
        return this.localVariableStack;
    }
}
