package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.bytecode.ByteCodeWriter;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.data.*;
import org.mufuku.yaoocai.v1.compiler.ast.*;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class Translator {

    private static final String INCOMPATIBLE_TYPE_ERROR_MESSAGE = "Incompatible types";
    private static final String NOT_A_STATEMENT_ERROR_MESSAGE = "Not a statement";

    private final ASTScript script;
    private final OutputStream out;

    private LocalVariableStorage currentLocalVariableStorage;
    private FunctionStorage functionStorage = new FunctionStorage();
    private TypeRegistry typeRegistry;
    private ASTFunction currentFunction;

    private BCConstantPoolBuilder constantPoolBuilder;
    private BCUnit unit;
    private BCCodeBuilder codeBuilder;

    public Translator(ASTScript script, OutputStream out) {
        this.script = script;
        this.out = out;
    }

    public void translate() throws IOException {

        this.constantPoolBuilder = new BCConstantPoolBuilder();
        short unitNameIndex = constantPoolBuilder.getSymbolIndex("main_unit");
        this.unit = new BCUnit(unitNameIndex);
        this.unit.setItems(new ArrayList<>());

        BCUnits units = new BCUnits(Collections.singletonList(unit));

        BCFile bcFile = new BCFile();
        bcFile.setPreamble(InstructionSet.PREAMBLE);
        bcFile.setMajorVersion(script.getMajorVersion());
        bcFile.setMinorVersion(script.getMinorVersion());
        bcFile.setConstantPool(this.constantPoolBuilder.build());
        bcFile.setUnits(units);

        preFillStorage();
        emitBody();

        ByteCodeWriter byteCodeWriter = new ByteCodeWriter(out);
        byteCodeWriter.writeByteCode(bcFile);
    }

    private void preFillStorage() {
        for (ASTBasicFunction declaredFunction : script.declaredFunctions()) {
            if (declaredFunction instanceof ASTFunction) {
                functionStorage.addFunction((ASTFunction) declaredFunction);
            } else if (declaredFunction instanceof ASTBuiltinFunction) {
                functionStorage.addBuiltinFunction((ASTBuiltinFunction) declaredFunction);
            }
        }
    }

    private void emitBody() throws IOException {
        for (ASTBasicFunction basicFunction : script.declaredFunctions()) {
            if (basicFunction instanceof ASTFunction) {
                this.currentLocalVariableStorage = new LocalVariableStorage();
                this.typeRegistry = new TypeRegistry(currentLocalVariableStorage, functionStorage);
                this.currentFunction = (ASTFunction) basicFunction;
                emitFunction(currentFunction);
            }
        }
    }

    private void emitFunction(ASTFunction function) throws IOException {
        this.codeBuilder = new BCCodeBuilder();
        populateParametersOnLocalVariableStorage(function.getParameters());
        emitCode(function.getBlock());
        if (function.getReturnType() == null) {
            writeOpCode(InstructionSet.OpCodes.RETURN);
        } else {
            if (branchDoesNotReturn(function.getBlock())) {
                throw new ParsingException("Function does not return properly");
            }
        }

        short functionNameIndex = constantPoolBuilder.getSymbolIndex(function.getIdentifier());

        BCUnitItemFunction unitItemFunction = new BCUnitItemFunction(functionNameIndex);
        unitItemFunction.setCode(codeBuilder.build());
        unitItemFunction.setParameters(convertParametersMetadata(function.getParameters()));
        unitItemFunction.setReturnType(convertType(function.getReturnType()));
        unitItemFunction.setLocalVariableTable(convertLocalVariableTable());

        this.unit.getItems().add(unitItemFunction);
    }

    private BCParameters convertParametersMetadata(ASTParameters parameters) {
        List<BCNameAndType> nameAndTypes = new ArrayList<>();
        for (ASTParameter parameter : parameters) {
            BCType type = convertType(parameter.getType());
            short parameterNameIndex = constantPoolBuilder.getSymbolIndex(parameter.getIdentifier());
            BCNameAndType nameAndType = new BCNameAndType(type, parameterNameIndex);
            nameAndTypes.add(nameAndType);
        }
        return new BCParameters(nameAndTypes);
    }

    private BCLocalVariableTable convertLocalVariableTable() {
        List<BCNameAndType> nameAndTypes = new ArrayList<>();
        for (Map.Entry<String, LocalVariable> localVariableEntry : currentLocalVariableStorage.getRealLocalVariables()) {

            String variableName = localVariableEntry.getKey();
            LocalVariable localVariable = localVariableEntry.getValue();

            short variableIndex = constantPoolBuilder.getSymbolIndex(variableName);
            BCType type = convertType(localVariable.getType());

            BCNameAndType nameAndType = new BCNameAndType(type, variableIndex);
            nameAndTypes.add(nameAndType);
        }
        return new BCLocalVariableTable(nameAndTypes);
    }

    private BCType convertType(ASTType type) {
        if (type == null) {
            return new BCType(BCTypeType.NO);
        } else if (type.isPrimitive()) {
            return convertPrimitiveType(type);
        } else {
            return convertReferenceType(type);
        }
    }

    private BCType convertPrimitiveType(ASTType type) {
        BCTypeType typeType;
        if (ASTType.BOOLEAN.equals(type)) {
            typeType = BCTypeType.BOOLEAN;
        } else if (ASTType.INTEGER.equals(type)) {
            typeType = BCTypeType.INTEGER;
        } else {
            typeType = BCTypeType.NO;
        }
        return new BCType(typeType);
    }

    private BCType convertReferenceType(ASTType type) {
        BCType bcType = new BCType(BCTypeType.REFERENCE_TYPE);
        short typeNameIndex = constantPoolBuilder.getSymbolIndex(type.getTypeName());
        bcType.setReferenceNameIndex(typeNameIndex);
        return bcType;
    }

    private boolean branchDoesNotReturn(ASTBlock block) {
        if (block.isEmpty()) {
            return true;
        } else {
            ASTStatement lastStatement = block.getLastStatement();
            if (lastStatement instanceof ASTReturnStatement) {
                return false;
            } else if (lastStatement instanceof ASTIfStatement) {
                List<ASTBaseIfStatement> ifStatements = ((ASTIfStatement) lastStatement).getStatements();
                for (ASTBaseIfStatement ifStatement : ifStatements) {
                    if (branchDoesNotReturn(ifStatement.getBlock())) {
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        }
    }


    private void populateParametersOnLocalVariableStorage(ASTParameters parameters) {
        for (ASTParameter parameter : parameters) {
            String variableName = parameter.getIdentifier();
            currentLocalVariableStorage.addVariable(variableName, parameter.getType());
            currentLocalVariableStorage.markInitialized(variableName);
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
        byte blockSize = (byte) ((byte) 3 + calculateInstructionSize(statement.getBlock()));
        // jump back = negative size for block + condition
        byte jumpBackSize = (byte) (-calculateExpressionSize(statement.getConditionExpression()) - blockSize);
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

                byte jumpSize = ifJumpTable.getIfJumpOffset(i);
                writeOpCode(InstructionSet.OpCodes.IF, jumpSize);

                emitCode(ifStatement.getBlock());

                byte endJumpSize = ifJumpTable.getEndJumpOffset(i);
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

            byte blockSize = 0;
            if (last) { // when last
                blockSize += 2;
            }
            blockSize += calculateInstructionSize(ifStatement.getBlock());

            byte expressionSize = 0;
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
        String variableName = localVariableDeclarationStatement.getIdentifier();
        byte index = currentLocalVariableStorage.addVariable(variableName, localVariableDeclarationStatement.getType());
        if (localVariableDeclarationStatement.getInitializationExpression() != null) {
            validateInitializationExpression(localVariableDeclarationStatement);
            emitExpression(localVariableDeclarationStatement.getInitializationExpression());
            writeOpCode(InstructionSet.OpCodes.STORE, index);
            currentLocalVariableStorage.markInitialized(variableName);
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
        currentLocalVariableStorage.markInitialized(variableExpression.getIdentifier());
        byte variableIndex = currentLocalVariableStorage.getVariableIndex(variableExpression.getIdentifier());
        writeOpCode(InstructionSet.OpCodes.STORE, variableIndex);
    }

    private void emitArithmeticAssignment(ASTBinaryExpression expression) throws IOException {
        validateLeftRightCompatibility(expression);
        ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getLeft();
        emitVariable(variableExpression);
        emitExpression(expression.getRight());
        if (expression.getOperator() == ASTOperator.ADDITION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.I_ADD);
        } else if (expression.getOperator() == ASTOperator.SUBTRACTION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.I_SUB);
        } else if (expression.getOperator() == ASTOperator.MULTIPLICATION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.I_MUL);
        } else if (expression.getOperator() == ASTOperator.DIVISION_ASSIGNMENT) {
            writeOpCode(InstructionSet.OpCodes.I_DIV);
        }
        writeOpCode(InstructionSet.OpCodes.STORE, currentLocalVariableStorage.getVariableIndex(variableExpression.getIdentifier()));
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
        byte[] jumpTable = new byte[numExpression - 1];
        for (int i = 0; i < numExpression - 1; i++) {
            byte jumpOffset = 1;
            for (int j = i + 1; j < numExpression; j++) {
                // last if has 5 instructions overhead, others 6
                byte overhead = j < numExpression - 1 ? (byte) 5 : (byte) 6;
                jumpOffset += overhead + calculateExpressionSize(conditionExpressions.get(j));
            }
            jumpTable[i] = jumpOffset;
        }

        for (int i = 0; i < numExpression; i++) {
            ASTExpression conditionExpression = conditionExpressions.get(i);

            validateConditionalExpression(conditionExpression);
            emitExpression(conditionExpression);
            writeOpCode(InstructionSet.OpCodes.IF, (byte) 4);
            writeOpCode(InstructionSet.OpCodes.B_CONST_TRUE);

            if (i < numExpression - 1) {
                writeOpCode(InstructionSet.OpCodes.GOTO, jumpTable[i]);
            } else {
                writeOpCode(InstructionSet.OpCodes.GOTO, (byte) 2);
                writeOpCode(InstructionSet.OpCodes.B_CONST_FALSE);
            }
        }
    }

    private void emitConditionalAndExpression(ASTBinaryExpression expression) throws IOException {
        List<ASTExpression> conditionExpressions = new ArrayList<>();
        flattenConditions(expression, conditionExpressions, ASTOperator.CONDITIONAL_AND);

        int numExpression = conditionExpressions.size();
        byte[] jumpTable = new byte[numExpression - 1];
        for (int i = 0; i < numExpression - 1; i++) {
            byte jumpOffset = 1;
            for (int j = i + 1; j < numExpression; j++) {
                // last if has 6 instructions overhead, others 2
                byte overhead = j < numExpression - 1 ? (byte) 2 : (byte) 5;
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

        writeOpCode(InstructionSet.OpCodes.IF, (byte) 4);
        writeOpCode(InstructionSet.OpCodes.B_CONST_TRUE);
        writeOpCode(InstructionSet.OpCodes.GOTO, (byte) 2);
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
            writeOpCode(InstructionSet.OpCodes.I_ADD);
        } else if (expression.getOperator() == ASTOperator.SUBTRACTION) {
            writeOpCode(InstructionSet.OpCodes.I_SUB);
        } else if (expression.getOperator() == ASTOperator.MULTIPLICATION) {
            writeOpCode(InstructionSet.OpCodes.I_MUL);
        } else if (expression.getOperator() == ASTOperator.DIVISION) {
            writeOpCode(InstructionSet.OpCodes.I_DIV);
        } else if (expression.getOperator() == ASTOperator.MODULO) {
            writeOpCode(InstructionSet.OpCodes.I_MOD);
        } else if (expression.getOperator() == ASTOperator.EQUAL) {
            writeOpCode(InstructionSet.OpCodes.I_CMP_EQ);
        } else if (expression.getOperator() == ASTOperator.NOT_EQUAL) {
            writeOpCode(InstructionSet.OpCodes.I_CMP_NE);
        } else if (expression.getOperator() == ASTOperator.LESS_THAN) {
            writeOpCode(InstructionSet.OpCodes.I_CMP_LT);
        } else if (expression.getOperator() == ASTOperator.LESS_THAN_OR_EQUAL) {
            writeOpCode(InstructionSet.OpCodes.I_CMP_LTE);
        } else if (expression.getOperator() == ASTOperator.GREATER_THAN) {
            writeOpCode(InstructionSet.OpCodes.I_CMP_GT);
        } else if (expression.getOperator() == ASTOperator.GREATER_THAN_OR_EQUAL) {
            writeOpCode(InstructionSet.OpCodes.I_CMP_GTE);
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
            writeOpCode(InstructionSet.OpCodes.I_CONST_1);
            if (expression.getUnaryOperator() == ASTUnaryOperator.PRE_INCREMENT) {
                writeOpCode(InstructionSet.OpCodes.I_ADD);
            } else if (expression.getUnaryOperator() == ASTUnaryOperator.PRE_DECREMENT) {
                writeOpCode(InstructionSet.OpCodes.I_SUB);
            }
            writeOpCode(InstructionSet.OpCodes.STORE, currentLocalVariableStorage.getVariableIndex(variableExpression.getIdentifier()));
            emitVariable(variableExpression);
        } else if (expression.getUnaryOperator() == ASTUnaryOperator.POST_INCREMENT ||
                expression.getUnaryOperator() == ASTUnaryOperator.POST_DECREMENT) {
            validateNumericExpression(expression.getSubExpression());
            ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getSubExpression();
            emitVariable(variableExpression);
            emitVariable(variableExpression);
            writeOpCode(InstructionSet.OpCodes.I_CONST_1);
            if (expression.getUnaryOperator() == ASTUnaryOperator.POST_INCREMENT) {
                writeOpCode(InstructionSet.OpCodes.I_ADD);
            } else if (expression.getUnaryOperator() == ASTUnaryOperator.POST_DECREMENT) {
                writeOpCode(InstructionSet.OpCodes.I_SUB);
            }
            writeOpCode(InstructionSet.OpCodes.STORE, currentLocalVariableStorage.getVariableIndex(variableExpression.getIdentifier()));

        } else if (expression.getUnaryOperator() == ASTUnaryOperator.NEGATE) {
            validateNumericExpression(expression.getSubExpression());
            emitExpression(expression.getSubExpression());
            writeOpCode(InstructionSet.OpCodes.I_NEG);
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

    private void emitLiteral(ASTLiteralExpression expression) {
        if (expression.getValue() instanceof Boolean) {
            Boolean value = (Boolean) expression.getValue();
            if (value) {
                writeOpCode(InstructionSet.OpCodes.B_CONST_TRUE);
            } else {
                writeOpCode(InstructionSet.OpCodes.B_CONST_FALSE);
            }
        } else if (expression.getValue() instanceof Integer) {
            emitIntegerLiteral(expression);
        }
    }

    private void emitIntegerLiteral(ASTLiteralExpression expression) {
        int value = (int) expression.getValue();

        if (value == 0) {
            writeOpCode(InstructionSet.OpCodes.I_CONST_0);
        } else if (value == 1) {
            writeOpCode(InstructionSet.OpCodes.I_CONST_1);
        } else {
            short largeIntegerIndex = constantPoolBuilder.getIntegerIndex(value);
            if (largeIntegerIndex <= 0xFF) {
                byte smallIntegerIndex = (byte) largeIntegerIndex;
                writeOpCode(InstructionSet.OpCodes.CONST_P1B, smallIntegerIndex);
            } else {
                writeOpCodeTwoBytesParameter(InstructionSet.OpCodes.CONST_P2B, largeIntegerIndex);
            }
        }
    }

    private void emitFunctionCall(ASTFunctionCallExpression expression) throws IOException {
        String functionName = expression.getFunctionName();
        ASTBasicFunction basicFunction = functionStorage.resolveFunction(functionName);
        if (basicFunction == null) {
            throw new ParsingException("Invalid function " + functionName);
        }

        if (expression.getArguments() != null) {
            emitFunctionArguments(expression, basicFunction);
        }

        if (basicFunction instanceof ASTBuiltinFunction) {
            ASTBuiltinFunction builtinFunction = (ASTBuiltinFunction) basicFunction;
            short builtinFunctionIndex = constantPoolBuilder.getSymbolIndex(builtinFunction.getBindName());
            writeOpCodeTwoBytesParameter(InstructionSet.OpCodes.INVOKE_BUILTIN, builtinFunctionIndex);
        } else if (basicFunction instanceof ASTFunction) {
            ASTFunction functionCall = (ASTFunction) basicFunction;
            short functionIndex = constantPoolBuilder.getSymbolIndex(functionCall.getIdentifier());
            writeOpCodeTwoBytesParameter(InstructionSet.OpCodes.INVOKE, functionIndex);
        }
    }

    private void emitFunctionArguments(ASTFunctionCallExpression expression, ASTBasicFunction basicFunction) throws IOException {
        String functionName = expression.getFunctionName();

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

    private byte calculateExpressionSize(ASTExpression expression) throws IOException {
        ByteArrayOutputStream subOut = new ByteArrayOutputStream();
        Translator temp = setUpSubTranslator(subOut);
        temp.emitExpression(expression);
        return temp.codeBuilder.getSize();
    }

    private byte calculateInstructionSize(ASTBlock block) throws IOException {
        ByteArrayOutputStream subOut = new ByteArrayOutputStream();
        Translator temp = setUpSubTranslator(subOut);
        temp.emitCode(block);
        return temp.codeBuilder.getSize();
    }

    private Translator setUpSubTranslator(ByteArrayOutputStream subOut) {
        Translator temp = new Translator(script, subOut);
        temp.currentLocalVariableStorage = currentLocalVariableStorage;
        temp.functionStorage = this.functionStorage;
        temp.typeRegistry = this.typeRegistry;
        temp.currentFunction = this.currentFunction;
        temp.codeBuilder = new BCCodeBuilder();
        temp.constantPoolBuilder = this.constantPoolBuilder;
        return temp;
    }

    private void emitVariable(ASTVariableExpression variable) {
        byte variableIndex = currentLocalVariableStorage.getVariableIndex(variable.getIdentifier());
        writeOpCode(InstructionSet.OpCodes.LOAD, variableIndex);
    }

    private void writeOpCode(InstructionSet.OpCodes opCode) {
        this.codeBuilder.writeOpCode(opCode);
    }

    private void writeOpCode(InstructionSet.OpCodes opCode, byte parameter) {
        this.codeBuilder.writeOpCode(opCode, parameter);
    }

    private void writeOpCodeTwoBytesParameter(InstructionSet.OpCodes opCode, short parameter) {
        byte index1 = (byte) ((parameter >>> 8) & 0xFF);
        byte index2 = (byte) (parameter & 0xFF);
        this.codeBuilder.writeOpCode(opCode, index1, index2);
    }
}
