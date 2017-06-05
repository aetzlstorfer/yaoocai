package org.mufuku.yaoocai.v1.vm;

import org.mufuku.yaoocai.v1.bytecode.BasicByteCodeConsumer;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.vm.builtins.BuiltInVMFunction;
import org.mufuku.yaoocai.v1.vm.builtins.DefaultBuiltIns;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class YAOOCAI_VM extends BasicByteCodeConsumer implements VirtualMachine {

    private final Deque<Object> stack = new ArrayDeque<>();

    private final Deque<LocalVariableStack> localVariableStack = new ArrayDeque<>();

    private final Deque<Integer> callStack = new ArrayDeque<>();
    private final List<Integer> functionPointer = new ArrayList<>();
    private final Map<Short, BuiltInVMFunction> builtIns;
    private short[] code;
    private int codePointer = 0;

    private PrintStream out = System.out;
    private boolean execution = true;

    public YAOOCAI_VM(InputStream in) {
        this(in, DefaultBuiltIns.getBuiltIns());
    }

    public YAOOCAI_VM(InputStream in, Map<Short, BuiltInVMFunction> builtIns) {
        super(in, InstructionSet.MAJOR_VERSION, InstructionSet.MINOR_VERSION);
        this.builtIns = builtIns;
    }

    public void execute() throws IOException {
        readHeader();
        readCode();
        executeCode();
    }

    private void readCode() throws IOException {
        this.code = new short[in.available() / 2];
        this.codePointer = 0;
        Short currentOpCode = storeAndGetNext();
        while (currentOpCode != null && currentOpCode == InstructionSet.OpCodes.FUNCTION.code()) {
            functionPointer.add(this.codePointer - 1);
            currentOpCode = storeAndGetNext();
            while (currentOpCode != null && currentOpCode != InstructionSet.OpCodes.FUNCTION.code()) {
                consumeOpCode(currentOpCode);
                currentOpCode = storeAndGetNext();
            }
        }
        this.codePointer = 0;
    }

    private Short storeAndGetNext() throws IOException
    {
        Short currentCode = super.getNext();
        if (currentCode != null) {
            this.code[this.codePointer++] = currentCode;
        }
        return currentCode;
    }

    private void consumeOpCode(short currentOpCode) throws IOException {
        InstructionSet.OpCodes opCode = InstructionSet.OpCodes.get(currentOpCode);
        if (opCode.opCodeParam() > 0) {
            for (int i = 0; i < opCode.opCodeParam(); i++) {
                storeAndGetNext();
            }
        }
    }

    private void executeCode() {
        this.codePointer = this.functionPointer.get(this.mainFunctionIndex);
        while (execution) {
            executeNextInstruction();
        }
    }

    private void executeNextInstruction() {
        short opCode = this.code[this.codePointer];
        if (opCode == InstructionSet.OpCodes.FUNCTION.code()) {
            localVariableStack.push(new LocalVariableStack());
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.I_CONST.code()) {
            this.codePointer++;
            stack.push(code[codePointer]);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.B_CONST_TRUE.code()) {
            stack.push(true);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.B_CONST_FALSE.code()) {
            stack.push(false);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.STORE.code()) {
            this.codePointer++;
            short variableIndex = code[codePointer];
            Object value = stack.pop();
            localVariableStack.peek().setValue(variableIndex, value);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.LOAD.code()) {
            this.codePointer++;
            short variableIndex = code[codePointer];
            Object value = localVariableStack.peek().getValue(variableIndex);
            stack.push(value);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.INVOKE.code()) {
            this.codePointer++;
            short functionIndex = code[codePointer];
            callStack.push(codePointer + 1);
            this.codePointer = functionPointer.get(functionIndex);
        } else if (opCode == InstructionSet.OpCodes.INVOKE_BUILTIN.code()) {
            codePointer++;
            short functionIndex = code[codePointer];
            codePointer++;
            BuiltInVMFunction builtInVMFunction = builtIns.get(functionIndex);
            builtInVMFunction.handle(stack, this);
        } else if (opCode == InstructionSet.OpCodes.RETURN.code()) {
            localVariableStack.pop();
            execution = !localVariableStack.isEmpty();
            if (execution) {
                this.codePointer = callStack.pop();
            }
        } else if (opCode == InstructionSet.OpCodes.ADD.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push((short) (val1 + val2));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.SUB.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push((short) (val1 - val2));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.MUL.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push((short) (val1 * val2));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.DIV.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push((short) (val1 / val2));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.MOD.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push((short) (val1 % val2));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.NEG.code()) {
            Short val = (Short) stack.pop();
            stack.push((short) (-val));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.AND.code()) {
            Boolean v2 = (Boolean) stack.pop();
            Boolean v1 = (Boolean) stack.pop();
            stack.push(v2 & v1);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.OR.code()) {
            Boolean v2 = (Boolean) stack.pop();
            Boolean v1 = (Boolean) stack.pop();
            stack.push(v2 | v1);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.NOT.code()) {
            Boolean v = (Boolean) stack.pop();
            stack.push(!v);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.CMP_LT.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push(val1 < val2);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.CMP_LTE.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push(val1 <= val2);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.CMP_GT.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push(val1 > val2);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.CMP_GTE.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push(val1 >= val2);
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.CMP_EQ.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push(Objects.equals(val1, val2));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.CMP_NE.code()) {
            Short val2 = (Short) stack.pop();
            Short val1 = (Short) stack.pop();
            stack.push(!Objects.equals(val1, val2));
            this.codePointer++;
        } else if (opCode == InstructionSet.OpCodes.IF.code()) {
            this.codePointer++;
            Boolean condition = (Boolean) stack.pop();
            if (condition) {
                this.codePointer++;
            } else {
                short elseJump = code[codePointer];
                this.codePointer += elseJump;
            }
        } else if (opCode == InstructionSet.OpCodes.GOTO.code()) {
            this.codePointer++;
            short jump = code[codePointer];
            this.codePointer += jump;
        } else if (opCode == InstructionSet.OpCodes.POP_PARAMS.code()) {
            this.codePointer++;
            short params = code[codePointer];
            List<Object> tempParams = new ArrayList<>();
            for (short i = 0; i < params; i++) {
                tempParams.add(stack.pop());
            }
            for (short i = 0; i < params; i++) {
                localVariableStack.peek().setValue(i, tempParams.get(params - i - 1));
            }
            this.codePointer++;
        }
    }

    @Override
    public PrintStream getOut()
    {
        return out;
    }

    @Override
    public void setOut(PrintStream out)
    {
        this.out = out;
    }
}
