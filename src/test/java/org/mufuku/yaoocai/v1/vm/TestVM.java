package org.mufuku.yaoocai.v1.vm;

import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.data.BCFile;
import org.mufuku.yaoocai.v1.bytecode.data.BCUnit;
import org.mufuku.yaoocai.v1.bytecode.data.BCUnitItem;
import org.mufuku.yaoocai.v1.bytecode.data.BCUnitItemFunction;
import org.mufuku.yaoocai.v1.vm.builtins.BuiltInVMFunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class TestVM extends VM {

    private BitSet executedOpCodes;

    public TestVM(InputStream in, Map<String, BuiltInVMFunction> builtIns) {
        super(in, builtIns);
    }

    @Override
    protected void readCode() throws IOException {
        super.readCode();
        Collection<BCUnitItemFunction> functions = getFunctions(file);
        int totalCodeLength = functions.stream().mapToInt(f -> f.getCode().getCode().length).sum();
        executedOpCodes = new BitSet(totalCodeLength);
    }

    private Collection<BCUnitItemFunction> getFunctions(BCFile file) {
        Collection<BCUnitItemFunction> result = new ArrayList<>();
        for (BCUnit unit : file.getUnits().getUnits()) {
            for (BCUnitItem unitItem : unit.getItems()) {
                if (unitItem instanceof BCUnitItemFunction) {
                    result.add((BCUnitItemFunction) unitItem);
                }
            }
        }
        return result;
    }

    @Override
    protected void executeNextInstruction() throws IOException {
        CallStackElement callStackElement = getCurrentCallStackElement();
        byte opCode = callStackElement.getCode();
        InstructionSet.OpCodes opCodeInstruction = InstructionSet.OpCodes.get(opCode);
        executedOpCodes.set(callStackElement.getCodePointer(), callStackElement.getCodePointer() + 1 + opCodeInstruction.opCodeParam());
        super.executeNextInstruction();
    }

    public double getPercentOfInstructionsCalled() {
        return 100 * executedOpCodes.cardinality() / (double) executedOpCodes.length();
    }

    public Deque<Object> getStack() {
        return this.stack;
    }

    public Deque<LocalVariableStack> getLocalVariableStack() {
        return this.localVariableStack;
    }

    public Deque<CallStackElement> getCallStack() {
        return this.callStack;
    }
}
