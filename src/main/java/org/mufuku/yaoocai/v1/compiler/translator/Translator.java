package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.bytecode.BasicByteCodeProducer;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.compiler.ast.*;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class Translator extends BasicByteCodeProducer {

    private static final String INCOMPATIBLE_TYPE_ERROR_MESSAGE = "Incompatible types";
    private static final String NOT_A_STATEMENT_ERROR_MESSAGE = "Not a statement";

    private final ASTScript script;

    private LocalVariableStorage currentLocalVariableStorage;
    private FunctionStorage functionStorage = new FunctionStorage();
    private TypeRegistry typeRegistry;
    private ASTFunction currentFunction;

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
        for (ASTBasicFunction function : script.declaredFunctions()) {
            if (function instanceof ASTFunction) {
                functionStorage.addFunction((ASTFunction) function);
            } else if (function instanceof ASTBuiltinFunction) {
                functionStorage.addBuiltinFunction((ASTBuiltinFunction) function);
            }
        }
    }

    private void emitBody() throws IOException {
        for (ASTBasicFunction basicFunction : script.declaredFunctions()) {
            if (basicFunction instanceof ASTFunction) {
                this.currentLocalVariableStorage = new LocalVariableStorage();
                this.typeRegistry = new TypeRegistry(currentLocalVariableStorage, functionStorage);
                ASTFunction function = (ASTFunction) basicFunction;
                this.currentFunction = function;
                emitFunction(function);
            }
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
            } else if (statement instanceof ASTBlock) {
                emitCode((ASTBlock) statement);
            }
        }
    }

    private void emitExpressionStatement(ASTExpressionStatement statement) throws IOException {
        validateExpressionStatementExpression(statement.getExpression());
        emitExpression(statement.getExpression());
        if (isExpressionPopNecessary(statement)) {
            writeOpCode(InstructionSet.OpCodes.POP);
        }
    }

    private void validateExpressionStatementExpression(ASTExpression expression) {
        if (expression instanceof ASTBinaryExpression) {
            ASTBinaryExpression binaryExpression = (ASTBinaryExpression) expression;
            if (ASTOperator.ASSIGNMENT_OPERATORS.contains(binaryExpression.getOperator())) {
                if (!(binaryExpression.getLeft() instanceof ASTVariableExpression)) {
                    throw new ParsingException(NOT_A_STATEMENT_ERROR_MESSAGE);
                }
            } else {
                throw new ParsingException(NOT_A_STATEMENT_ERROR_MESSAGE);
            }
        } else if (expression instanceof ASTUnaryExpression) {
            ASTUnaryExpression unaryExpression = (ASTUnaryExpression) expression;
            if (!ASTUnaryOperator.INCREMENT_AND_DECREMENT_OPERATORS.contains(unaryExpression.getUnaryOperator())) {
                throw new ParsingException(NOT_A_STATEMENT_ERROR_MESSAGE);
            }
        } else if (!(expression instanceof ASTFunctionCallExpression)) {
            throw new ParsingException(NOT_A_STATEMENT_ERROR_MESSAGE);
        }
    }

    private boolean isExpressionPopNecessary(ASTExpressionStatement statement) {
        ASTExpression expression = statement.getExpression();
        boolean popNecessary = false;
        if (expression instanceof ASTUnaryExpression) {
            ASTUnaryExpression unaryExpression = (ASTUnaryExpression) expression;
            popNecessary = EnumSet.of(
                    ASTUnaryOperator.PRE_INCREMENT,
                    ASTUnaryOperator.PRE_DECREMENT,
                    ASTUnaryOperator.POST_INCREMENT,
                    ASTUnaryOperator.POST_DECREMENT).contains(unaryExpression.getUnaryOperator());
        } else if (expression instanceof ASTFunctionCallExpression) {
            ASTFunctionCallExpression astFunctionCallExpression = (ASTFunctionCallExpression) statement.getExpression();
            ASTType functionReturnType = functionStorage.getFunctionReturnType(astFunctionCallExpression.getFunctionName());
            popNecessary = functionReturnType != null;
        }
        return popNecessary;
    }

    private void emitWhileStatement(ASTWhileStatement statement) throws IOException {
        // +3 = +1(if) +1(if address) +1 to be ahead of last instruction
        short blockSize = (short) ((short) 3 + calculateInstructionSize(statement.getBlock()));
        // jump back = negative size for block + condition
        short jumpBackSize = (short) (-calculateExpressionSize(statement.getConditionExpression()) - blockSize);
        validateConditionalExpression(statement.getConditionExpression());
        emitExpression(statement.getConditionExpression());
        writeOpCode(InstructionSet.OpCodes.IF, blockSize);
        emitCode(statement.getBlock());
        writeOpCode(InstructionSet.OpCodes.GOTO, jumpBackSize);
    }

    private void validateConditionalExpression(ASTExpression expression) {
        ASTType type = typeRegistry.resolveType(expression);
        if (!ASTType.BOOLEAN.equals(type)) {
            throw new ParsingException(INCOMPATIBLE_TYPE_ERROR_MESSAGE);
        }
    }

    private void emitIfStatement(ASTIfStatement statement) throws IOException {
        List<ASTBaseIfStatement> ifStatements = statement.getStatements();

        IfJumpTable ifJumpTable = calculateIfJumpTable(ifStatements);

        for (int i = 0; i < ifStatements.size(); i++) {
            ASTBaseIfStatement ifStatement = ifStatements.get(i);

            if (ifStatement.getConditionExpression() != null) {
                validateConditionalExpression(ifStatement.getConditionExpression());
                emitExpression(ifStatement.getConditionExpression());

                short jumpSize = ifJumpTable.getIfJumpOffset(i);
                writeOpCode(InstructionSet.OpCodes.IF, jumpSize);

                emitCode(ifStatement.getBlock());

                short endJumpSize = ifJumpTable.getEndJumpOffset(i);
                if (endJumpSize > 1) {
                    writeOpCode(InstructionSet.OpCodes.GOTO, endJumpSize);
                }
            } else { // write else block
                emitCode(ifStatement.getBlock());
            }

        }
    }

    private IfJumpTable calculateIfJumpTable(List<ASTBaseIfStatement> ifStatements) throws IOException {
        IfJumpTable ifJumpTable = new IfJumpTable();
        for (int i = 0; i < ifStatements.size(); i++) {
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
        validateReturnStatementExpression(returnStatement.getExpression());
        emitExpression(returnStatement.getExpression());
        writeOpCode(InstructionSet.OpCodes.RETURN);
    }

    private void validateReturnStatementExpression(ASTExpression expression) {
        ASTType type = typeRegistry.resolveType(expression);
        if (!typeRegistry.compatible(type, currentFunction.getReturnType())) {
            throw new ParsingException("Incompatible return type");
        }
    }

    private void emitLocalVariable(ASTLocalVariableDeclarationStatement localVariableDeclarationStatement) throws IOException {
        short index = currentLocalVariableStorage.addVariable(localVariableDeclarationStatement.getIdentifier(), localVariableDeclarationStatement.getType());
        if (localVariableDeclarationStatement.getInitializationExpression() != null) {
            validateInitializationExpression(localVariableDeclarationStatement);
            emitExpression(localVariableDeclarationStatement.getInitializationExpression());
            writeOpCode(InstructionSet.OpCodes.STORE, index);
        }
    }

    private void validateInitializationExpression(ASTLocalVariableDeclarationStatement localVariableDeclarationStatement) {
        ASTType initializationType = typeRegistry.resolveType(localVariableDeclarationStatement.getInitializationExpression());
        ASTType variableType = currentLocalVariableStorage.getVariableType(localVariableDeclarationStatement.getIdentifier());
        if (!typeRegistry.compatible(variableType, initializationType)) {
            throw new ParsingException("Invalid assignment type");
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
        } else if (expression.getOperator() == ASTOperator.CONDITIONAL_OR) {
            emitConditionalOrExpression(expression);
        } else if (expression.getOperator() == ASTOperator.CONDITIONAL_AND) {
            emitConditionalAndExpression(expression);
        } else {
            emitSimpleBinaryExpression(expression);
        }
    }

    private void emitVariableAssignment(ASTBinaryExpression expression) throws IOException {
        validateLeftRightCompatibility(expression);
        ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getLeft();
        emitExpression(expression.getRight());
        short variableIndex = currentLocalVariableStorage.getVariableIndex(variableExpression.getVariableName());
        writeOpCode(InstructionSet.OpCodes.STORE, variableIndex);
    }

    private void emitArithmeticAssignment(ASTBinaryExpression expression) throws IOException {
        validateLeftRightCompatibility(expression);
        ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getLeft();
        emitVariable(variableExpression);
        emitExpression(expression.getRight());
        if (expression.getOperator() == ASTOperator.ADDITION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.ADD);
        } else if (expression.getOperator() == ASTOperator.SUBTRACTION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.SUB);
        } else if (expression.getOperator() == ASTOperator.MULTIPLICATION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.MUL);
        } else if (expression.getOperator() == ASTOperator.DIVISION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.DIV);
        }
        writeOpCode(InstructionSet.OpCodes.STORE, currentLocalVariableStorage.getVariableIndex(variableExpression.getVariableName()));
    }

    private void validateLeftRightCompatibility(ASTBinaryExpression expression) {
        ASTType variableType = typeRegistry.resolveType(expression.getLeft());
        ASTType assignmentType = typeRegistry.resolveType(expression.getRight());
        if (!typeRegistry.compatible(variableType, assignmentType)) {
            throw new ParsingException(INCOMPATIBLE_TYPE_ERROR_MESSAGE);
        }
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

            validateConditionalExpression(conditionExpression);
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
            validateConditionalExpression(conditionExpression);
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

    private void emitSimpleBinaryExpression(ASTBinaryExpression expression) throws IOException {
        emitExpression(expression.getLeft());
        emitExpression(expression.getRight());
        if (expression.getOperator() == ASTOperator.ADDITION) {
            writeOpCode(InstructionSet.OpCodes.ADD);
        } else if (expression.getOperator() == ASTOperator.SUBTRACTION) {
            writeOpCode(InstructionSet.OpCodes.SUB);
        } else if (expression.getOperator() == ASTOperator.MULTIPLICATION) {
            writeOpCode(InstructionSet.OpCodes.MUL);
        } else if (expression.getOperator() == ASTOperator.DIVISION) {
            writeOpCode(InstructionSet.OpCodes.DIV);
        } else if (expression.getOperator() == ASTOperator.MODULO) {
            writeOpCode(InstructionSet.OpCodes.MOD);
        } else if (expression.getOperator() == ASTOperator.EQUAL) {
            writeOpCode(InstructionSet.OpCodes.CMP_EQ);
        } else if (expression.getOperator() == ASTOperator.NOT_EQUAL) {
            writeOpCode(InstructionSet.OpCodes.CMP_NE);
        } else if (expression.getOperator() == ASTOperator.LESS_THAN) {
            writeOpCode(InstructionSet.OpCodes.CMP_LT);
        } else if (expression.getOperator() == ASTOperator.LESS_THAN_OR_EQUAL) {
            writeOpCode(InstructionSet.OpCodes.CMP_LTE);
        } else if (expression.getOperator() == ASTOperator.GREATER_THAN) {
            writeOpCode(InstructionSet.OpCodes.CMP_GT);
        } else if (expression.getOperator() == ASTOperator.GREATER_THAN_OR_EQUAL) {
            writeOpCode(InstructionSet.OpCodes.CMP_GTE);
        } else if (expression.getOperator() == ASTOperator.BITWISE_AND) {
            writeOpCode(InstructionSet.OpCodes.AND);
        } else if (expression.getOperator() == ASTOperator.BITWISE_OR) {
            writeOpCode(InstructionSet.OpCodes.OR);
        }
    }

    private void emitUnaryExpression(ASTUnaryExpression expression) throws IOException {
        if (expression.getUnaryOperator() == ASTUnaryOperator.PRE_INCREMENT ||
                expression.getUnaryOperator() == ASTUnaryOperator.PRE_DECREMENT) {
            validateNumericExpression(expression.getSubExpression());
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
            validateNumericExpression(expression.getSubExpression());
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
            validateNumericExpression(expression.getSubExpression());
            emitExpression(expression.getSubExpression());
            writeOpCode(InstructionSet.OpCodes.NEG);
        } else if (expression.getUnaryOperator() == ASTUnaryOperator.BITWISE_NOT) {
            validateConditionalExpression(expression.getSubExpression());
            emitExpression(expression.getSubExpression());
            writeOpCode(InstructionSet.OpCodes.NOT);
        }
    }

    private void validateNumericExpression(ASTExpression expression) {
        ASTType type = typeRegistry.resolveType(expression);
        if (!ASTType.INTEGER.equals(type)) {
            throw new ParsingException("Expected numeric type");
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
        String functionName = expression.getFunctionName();
        ASTBasicFunction basicFunction = functionStorage.resolveFunction(functionName);
        if (basicFunction == null) {
            throw new ParsingException("Invalid function " + functionName);
        }

        if (expression.getArguments() != null) {
            if (basicFunction.getParameters().getParameterSize() != expression.getArguments().getArgumentsSize()) {
                throw new ParsingException("No function with name " + functionName + " found for compatible types");
            }

            Iterator<ASTParameter> parameterIterator = basicFunction.getParameters().iterator();
            for (ASTExpression argExpression : expression.getArguments()) {
                if (parameterIterator.hasNext()) {
                    ASTType expectedParameterType = parameterIterator.next().getType();
                    ASTType actualParameterType = typeRegistry.resolveType(argExpression);
                    if (typeRegistry.compatible(expectedParameterType, actualParameterType)) {
                        emitExpression(argExpression);
                    } else {
                        throw new ParsingException(INCOMPATIBLE_TYPE_ERROR_MESSAGE);
                    }
                }
            }
        }

        ASTBuiltinFunction builtinFunction = functionStorage.getBuiltinFunction(functionName);
        Short functionIndex = functionStorage.getFunctionIndex(functionName);
        if (builtinFunction != null) {
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
        temp.typeRegistry = this.typeRegistry;
        temp.currentFunction = this.currentFunction;
        temp.emitExpression(expression);
        return (short) (out.size() / 2); //2bytes -> 1 short
    }

    private short calculateInstructionSize(ASTBlock block) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Translator temp = new Translator(script, out);
        temp.currentLocalVariableStorage = this.currentLocalVariableStorage;
        temp.functionStorage = this.functionStorage;
        temp.typeRegistry = this.typeRegistry;
        temp.currentFunction = this.currentFunction;
        temp.emitCode(block);
        return (short) (out.size() / 2); //2bytes -> 1 short
    }

    private void emitVariable(ASTVariableExpression variable) throws IOException {
        short variableIndex = currentLocalVariableStorage.getVariableIndex(variable.getVariableName());
        writeOpCode(InstructionSet.OpCodes.LOAD, variableIndex);
    }
}
