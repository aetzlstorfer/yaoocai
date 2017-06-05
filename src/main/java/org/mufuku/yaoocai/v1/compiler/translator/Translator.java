package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.bytecode.BasicByteCodeProducer;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.compiler.ast.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class Translator extends BasicByteCodeProducer {

    private final ASTScript script;

    private LocalVariableStorage currentLocalVariableStorage;
    private FunctionStorage functionStorage = new FunctionStorage();

    public Translator(ASTScript script, OutputStream out) {
        super(out);
        this.script = script;
    }

    public void translate() throws IOException {
        preFillStorage();
        Short mainIndex = functionStorage.getFunctionIndex("main");
        emitHeader(InstructionSet.PREAMBLE, script.getMajorVersion(), script.getMinorVersion(), mainIndex);
        emitBody();
    }

    private void preFillStorage() {
        Iterator<ASTFunction> functionsIt = script.functions();
        while (functionsIt.hasNext()) {
            functionStorage.addFunction(functionsIt.next().getIdentifier());
        }
        Iterator<ASTBuiltinFunction> builtinFunctionsIt = script.builtInFunctions();
        while (builtinFunctionsIt.hasNext()) {
            functionStorage.addBuiltinFunction(builtinFunctionsIt.next());
        }
    }

    private void emitBody() throws IOException {
        Iterator<ASTFunction> functionsIt = script.functions();
        while (functionsIt.hasNext()) {
            this.currentLocalVariableStorage = new LocalVariableStorage();
            emitFunction(functionsIt.next());
        }
    }

    private void emitFunction(ASTFunction function) throws IOException {
        writeOpCode(InstructionSet.OpCodes.FUNCTION);
        populateParametersOnLocalVariableStorage(function.getParameters());
        takeOverParams(function.getParameters());
        emitCode(function.getBlock());
        if (function.getReturnType() == null) {
            writeOpCode(InstructionSet.OpCodes.RETURN);
        }
    }

    private void populateParametersOnLocalVariableStorage(ASTParameters parameters) {
        for (ASTParameter parameter : parameters) {
            currentLocalVariableStorage.addVariable(parameter.getIdentifier(), parameter.getType());
        }
    }

    private void takeOverParams(ASTParameters parameters) throws IOException {
        if (parameters.getParameterSize() > 0) {
            writeOpCode(InstructionSet.OpCodes.POP_PARAMS, (short) parameters.getParameterSize());
        }
    }

    private void emitCode(ASTBlock block) throws IOException {
        for (ASTStatement statement : block) {
            if (statement instanceof ASTLocalVariableDeclarationStatement) {
                emitLocalVariable((ASTLocalVariableDeclarationStatement) statement);
            } else if (statement instanceof ASTReturnStatement) {
                emitReturnStatement((ASTReturnStatement) statement);
            } else if (statement instanceof ASTIfStatement) {
                emitIfStatement((ASTIfStatement) statement);
            } else if (statement instanceof ASTExpressionStatement) {
                emitExpressionStatement((ASTExpressionStatement) statement);
            } else if (statement instanceof ASTWhileStatement) {
                emitWhileStatement((ASTWhileStatement) statement);
            }
            else if (statement instanceof ASTBlock)
            {
                emitCode((ASTBlock) statement);
            }
        }
    }

    private void emitExpressionStatement(ASTExpressionStatement statement) throws IOException {
        emitExpression(statement.getExpression());
    }

    private void emitWhileStatement(ASTWhileStatement statement) throws IOException {
        // +3 = +1(if) +1(if address) +1 to be ahead of last instruction
        short blockSize = (short) ((short) 3 + calculateInstructionSize(statement.getBlock()));
        // jump back = negative size for block + condition
        short jumpBackSize = (short) (-calculateExpressionSize(statement.getConditionExpression()) - blockSize);
        emitExpression(statement.getConditionExpression());
        writeOpCode(InstructionSet.OpCodes.IF, blockSize);
        emitCode(statement.getBlock());
        writeOpCode(InstructionSet.OpCodes.GOTO, jumpBackSize);
    }

    private void emitIfStatement(ASTIfStatement statement) throws IOException {
        List<ASTBaseIfStatement> ifStatements = statement.getStatements();

        IfJumpTable ifJumpTable = calculateIfJumpTable(ifStatements);

        for (int i = 0; i < ifStatements.size(); i++) {
            ASTBaseIfStatement ifStatement = ifStatements.get(i);

            if (ifStatement.getConditionExpression() != null)
            {
                emitExpression(ifStatement.getConditionExpression());

                short jumpSize = ifJumpTable.getIfJumpOffset(i);
                writeOpCode(InstructionSet.OpCodes.IF, jumpSize);

                emitCode(ifStatement.getBlock());

                short endJumpSize = ifJumpTable.getEndJumpOffset(i);
                if (endJumpSize > 1)
                {
                    writeOpCode(InstructionSet.OpCodes.GOTO, endJumpSize);
                }
            }
            else
            { // write else block
                emitCode(ifStatement.getBlock());
            }

        }
    }

    private IfJumpTable calculateIfJumpTable(List<ASTBaseIfStatement> ifStatements) throws IOException
    {
        IfJumpTable ifJumpTable = new IfJumpTable();
        for (int i = 0; i < ifStatements.size(); i++)
        {
            ASTBaseIfStatement ifStatement = ifStatements.get(i);

            boolean last = i < ifStatements.size() - 1;

            short blockSize = 0;
            if (last) { // when last
                blockSize += 2;
            }
            blockSize += calculateInstructionSize(ifStatement.getBlock());

            short expressionSize = 0;
            if (last) {
                expressionSize += 2;
            }
            if (ifStatement.getConditionExpression() != null) {
                expressionSize += calculateExpressionSize(ifStatement.getConditionExpression());
            }
            ifJumpTable.addEntry(blockSize, expressionSize);
        }
        return ifJumpTable;
    }

    private void emitReturnStatement(ASTReturnStatement returnStatement) throws IOException {
        emitExpression(returnStatement.getExpression());
        writeOpCode(InstructionSet.OpCodes.RETURN);
    }

    private void emitLocalVariable(ASTLocalVariableDeclarationStatement localVariableDeclarationStatement) throws IOException {
        short index = currentLocalVariableStorage.addVariable(localVariableDeclarationStatement.getIdentifier(), localVariableDeclarationStatement.getType());
        if (localVariableDeclarationStatement.getInitializationExpression() != null) {
            emitExpression(localVariableDeclarationStatement.getInitializationExpression());
            writeOpCode(InstructionSet.OpCodes.STORE, index);
        }
    }

    private void emitExpression(ASTExpression expression) throws IOException {
        if (expression instanceof ASTLiteralExpression) {
            emitLiteral((ASTLiteralExpression) expression);
        } else if (expression instanceof ASTFunctionCallExpression) {
            emitFunctionCall((ASTFunctionCallExpression) expression);
        } else if (expression instanceof ASTVariableExpression) {
            emitVariable((ASTVariableExpression) expression);
        } else if (expression instanceof ASTBinaryExpression) {
            emitBinaryExpression((ASTBinaryExpression) expression);
        } else if (expression instanceof ASTUnaryExpression) {
            emitUnaryExpression((ASTUnaryExpression) expression);
        }
    }

    private void emitBinaryExpression(ASTBinaryExpression expression) throws IOException {
        if (expression.getOperator() == ASTOperator.ASSIGNMENT) {
            emitVariableAssignment(expression);
        } else if (expression.getOperator() == ASTOperator.ADDITION_ASSIGNMENT ||
                expression.getOperator() == ASTOperator.SUBTRACTION_ASSIGNMENT ||
                expression.getOperator() == ASTOperator.MULTIPLICATION_ASSIGNMENT ||
                expression.getOperator() == ASTOperator.DIVISION_ASSIGNMENT
                ) {
            emitArithmeticAssignment(expression);
        }
        else if (expression.getOperator() == ASTOperator.CONDITIONAL_OR)
        {
            emitConditionalOrExpression(expression);
        }
        else if (expression.getOperator() == ASTOperator.CONDITIONAL_AND)
        {
            emitConditionalAndExpression(expression);
        }
        else
        {
            emitSimpleBinaryExpression(expression);
        }
    }

    private void emitVariableAssignment(ASTBinaryExpression expression) throws IOException
    {
        ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getLeft();
        emitExpression(expression.getRight());
        short variableIndex = currentLocalVariableStorage.getVariableIndex(variableExpression.getVariableName());
        writeOpCode(InstructionSet.OpCodes.STORE, variableIndex);
    }

    private void emitArithmeticAssignment(ASTBinaryExpression expression) throws IOException
    {
        ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getLeft();
        emitVariable(variableExpression);
        emitExpression(expression.getRight());
        if (expression.getOperator() == ASTOperator.ADDITION_ASSIGNMENT)
        {
            writeOpCode(InstructionSet.OpCodes.ADD);
        }
        else if (expression.getOperator() == ASTOperator.SUBTRACTION_ASSIGNMENT)
        {
            writeOpCode(InstructionSet.OpCodes.SUB);
        }
        else if (expression.getOperator() == ASTOperator.MULTIPLICATION_ASSIGNMENT)
        {
            writeOpCode(InstructionSet.OpCodes.MUL);
        }
        else if (expression.getOperator() == ASTOperator.DIVISION_ASSIGNMENT)
        {
            writeOpCode(InstructionSet.OpCodes.DIV);
        }
        writeOpCode(InstructionSet.OpCodes.STORE, currentLocalVariableStorage.getVariableIndex(variableExpression.getVariableName()));
    }

    private void emitConditionalOrExpression(ASTBinaryExpression expression) throws IOException {
        List<ASTExpression> conditionExpressions = new ArrayList<>();
        flattenConditions(expression, conditionExpressions, ASTOperator.CONDITIONAL_OR);

        int numExpression = conditionExpressions.size();
        short[] jumpTable = new short[numExpression - 1];
        for (int i = 0; i < numExpression - 1; i++) {
            short jumpOffset = 1;
            for (int j = i + 1; j < numExpression; j++) {
                // last if has 5 instructions overhead, others 6
                short overhead = j < numExpression - 1 ? (short) 5 : (short) 6;
                jumpOffset += overhead + calculateExpressionSize(conditionExpressions.get(j));
            }
            jumpTable[i] = jumpOffset;
        }

        for (int i = 0; i < numExpression; i++) {
            ASTExpression conditionExpression = conditionExpressions.get(i);

            emitExpression(conditionExpression);
            writeOpCode(InstructionSet.OpCodes.IF, (short) 4);
            writeOpCode(InstructionSet.OpCodes.B_CONST_TRUE);

            if (i < numExpression - 1) {
                writeOpCode(InstructionSet.OpCodes.GOTO, jumpTable[i]);
            } else {
                writeOpCode(InstructionSet.OpCodes.GOTO, (short) 2);
                writeOpCode(InstructionSet.OpCodes.B_CONST_FALSE);
            }
        }
    }

    private void emitConditionalAndExpression(ASTBinaryExpression expression) throws IOException {
        List<ASTExpression> conditionExpressions = new ArrayList<>();
        flattenConditions(expression, conditionExpressions, ASTOperator.CONDITIONAL_AND);

        int numExpression = conditionExpressions.size();
        short[] jumpTable = new short[numExpression - 1];
        for (int i = 0; i < numExpression - 1; i++) {
            short jumpOffset = 1;
            for (int j = i + 1; j < numExpression; j++) {
                // last if has 6 instructions overhead, others 2
                short overhead = j < numExpression - 1 ? (short) 2 : (short) 5;
                jumpOffset += overhead + calculateExpressionSize(conditionExpressions.get(j));
            }
            jumpTable[i] = jumpOffset;
        }

        for (int i = 0; i < numExpression; i++) {
            ASTExpression conditionExpression = conditionExpressions.get(i);
            emitExpression(conditionExpression);
            if (i < numExpression - 1) { // not last
                writeOpCode(InstructionSet.OpCodes.IF, jumpTable[i]);
            }
        }

        writeOpCode(InstructionSet.OpCodes.IF, (short) 4);
        writeOpCode(InstructionSet.OpCodes.B_CONST_TRUE);
        writeOpCode(InstructionSet.OpCodes.GOTO, (short) 2);
        writeOpCode(InstructionSet.OpCodes.B_CONST_FALSE);
    }

    private void flattenConditions(ASTBinaryExpression expression, List<ASTExpression> expressions, ASTOperator operator) {
        if (expression.getLeft() instanceof ASTBinaryExpression &&
                ((ASTBinaryExpression) expression.getLeft()).getOperator() == operator
                ) {
            // go down tree on each left node which is a conditional or
            flattenConditions((ASTBinaryExpression) expression.getLeft(), expressions, operator);
            expressions.add(expression.getRight());
        } else {
            expressions.add(expression.getLeft());
            expressions.add(expression.getRight());
        }
    }

    private void emitSimpleBinaryExpression(ASTBinaryExpression expression) throws IOException
    {
        emitExpression(expression.getLeft());
        if (expression.getRight() != null)
        {
            emitExpression(expression.getRight());
            if (expression.getOperator() == ASTOperator.ADDITION)
            {
                writeOpCode(InstructionSet.OpCodes.ADD);
            }
            else if (expression.getOperator() == ASTOperator.SUBTRACTION)
            {
                writeOpCode(InstructionSet.OpCodes.SUB);
            }
            else if (expression.getOperator() == ASTOperator.MULTIPLICATION)
            {
                writeOpCode(InstructionSet.OpCodes.MUL);
            }
            else if (expression.getOperator() == ASTOperator.DIVISION)
            {
                writeOpCode(InstructionSet.OpCodes.DIV);
            }
            else if (expression.getOperator() == ASTOperator.MODULO)
            {
                writeOpCode(InstructionSet.OpCodes.MOD);
            }
            else if (expression.getOperator() == ASTOperator.EQUAL)
            {
                writeOpCode(InstructionSet.OpCodes.CMP_EQ);
            }
            else if (expression.getOperator() == ASTOperator.NOT_EQUAL)
            {
                writeOpCode(InstructionSet.OpCodes.CMP_NE);
            }
            else if (expression.getOperator() == ASTOperator.LESS_THAN)
            {
                writeOpCode(InstructionSet.OpCodes.CMP_LT);
            }
            else if (expression.getOperator() == ASTOperator.LESS_THAN_OR_EQUAL)
            {
                writeOpCode(InstructionSet.OpCodes.CMP_LTE);
            }
            else if (expression.getOperator() == ASTOperator.GREATER_THAN)
            {
                writeOpCode(InstructionSet.OpCodes.CMP_GT);
            }
            else if (expression.getOperator() == ASTOperator.GREATER_THAN_OR_EQUAL)
            {
                writeOpCode(InstructionSet.OpCodes.CMP_GTE);
            }
            else if (expression.getOperator() == ASTOperator.BITWISE_AND)
            {
                writeOpCode(InstructionSet.OpCodes.AND);
            }
            else if (expression.getOperator() == ASTOperator.BITWISE_OR)
            {
                writeOpCode(InstructionSet.OpCodes.OR);
            }
        }
    }

    private void emitUnaryExpression(ASTUnaryExpression expression) throws IOException {
        if (expression.getUnaryOperator() == ASTUnaryOperator.PRE_INCREMENT ||
                expression.getUnaryOperator() == ASTUnaryOperator.PRE_DECREMENT) {
            ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getSubExpression();
            emitVariable(variableExpression);
            writeOpCode(InstructionSet.OpCodes.I_CONST, (short) 1);
            if (expression.getUnaryOperator() == ASTUnaryOperator.PRE_INCREMENT) {
                writeOpCode(InstructionSet.OpCodes.ADD);
            } else if (expression.getUnaryOperator() == ASTUnaryOperator.PRE_DECREMENT) {
                writeOpCode(InstructionSet.OpCodes.SUB);
            }
            writeOpCode(InstructionSet.OpCodes.STORE, currentLocalVariableStorage.getVariableIndex(variableExpression.getVariableName()));
            emitVariable(variableExpression);
        } else if (expression.getUnaryOperator() == ASTUnaryOperator.POST_INCREMENT ||
                expression.getUnaryOperator() == ASTUnaryOperator.POST_DECREMENT) {
            ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getSubExpression();
            emitVariable(variableExpression);
            emitVariable(variableExpression);
            writeOpCode(InstructionSet.OpCodes.I_CONST, (short) 1);
            if (expression.getUnaryOperator() == ASTUnaryOperator.POST_INCREMENT) {
                writeOpCode(InstructionSet.OpCodes.ADD);
            } else if (expression.getUnaryOperator() == ASTUnaryOperator.POST_DECREMENT) {
                writeOpCode(InstructionSet.OpCodes.SUB);
            }
            writeOpCode(InstructionSet.OpCodes.STORE, currentLocalVariableStorage.getVariableIndex(variableExpression.getVariableName()));

        } else if (expression.getUnaryOperator() == ASTUnaryOperator.NEGATE) {
            emitExpression(expression.getSubExpression());
            writeOpCode(InstructionSet.OpCodes.NEG);
        } else if (expression.getUnaryOperator() == ASTUnaryOperator.BITWISE_NOT) {
            emitExpression(expression.getSubExpression());
            writeOpCode(InstructionSet.OpCodes.NOT);
        }
    }

    private void emitLiteral(ASTLiteralExpression expression) throws IOException {
        if (expression.getValue() instanceof Boolean) {
            Boolean value = (Boolean) expression.getValue();
            if (value) {
                writeOpCode(InstructionSet.OpCodes.B_CONST_TRUE);
            } else {
                writeOpCode(InstructionSet.OpCodes.B_CONST_FALSE);
            }
        } else if (expression.getValue() instanceof Integer) {
            int value = (int) expression.getValue();
            writeOpCode(InstructionSet.OpCodes.I_CONST, (short) value);
        }
    }

    private void emitFunctionCall(ASTFunctionCallExpression expression) throws IOException {
        if (expression.getArguments() != null) {
            for (ASTExpression argExpression : expression.getArguments()) {
                emitExpression(argExpression);
            }
        }

        String functionName = expression.getFunctionName();
        ASTBuiltinFunction builtinFunction = functionStorage.getBuiltinFunction(functionName);
        Short functionIndex = functionStorage.getFunctionIndex(functionName);

        // TODO check for non unique function resolution

        if (builtinFunction != null) {
            // TODO check for non matching vmfunc
            writeOpCode(InstructionSet.OpCodes.INVOKE_BUILTIN, builtinFunction.getFunctionCode());
        } else if (functionIndex != null) {
            writeOpCode(InstructionSet.OpCodes.INVOKE, functionIndex);
        }
    }

    private short calculateExpressionSize(ASTExpression expression) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Translator temp = new Translator(script, out);
        temp.currentLocalVariableStorage = this.currentLocalVariableStorage;
        temp.functionStorage = this.functionStorage;
        temp.emitExpression(expression);
        return (short) (out.size() / 2); //2bytes -> 1 short
    }

    private short calculateInstructionSize(ASTBlock block) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Translator temp = new Translator(script, out);
        temp.currentLocalVariableStorage = this.currentLocalVariableStorage;
        temp.functionStorage = this.functionStorage;
        temp.emitCode(block);
        return (short) (out.size() / 2); //2bytes -> 1 short
    }

    private void emitVariable(ASTVariableExpression variable) throws IOException {
        short variableIndex = currentLocalVariableStorage.getVariableIndex(variable.getVariableName());
        writeOpCode(InstructionSet.OpCodes.LOAD, variableIndex);
    }
}
