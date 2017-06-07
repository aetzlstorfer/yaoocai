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
public class VM extends BasicByteCodeConsumer implements VirtualMachine {

    final Deque<Object> stack = new ArrayDeque<>();

    final Deque<LocalVariableStack> localVariableStack = new ArrayDeque<>();

    private final Deque<Integer> callStack = new ArrayDeque<>();
    private final List<Integer> functionPointer = new ArrayList<>();
    private final Map<Short, BuiltInVMFunction> builtIns;
    short[] code;
    int codePointer = 0;

    private PrintStream out = System.out;   // NOSONAR we want ot use out put stream on purpose at
    // it is the access to the outside world
    private boolean execution = true;

    public VM(InputStream in) {
        this(in, DefaultBuiltIns.STANDARD_BUILT_INS);
    }

    VM(InputStream in, Map<Short, BuiltInVMFunction> builtIns) {
        super(in, InstructionSet.MAJOR_VERSION, InstructionSet.MINOR_VERSION);
        this.builtIns = builtIns;
    }

    @Override
    public void execute() throws IOException {
        readHeader();
        readCode();
        executeCode();
    }

    void readCode() throws IOException {
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

    private Short storeAndGetNext() throws IOException {
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

    void executeNextInstruction() {

        short opCode = this.code[this.codePointer];
        InstructionSet.OpCodes instruction = InstructionSet.OpCodes.get(opCode);

        switch (instruction) {
            case FUNCTION:
                localVariableStack.push(new LocalVariableStack());
                this.codePointer++;
                break;
            case I_CONST:
                this.codePointer++;
                stack.push(code[codePointer]);
                this.codePointer++;
                break;
            case B_CONST_TRUE:
                stack.push(true);
                this.codePointer++;
                break;
            case B_CONST_FALSE:
                stack.push(false);
                this.codePointer++;
                break;
            case STORE:
                performStore();
                break;
            case LOAD:
                performLoad();
                break;
            case POP:
                this.codePointer++;
                stack.pop();
                break;
            case INVOKE:
                performInvoke();
                break;
            case INVOKE_BUILTIN:
                performInvokationBuiltIn();
                break;
            case RETURN:
                localVariableStack.pop();
                execution = !localVariableStack.isEmpty();
                if (execution) {
                    this.codePointer = callStack.pop();
                }
                break;
            case ADD:
                performAddition();
                break;
            case SUB:
                performSubtraction();
                break;
            case MUL:
                performMultiplication();
                break;
            case DIV:
                performDivision();
                break;
            case MOD:
                performModulo();
                break;
            case NEG:
                Short val = (Short) stack.pop();
                stack.push((short) (-val));
                this.codePointer++;
                break;
            case AND:
                performBitwiseAnd();
                break;
            case OR:
                performBitwiseOr();
                break;
            case NOT:
                Boolean v = (Boolean) stack.pop();
                stack.push(!v);
                this.codePointer++;
                break;
            case CMP_LT:
                performCompareLessThan();
                break;
            case CMP_LTE:
                performCompareLessThanOrEqual();
                break;
            case CMP_GT:
                performCompareGreaterThan();
                break;
            case CMP_GTE:
                performCompareGreaterThanOrEqual();
                break;
            case CMP_EQ:
                performCompareEqual();
                break;
            case CMP_NE:
                performCompareNotEqual();
                break;
            case IF:
                performIf();
                break;
            case GOTO:
                performGoto();
                break;
            case POP_PARAMS:
                performPopParameters();
                break;
        }
    }

    private void performStore() {
        this.codePointer++;
        short variableIndex = code[codePointer];
        Object value = stack.pop();
        localVariableStack.peek().setValue(variableIndex, value);
        this.codePointer++;
    }

    private void performLoad() {
        this.codePointer++;
        short variableIndex = code[codePointer];
        Object value = localVariableStack.peek().getValue(variableIndex);
        stack.push(value);
        this.codePointer++;
    }

    private void performInvoke() {
        this.codePointer++;
        short functionIndex = code[codePointer];
        callStack.push(codePointer + 1);
        this.codePointer = functionPointer.get(functionIndex);
    }

    private void performInvokationBuiltIn() {
        codePointer++;
        short functionIndex = code[codePointer];
        codePointer++;
        BuiltInVMFunction builtInVMFunction = builtIns.get(functionIndex);
        builtInVMFunction.handle(stack, this);
    }

    private void performAddition() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push((short) (val1 + val2));
        this.codePointer++;
    }

    private void performSubtraction() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push((short) (val1 - val2));
        this.codePointer++;
    }

    private void performMultiplication() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push((short) (val1 * val2));
        this.codePointer++;
    }

    private void performDivision() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push((short) (val1 / val2));
        this.codePointer++;
    }

    private void performModulo() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push((short) (val1 % val2));
        this.codePointer++;
    }

    private void performBitwiseAnd() {
        Boolean v2 = (Boolean) stack.pop();
        Boolean v1 = (Boolean) stack.pop();
        stack.push(v2 & v1); // NOSONAR we want bitwise and on purpose here
        this.codePointer++;
    }

    private void performBitwiseOr() {
        Boolean v2 = (Boolean) stack.pop();
        Boolean v1 = (Boolean) stack.pop();
        stack.push(v2 | v1); // NOSONAR we want bitwise or on purpose here
        this.codePointer++;
    }

    private void performCompareLessThan() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push(val1 < val2);
        this.codePointer++;
    }

    private void performCompareLessThanOrEqual() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push(val1 <= val2);
        this.codePointer++;
    }

    private void performCompareGreaterThan() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push(val1 > val2);
        this.codePointer++;
    }

    private void performCompareGreaterThanOrEqual() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push(val1 >= val2);
        this.codePointer++;
    }

    private void performCompareEqual() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push(Objects.equals(val1, val2));
        this.codePointer++;
    }

    private void performCompareNotEqual() {
        Short val2 = (Short) stack.pop();
        Short val1 = (Short) stack.pop();
        stack.push(!Objects.equals(val1, val2));
        this.codePointer++;
    }

    private void performIf() {
        this.codePointer++;
        Boolean condition = (Boolean) stack.pop();
        if (condition) {
            this.codePointer++;
        } else {
            short elseJump = code[codePointer];
            this.codePointer += elseJump;
        }
    }

    private void performGoto() {
        this.codePointer++;
        short jump = code[codePointer];
        this.codePointer += jump;
    }

    private void performPopParameters() {
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

    @Override
    public PrintStream getOut() {
        return out;
    }

    @Override
    public void setOut(PrintStream out) {
        this.out = out;
    }
}
