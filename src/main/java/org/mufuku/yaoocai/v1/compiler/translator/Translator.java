package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.compiler.ast.*;
import sun.security.pkcs.ParsingException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class Translator {

    private final ASTScript script;
    private final DataOutputStream out;

    private LocalVariableStorage currentLocalVariableStorage;
    private FunctionStorage functionStorage = new FunctionStorage();

    public Translator(ASTScript script, OutputStream out) {
        this.script = script;
        this.out = new DataOutputStream(out);
    }

    public void translate() throws IOException {
        preFillStorage();
        emitHeader();
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

    private void emitHeader() throws IOException {
        writeString(InstructionSet.PREAMBLE);
        out.writeShort(script.getMajorVersion());
        out.writeShort(script.getMinorVersion());
        Short mainFunctionIndex = functionStorage.getFunctionIndex("main");
        out.writeShort(mainFunctionIndex);
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
            } else {
                throw new ParsingException("Unsupported block type " + block.getClass());
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

        List<Short> blockSizes = new ArrayList<>();
        List<Short> ifExpressionSizes = new ArrayList<>();

        for (int i = 0; i < ifStatements.size(); i++) {
            ASTBaseIfStatement ifStatement = ifStatements.get(i);

            boolean last = i < ifStatements.size() - 1;

            short blockSize = 0;
            if (last) { // when last
                blockSize += 2;
            }
            blockSize += calculateInstructionSize(ifStatement.getBlock());
            blockSizes.add(blockSize);

            short expressionSize = 0;
            if (last) {
                expressionSize += 2;
            }
            if (ifStatement.getConditionExpression() != null) {
                expressionSize += calculateExpressionSize(ifStatement.getConditionExpression());
            }
            ifExpressionSizes.add(expressionSize);
        }

        for (int i = 0; i < ifStatements.size(); i++) {
            ASTBaseIfStatement ifStatement = ifStatements.get(i);

            if (ifStatement.getConditionExpression() != null) {
                emitExpression(ifStatement.getConditionExpression());

                short jumpSize = 1;
                jumpSize += blockSizes.get(i);
                writeOpCode(InstructionSet.OpCodes.IF, jumpSize);

                emitCode(ifStatement.getBlock());

                short endJumpSize = 1;
                for (int j = i + 1; j < ifStatements.size(); j++) {
                    endJumpSize += blockSizes.get(j);
                    endJumpSize += ifExpressionSizes.get(j);
                }

                if (endJumpSize > 1) {
                    writeOpCode(InstructionSet.OpCodes.GOTO, endJumpSize);
                }
            } else { // write else block
                emitCode(ifStatement.getBlock());
            }

        }
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
        }
    }

    private void emitBinaryExpression(ASTBinaryExpression expression) throws IOException {
        if (expression.getOperator() == ASTOperator.ASSIGNMENT) {
            ASTVariableExpression variableExpression = (ASTVariableExpression) expression.getLeft();
            emitExpression(expression.getRight());
            short variableIndex = currentLocalVariableStorage.getVariableIndex(variableExpression.getVariableName());
            writeOpCode(InstructionSet.OpCodes.STORE, variableIndex);
        } else {
            emitExpression(expression.getLeft());
            if (expression.getRight() != null) {
                emitExpression(expression.getRight());
                if (expression.getOperator() == ASTOperator.ADDITION) {
                    writeOpCode(InstructionSet.OpCodes.ADD);
                } else if (expression.getOperator() == ASTOperator.SUBTRACTION) {
                    writeOpCode(InstructionSet.OpCodes.SUB);
                } else if (expression.getOperator() == ASTOperator.MULTIPLICATION) {
                    writeOpCode(InstructionSet.OpCodes.MUL);
                } else if (expression.getOperator() == ASTOperator.DIVISION) {
                    writeOpCode(InstructionSet.OpCodes.DIV);
                } else if (expression.getOperator() == ASTOperator.LESS_THAN) {
                    writeOpCode(InstructionSet.OpCodes.CMP_LT);
                } else if (expression.getOperator() == ASTOperator.LESS_THAN_OR_EQUAL) {
                    writeOpCode(InstructionSet.OpCodes.CMP_LTE);
                } else if (expression.getOperator() == ASTOperator.GREATER_THAN) {
                    writeOpCode(InstructionSet.OpCodes.CMP_GT);
                } else if (expression.getOperator() == ASTOperator.GREATER_THAN_OR_EQUAL) {
                    writeOpCode(InstructionSet.OpCodes.CMP_GTE);
                } else {
                    throw new RuntimeException("invalid operator: " + expression.getOperator());
                }
            }
        }
    }

    private void emitLiteral(ASTLiteralExpression expression) throws IOException {
        if (expression.getValue() instanceof Boolean) {
            Boolean value = (Boolean) expression.getValue();
            if (value == null) {
                throw new IllegalStateException("Invalid boolean literal");
            } else if (value) {
                writeOpCode(InstructionSet.OpCodes.B_CONST_TRUE);
            } else {
                writeOpCode(InstructionSet.OpCodes.B_CONST_FALSE);
            }
        } else if (expression.getValue() instanceof Integer) {
            int value = (int) expression.getValue();
            writeOpCode(InstructionSet.OpCodes.I_CONST, (short) value);
        } else {
            throw new ParsingException("Unsupported literal expression type: " + expression.getClass());
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

    private void writeOpCode(InstructionSet.OpCodes opCode, short... params) throws IOException {
        out.writeShort(opCode.code());
        if (opCode.opCodeParam() != params.length) {
            throw new IllegalStateException("Invalid number of params");
        }
        for (short param : params) {
            out.writeShort(param);
        }
    }

    private void writeString(String value) throws IOException {
        for (char ch : value.toCharArray()) {
            out.writeChar(ch);
        }
    }
}
