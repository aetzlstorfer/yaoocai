package org.mufuku.yaoocai.v1.vm;

import org.mufuku.yaoocai.v1.bytecode.ByteCodeReader;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.data.BCFile;
import org.mufuku.yaoocai.v1.bytecode.data.BCUnit;
import org.mufuku.yaoocai.v1.bytecode.data.BCUnitItemFunction;
import org.mufuku.yaoocai.v1.vm.builtins.BuiltInVMFunction;
import org.mufuku.yaoocai.v1.vm.builtins.DefaultBuiltIns;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class VM implements VirtualMachine {

    final Deque<Object> stack = new ArrayDeque<>();
    final Deque<CallStackElement> callStack = new ArrayDeque<>();
    final Deque<LocalVariableStack> localVariableStack = new ArrayDeque<>();
    private final Map<String, BuiltInVMFunction> builtIns;
    private final ByteCodeReader byteCodeReader;
    BCFile file;
    private PrintStream out = System.out;   // NOSONAR we want ot use out put stream on purpose at
    // it is the access to the outside world
    private boolean execution = true;

    public VM(InputStream in) {
        this(in, DefaultBuiltIns.STANDARD_BUILT_INS);
    }

    public VM(InputStream in, Map<String, BuiltInVMFunction> builtIns) {
        this.byteCodeReader = new ByteCodeReader(in);
        this.builtIns = builtIns;
    }

    @Override
    public void execute() throws IOException {
        readCode();
        BCUnitItemFunction mainFunction = findFunction("main");
        executeCode(mainFunction);
    }

    protected void readCode() throws IOException {
        this.file = byteCodeReader.readByteCode();
    }

    private BCUnitItemFunction findFunction(String name) {
        BCUnit unit = file.getUnits().getUnits().iterator().next();
        return unit.getItems()
                .stream()
                .filter(f -> f instanceof BCUnitItemFunction)
                .map(f -> (BCUnitItemFunction) f)
                .filter(f -> name.equals(this.file.getConstantPool().getItems().get(f.getFunctionNameIndex()).getValue()))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("Cannot find function: " + name));
    }

    private void executeCode(BCUnitItemFunction function) throws IOException {
        CallStackElement callStackElement = new CallStackElement(function.getCode().getCode());
        this.callStack.push(callStackElement);
        this.localVariableStack.push(new LocalVariableStack());
        pushArguments(function);
        while (execution) {
            executeNextInstruction();
        }
    }

    private void pushArguments(BCUnitItemFunction function) {
        int params = function.getParameters().getNameAndTypes().size();
        List<Object> tempParams = new ArrayList<>();
        for (byte i = 0; i < params; i++) {
            tempParams.add(stack.pop());
        }
        for (byte i = 0; i < params; i++) {
            localVariableStack.peek().setValue(i, tempParams.get(params - i - 1));
        }
    }

    void executeNextInstruction() throws IOException {
        CallStackElement callStackElement = getCurrentCallStackElement();
        byte opCode = callStackElement.getCode();
        InstructionSet.OpCodes instruction = InstructionSet.OpCodes.get(opCode);
        switch (instruction) {
            case B_CONST_TRUE:
                stack.push(true);
                callStackElement.move();
                break;
            case B_CONST_FALSE:
                stack.push(false);
                callStackElement.move();
                break;
            case I_CONST_0:
                stack.push(0);
                callStackElement.move();
                break;
            case I_CONST_1:
                stack.push(1);
                callStackElement.move();
                break;
            case CONST_P1B:
                callStackElement.move();
                byte cpIndex = callStackElement.getCode();
                callStackElement.move();
                Object cpValue = file.getConstantPool().getItems().get(cpIndex).getValue();
                stack.push(cpValue);
                break;
            case STORE:
                performStore();
                break;
            case LOAD:
                performLoad(callStackElement);
                break;
            case POP:
                callStackElement.move();
                stack.pop();
                break;
            case INVOKE:
                performInvoke();
                break;
            case INVOKE_BUILTIN:
                performInvocationBuiltIn();
                break;
            case RETURN:
                localVariableStack.pop();
                callStack.pop();
                execution = !localVariableStack.isEmpty();
                break;
            case I_ADD:
                performIntegerAddition();
                break;
            case I_SUB:
                performIntegerSubtraction();
                break;
            case I_MUL:
                performIntegerMultiplication();
                break;
            case I_DIV:
                performIntegerDivision();
                break;
            case I_MOD:
                performIntegerModulo();
                break;
            case I_NEG:
                Integer val = (Integer) stack.pop();
                stack.push(-val);
                callStackElement.move();
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
                callStackElement.move();
                break;
            case I_CMP_LT:
                performIntegerCompareLessThan();
                break;
            case I_CMP_LTE:
                performIntegerCompareLessThanOrEqual();
                break;
            case I_CMP_GT:
                performIntegerCompareGreaterThan();
                break;
            case I_CMP_GTE:
                performIntegerCompareGreaterThanOrEqual();
                break;
            case I_CMP_EQ:
                performIntegerCompareEqual();
                break;
            case I_CMP_NE:
                performIntegerCompareNotEqual();
                break;
            case IF:
                performIf();
                break;
            case GOTO:
                performGoto();
                break;
        }
    }

    private void performStore() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        callStackElement.move();
        byte variableIndex = callStackElement.getCode();
        Object value = stack.pop();
        localVariableStack.peek().setValue(variableIndex, value);
        callStackElement.move();
    }

    CallStackElement getCurrentCallStackElement() {
        return this.callStack.peek();
    }

    private void performLoad(CallStackElement callStackElement) {
        callStackElement.move();
        byte variableIndex = callStackElement.getCode();
        Object value = localVariableStack.peek().getValue(variableIndex);
        stack.push(value);
        callStackElement.move();
    }

    private void performInvoke() throws IOException {
        String functionName = getFunctionName();
        BCUnitItemFunction function = findFunction(functionName);
        executeCode(function);
    }

    private void performInvocationBuiltIn() {
        String functionName = getFunctionName();
        BuiltInVMFunction builtInVMFunction = builtIns.get(functionName);
        builtInVMFunction.handle(stack, this);
    }

    private String getFunctionName() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        callStackElement.move();
        byte functionIndexHigh = callStackElement.getCode();
        callStackElement.move();
        byte functionIndexLow = callStackElement.getCode();
        callStackElement.move();
        int functionNameIndex = functionIndexHigh << 8 | (functionIndexLow & 0xff);
        return (String) this.file.getConstantPool().getItems().get(functionNameIndex).getValue();
    }

    private void performIntegerAddition() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 + val2);
        callStackElement.move();
    }

    private void performIntegerSubtraction() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 - val2);
        callStackElement.move();
    }

    private void performIntegerMultiplication() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 * val2);
        callStackElement.move();
    }

    private void performIntegerDivision() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 / val2);
        callStackElement.move();
    }

    private void performIntegerModulo() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 % val2);
        callStackElement.move();
    }

    private void performBitwiseAnd() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Boolean v2 = (Boolean) stack.pop();
        Boolean v1 = (Boolean) stack.pop();
        stack.push(v2 & v1); // NOSONAR we want bitwise and on purpose here
        callStackElement.move();
    }

    private void performBitwiseOr() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Boolean v2 = (Boolean) stack.pop();
        Boolean v1 = (Boolean) stack.pop();
        stack.push(v2 | v1); // NOSONAR we want bitwise or on purpose here
        callStackElement.move();
    }

    private void performIntegerCompareLessThan() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 < val2);
        callStackElement.move();
    }

    private void performIntegerCompareLessThanOrEqual() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 <= val2);
        callStackElement.move();
    }

    private void performIntegerCompareGreaterThan() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 > val2);
        callStackElement.move();
    }

    private void performIntegerCompareGreaterThanOrEqual() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(val1 >= val2);
        callStackElement.move();
    }

    private void performIntegerCompareEqual() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(Objects.equals(val1, val2));
        callStackElement.move();
    }

    private void performIntegerCompareNotEqual() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        Integer val2 = (Integer) stack.pop();
        Integer val1 = (Integer) stack.pop();
        stack.push(!Objects.equals(val1, val2));
        callStackElement.move();
    }

    private void performIf() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        callStackElement.move();
        Boolean condition = (Boolean) stack.pop();
        if (condition) {
            callStackElement.move();
        } else {
            byte elseJump = callStackElement.getCode();
            callStackElement.move(elseJump);
        }
    }

    private void performGoto() {
        CallStackElement callStackElement = getCurrentCallStackElement();
        callStackElement.move();
        byte jump = callStackElement.getCode();
        callStackElement.move(jump);
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
