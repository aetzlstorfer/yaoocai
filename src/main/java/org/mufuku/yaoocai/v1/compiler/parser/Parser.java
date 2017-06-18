package org.mufuku.yaoocai.v1.compiler.parser;

import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.compiler.ast.*;
import org.mufuku.yaoocai.v1.compiler.scanner.Scanner;
import org.mufuku.yaoocai.v1.compiler.scanner.ScannerSymbols;

import java.io.IOException;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class Parser {

    private final Scanner scanner;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    public ASTScript parse() {
        ASTScript script;
        try {
            scanner.initialize();
            script = parseScript();
        } catch (IOException e) {
            throw new ParsingException("Could not compile file correctly. Error: " + e.getMessage(), e);
        }
        if (scanner.getCurrentSymbol() != ScannerSymbols.EOI) {
            throw new ParsingException("unexpected " + scanner.getCurrentSymbol());
        }
        return script;
    }

    private ASTScript parseScript() throws IOException {
        ASTScript script = new ASTScript(InstructionSet.MAJOR_VERSION, InstructionSet.MINOR_VERSION);
        while (scanner.getCurrentSymbol() == ScannerSymbols.BUILTIN) {
            ASTBuiltinFunction builtinFunction = parseBuiltInFunctionDeclaration();
            script.addDeclaredFunction(builtinFunction);
        }
        while (scanner.getCurrentSymbol() == ScannerSymbols.FUNCTION) {
            ASTFunction function = parseFunctionDeclaration();
            script.addDeclaredFunction(function);
        }
        return script;
    }

    private ASTBuiltinFunction parseBuiltInFunctionDeclaration() throws IOException {
        checkAndProceed(ScannerSymbols.BUILTIN);
        checkAndProceed(ScannerSymbols.FUNCTION);

        String functionName = checkIdentifierAndProceed();

        ASTParameters parameters = parseParameters();
        ASTType returnType = null;
        if (checkOptionalAndProceed(ScannerSymbols.COLON)) {
            returnType = parseType();
        }

        checkAndProceed(ScannerSymbols.BUILTIN_ASSIGNMENT);
        String bindName = checkIdentifierAndProceed();

        ASTBuiltinFunction function = new ASTBuiltinFunction(functionName, bindName);
        function.setParameters(parameters);
        function.setReturnType(returnType);
        return function;
    }

    private ASTFunction parseFunctionDeclaration() throws IOException {
        checkAndProceed(ScannerSymbols.FUNCTION);
        String functionName = checkIdentifierAndProceed();

        ASTParameters parameters = parseParameters();
        ASTType returnType = null;
        if (checkOptionalAndProceed(ScannerSymbols.COLON)) {
            returnType = parseType();
        }
        ASTBlock block = parseBlock();
        ASTFunction function = new ASTFunction(functionName);
        function.setParameters(parameters);
        function.setReturnType(returnType);
        function.setBlock(block);
        return function;
    }

    private ASTParameters parseParameters() throws IOException {
        ASTParameters parameters = new ASTParameters();
        checkAndProceed(ScannerSymbols.PAR_START);
        if (scanner.getCurrentSymbol() != ScannerSymbols.PAR_END) {
            ASTParameter parameter = parseParameterDeclaration();
            parameters.addParameter(parameter);
            while (checkOptionalAndProceed(ScannerSymbols.COMMA)) {
                parameters.addParameter(parseParameterDeclaration());
            }
        }
        checkAndProceed(ScannerSymbols.PAR_END);
        return parameters;
    }

    private ASTParameter parseParameterDeclaration() throws IOException {
        String parameterName = checkIdentifierAndProceed();
        checkAndProceed(ScannerSymbols.COLON);
        ASTType parameterType = parseType();
        return new ASTParameter(parameterName, parameterType);
    }

    private ASTBlock parseBlock() throws IOException {
        ASTBlock block = new ASTBlock();
        checkAndProceed(ScannerSymbols.BLOCK_START);
        while (scanner.getCurrentSymbol() != ScannerSymbols.BLOCK_END) {
            block.addStatement(parseBlockStatement());
        }
        checkAndProceed(ScannerSymbols.BLOCK_END);
        return block;
    }

    private ASTStatement parseBlockStatement() throws IOException {
        ASTStatement statement;
        if (scanner.getCurrentSymbol() == ScannerSymbols.VARIABLE) {
            statement = parseLocalVariableDeclarationStatement();
        } else {
            statement = parseStatement();
        }
        return statement;
    }

    private ASTLocalVariableDeclarationStatement parseLocalVariableDeclarationStatement() throws IOException {
        checkAndProceed(ScannerSymbols.VARIABLE);
        String variableName = checkIdentifierAndProceed();
        checkAndProceed(ScannerSymbols.COLON);
        ASTType type = parseType();
        ASTExpression expression = null;
        if (checkOptionalAndProceed(ScannerSymbols.ASSIGNMENT_OPERATOR)) {
            expression = parseExpression();
        }
        checkAndProceed(ScannerSymbols.SEMICOLON);
        ASTLocalVariableDeclarationStatement localVariableDeclarationStatement =
                new ASTLocalVariableDeclarationStatement(variableName, type);
        localVariableDeclarationStatement.setInitializationExpression(expression);
        return localVariableDeclarationStatement;
    }

    private ASTStatement parseStatement() throws IOException {
        ASTStatement statement;
        if (scanner.getCurrentSymbol() == ScannerSymbols.BLOCK_START) {
            statement = parseBlock();
        } else if (scanner.getCurrentSymbol() == ScannerSymbols.IF) {
            statement = parseIfStatement();
        } else if (scanner.getCurrentSymbol() == ScannerSymbols.WHILE) {
            statement = parseWhileStatement();
        } else if (scanner.getCurrentSymbol() == ScannerSymbols.RETURN) {
            statement = parseReturnStatement();
        } else {
            statement = parseExpressionStatement();
        }
        return statement;
    }

    private ASTIfStatement parseIfStatement() throws IOException {
        checkAndProceed(ScannerSymbols.IF);
        ASTExpression expression = parseParExpression();
        ASTBlock block = parseBlock();

        ASTIfStatement ifStatement = new ASTIfStatement(expression, block);

        while (checkOptionalAndProceed(ScannerSymbols.ELSE)) {
            if (checkOptionalAndProceed(ScannerSymbols.IF)) {
                ASTExpression elseIfConditionExpression = parseParExpression();
                ASTBlock elseIfBlock = parseBlock();

                ASTBaseIfStatement elseIfStatement = new ASTBaseIfStatement(elseIfConditionExpression, elseIfBlock);
                ifStatement.addElseIfBlock(elseIfStatement);
            } else {
                ASTBlock elseBlock = parseBlock();
                ASTBaseIfStatement elseStatement = new ASTBaseIfStatement(null, elseBlock);
                ifStatement.setElseBlockStatement(elseStatement);
            }
        }
        return ifStatement;
    }

    private ASTWhileStatement parseWhileStatement() throws IOException {
        checkAndProceed(ScannerSymbols.WHILE);
        ASTExpression conditionalExpression = parseParExpression();
        ASTBlock block = parseBlock();
        return new ASTWhileStatement(conditionalExpression, block);
    }

    private ASTExpression parseParExpression() throws IOException {
        ASTExpression expression;
        checkAndProceed(ScannerSymbols.PAR_START);
        expression = parseExpression();
        checkAndProceed(ScannerSymbols.PAR_END);
        return expression;
    }

    private ASTReturnStatement parseReturnStatement() throws IOException {
        checkAndProceed(ScannerSymbols.RETURN);
        ASTExpression expression = parseExpression();
        checkAndProceed(ScannerSymbols.SEMICOLON);
        return new ASTReturnStatement(expression);
    }

    private ASTExpressionStatement parseExpressionStatement() throws IOException {
        ASTExpression expression = parseExpression();
        checkAndProceed(ScannerSymbols.SEMICOLON);
        return new ASTExpressionStatement(expression);
    }

    private ASTExpression parseExpression() throws IOException {
        return parseAssignmentExpression();
    }

    private ASTExpression parseAssignmentExpression() throws IOException {
        ASTExpression expr = parseConditionalOrExpression();
        ASTOperator operator = null;
        if (checkOptionalAndProceed(ScannerSymbols.ASSIGNMENT_OPERATOR)) {
            operator = ASTOperator.ASSIGNMENT;
        } else if (checkOptionalAndProceed(ScannerSymbols.ADDITION_ASSIGNMENT_OPERATOR)) {
            operator = ASTOperator.ADDITION_ASSIGNMENT;
        } else if (checkOptionalAndProceed(ScannerSymbols.SUBTRACTION_ASSIGNMENT_OPERATOR)) {
            operator = ASTOperator.SUBTRACTION_ASSIGNMENT;
        } else if (checkOptionalAndProceed(ScannerSymbols.MULTIPLICATION_ASSIGNMENT_OPERATOR)) {
            operator = ASTOperator.MULTIPLICATION_ASSIGNMENT;
        } else if (checkOptionalAndProceed(ScannerSymbols.DIVISION_ASSIGNMENT_OPERATOR)) {
            operator = ASTOperator.DIVISION_ASSIGNMENT;
        }

        if (operator != null) {
            expr = getOrCombineExpression(expr, parseConditionalOrExpression(), operator);
        }
        return expr;
    }

    private ASTExpression parseConditionalOrExpression() throws IOException {
        ASTExpression expr = parseConditionalAndExpression();
        while (checkOptionalAndProceed(ScannerSymbols.CONDITIONAL_OR_OPERATOR)) {
            expr = getOrCombineExpression(expr, parseConditionalAndExpression(), ASTOperator.CONDITIONAL_OR);
        }
        return expr;
    }

    private ASTExpression parseConditionalAndExpression() throws IOException {
        ASTExpression expr = parseBitwiseOrExpression();
        while (checkOptionalAndProceed(ScannerSymbols.CONDITIONAL_AND_OPERATOR)) {
            expr = getOrCombineExpression(expr, parseBitwiseOrExpression(), ASTOperator.CONDITIONAL_AND);
        }
        return expr;
    }

    private ASTExpression parseBitwiseOrExpression() throws IOException {
        ASTExpression expr = parseBitwiseAndExpression();
        while (checkOptionalAndProceed(ScannerSymbols.BITWISE_OR_OPERATOR)) {
            expr = getOrCombineExpression(expr, parseBitwiseAndExpression(), ASTOperator.BITWISE_OR);
        }
        return expr;
    }

    private ASTExpression parseBitwiseAndExpression() throws IOException {
        ASTExpression expr = parseComparisonExpression();
        while (checkOptionalAndProceed(ScannerSymbols.BITWISE_AND_OPERATOR)) {
            expr = getOrCombineExpression(expr, parseComparisonExpression(), ASTOperator.BITWISE_AND);
        }
        return expr;
    }

    private ASTExpression parseComparisonExpression() throws IOException {
        ASTExpression expr = parseAdditiveExpression();
        while (
                scanner.getCurrentSymbol() == ScannerSymbols.EQUALITY_OPERATOR ||
                        scanner.getCurrentSymbol() == ScannerSymbols.INEQUALITY_OPERATOR ||
                        scanner.getCurrentSymbol() == ScannerSymbols.GREATER_OPERATOR ||
                        scanner.getCurrentSymbol() == ScannerSymbols.GREATER_OR_EQUAL_OPERATOR ||
                        scanner.getCurrentSymbol() == ScannerSymbols.LESS_OPERATOR ||
                        scanner.getCurrentSymbol() == ScannerSymbols.LESS_OR_EQUAL_OPERATOR
                ) {
            ASTOperator operator = null;
            if (checkOptionalAndProceed(ScannerSymbols.EQUALITY_OPERATOR)) {
                operator = ASTOperator.EQUAL;
            } else if (checkOptionalAndProceed(ScannerSymbols.INEQUALITY_OPERATOR)) {
                operator = ASTOperator.NOT_EQUAL;
            } else if (checkOptionalAndProceed(ScannerSymbols.GREATER_OPERATOR)) {
                operator = ASTOperator.GREATER_THAN;
            } else if (checkOptionalAndProceed(ScannerSymbols.GREATER_OR_EQUAL_OPERATOR)) {
                operator = ASTOperator.GREATER_THAN_OR_EQUAL;
            } else if (checkOptionalAndProceed(ScannerSymbols.LESS_OPERATOR)) {
                operator = ASTOperator.LESS_THAN;
            } else if (checkOptionalAndProceed(ScannerSymbols.LESS_OR_EQUAL_OPERATOR)) {
                operator = ASTOperator.LESS_THAN_OR_EQUAL;
            }
            if (operator != null) {
                expr = getOrCombineExpression(expr, parseAdditiveExpression(), operator);
            }
        }
        return expr;
    }

    private ASTExpression parseAdditiveExpression() throws IOException {
        ASTExpression expr = parseMultiplicativeExpression();
        while (scanner.getCurrentSymbol() == ScannerSymbols.ADDITION_OPERATOR || scanner.getCurrentSymbol() == ScannerSymbols.SUBTRACTION_OPERATOR) {
            ASTOperator operator = null;
            if (checkOptionalAndProceed(ScannerSymbols.ADDITION_OPERATOR)) {
                operator = ASTOperator.ADDITION;
            } else if (checkOptionalAndProceed(ScannerSymbols.SUBTRACTION_OPERATOR)) {
                operator = ASTOperator.SUBTRACTION;
            }
            if (operator != null) {
                expr = getOrCombineExpression(expr, parseMultiplicativeExpression(), operator);
            }
        }
        return expr;
    }

    private ASTExpression parseMultiplicativeExpression() throws IOException {
        ASTExpression expr = parsePreIncrementExpression();
        while (
                scanner.getCurrentSymbol() == ScannerSymbols.MULTIPLICATION_OPERATOR ||
                        scanner.getCurrentSymbol() == ScannerSymbols.DIVISION_OPERATOR ||
                        scanner.getCurrentSymbol() == ScannerSymbols.MODULO_OPERATOR
                ) {
            ASTOperator operator = null;
            if (checkOptionalAndProceed(ScannerSymbols.MULTIPLICATION_OPERATOR)) {
                operator = ASTOperator.MULTIPLICATION;
            } else if (checkOptionalAndProceed(ScannerSymbols.DIVISION_OPERATOR)) {
                operator = ASTOperator.DIVISION;
            } else if (checkOptionalAndProceed(ScannerSymbols.MODULO_OPERATOR)) {
                operator = ASTOperator.MODULO;
            }
            if (operator != null) {
                expr = getOrCombineExpression(expr, parsePreIncrementExpression(), operator);
            }
        }
        return expr;
    }

    private ASTExpression parsePreIncrementExpression() throws IOException {
        ASTExpression expr;
        if (checkOptionalAndProceed(ScannerSymbols.INCREMENT_OPERATOR)) {
            expr = new ASTUnaryExpression(parsePrefixExpression(), ASTUnaryOperator.PRE_INCREMENT);
        } else if (checkOptionalAndProceed(ScannerSymbols.DECREMENT_OPERATOR)) {
            expr = new ASTUnaryExpression(parsePrefixExpression(), ASTUnaryOperator.PRE_DECREMENT);
        } else {
            expr = parsePrefixExpression();
        }
        return expr;
    }

    private ASTExpression parsePrefixExpression() throws IOException {
        ASTExpression expr;
        if (checkOptionalAndProceed(ScannerSymbols.SUBTRACTION_OPERATOR)) {
            ASTExpression subExpression = parsePrefixExpression();
            // optimization for literals to negate them on the fly
            if (subExpression instanceof ASTLiteralExpression) {
                ASTLiteralExpression subLiteralExpression = (ASTLiteralExpression) subExpression;
                Long originalValue = (Long) subLiteralExpression.getValue();
                expr = new ASTLiteralExpression<>(-originalValue, subLiteralExpression.getType());
            } else {
                expr = new ASTUnaryExpression(subExpression, ASTUnaryOperator.NEGATE);
            }
        } else if (checkOptionalAndProceed(ScannerSymbols.BITWISE_NEGATION_OPERATOR)) {
            expr = new ASTUnaryExpression(parsePrefixExpression(), ASTUnaryOperator.BITWISE_NOT);
        } else {
            expr = parsePrimary();
        }
        return expr;
    }

    private ASTExpression parsePrimary() throws IOException {
        ASTExpression expression;
        if (scanner.getCurrentSymbol() == ScannerSymbols.PAR_START) {
            expression = parseParExpression();
        } else if (scanner.getCurrentSymbol() == ScannerSymbols.IDENTIFIER) {
            String identifier = checkIdentifierAndProceed();
            if (scanner.getCurrentSymbol() != ScannerSymbols.PAR_START) {
                expression = new ASTVariableExpression(identifier);
                expression = parsePostIncrementExpression(expression);
            } else {
                expression = parseFunctionCall(identifier);
            }
        } else {
            expression = parseLiteral();
        }
        return expression;
    }

    private ASTExpression parsePostIncrementExpression(ASTExpression expression) throws IOException {
        if (checkOptionalAndProceed(ScannerSymbols.INCREMENT_OPERATOR)) {
            return new ASTUnaryExpression(expression, ASTUnaryOperator.POST_INCREMENT);
        } else if (checkOptionalAndProceed(ScannerSymbols.DECREMENT_OPERATOR)) {
            return new ASTUnaryExpression(expression, ASTUnaryOperator.POST_DECREMENT);
        }
        return expression;
    }

    private ASTExpression parseFunctionCall(String identifier) throws IOException {
        ASTFunctionCallExpression function = new ASTFunctionCallExpression(identifier);
        checkAndProceed(ScannerSymbols.PAR_START);
        if (scanner.getCurrentSymbol() != ScannerSymbols.PAR_END) {
            ASTArguments arguments = parseArguments();
            function.setArguments(arguments);
        }
        checkAndProceed(ScannerSymbols.PAR_END);
        return function;
    }

    private ASTArguments parseArguments() throws IOException {
        ASTArguments arguments = new ASTArguments();
        ASTExpression expression = parseExpression();
        arguments.addArgument(expression);
        while (scanner.getCurrentSymbol() != ScannerSymbols.PAR_END) {
            scanner.moveToNextSymbol();
            ASTExpression otherExpression = parseExpression();
            arguments.addArgument(otherExpression);
        }
        return arguments;
    }

    private ASTLiteralExpression parseLiteral() throws IOException {
        ASTLiteralExpression expression;
        if (scanner.getCurrentSymbol() == ScannerSymbols.INTEGER_LITERAL) {
            long value = scanner.getNumberAsLong();
            expression = new ASTLiteralExpression<>(value, ASTType.INTEGER);
        } else if (scanner.getCurrentSymbol() == ScannerSymbols.TRUE) {
            expression = new ASTLiteralExpression<>(true, ASTType.BOOLEAN);
        } else {
            check(ScannerSymbols.FALSE);
            expression = new ASTLiteralExpression<>(false, ASTType.BOOLEAN);
        }
        scanner.moveToNextSymbol();
        return expression;
    }

    private ASTType parseType() throws IOException {
        ASTType type;
        if (scanner.getCurrentSymbol() == ScannerSymbols.INTEGER) {
            type = ASTType.INTEGER;
        } else {
            check(ScannerSymbols.BOOLEAN);
            type = ASTType.BOOLEAN;
        }
        scanner.moveToNextSymbol();
        return type;
    }

    private ASTExpression getOrCombineExpression(ASTExpression left, ASTExpression right, ASTOperator operator) {
        ASTExpression expr = left;
        if (right != null) {
            ASTBinaryExpression binaryExpression = new ASTBinaryExpression(left);
            binaryExpression.setRight(right);
            binaryExpression.setOperator(operator);
            expr = binaryExpression;
        }
        return expr;
    }

    private boolean checkOptionalAndProceed(ScannerSymbols symbol) throws IOException {
        if (scanner.getCurrentSymbol() == symbol) {
            scanner.moveToNextSymbol();
            return true;
        }
        return false;
    }

    private void checkAndProceed(ScannerSymbols symbol) throws IOException {
        check(symbol);
        scanner.moveToNextSymbol();
    }

    private String checkIdentifierAndProceed() throws IOException {
        check(ScannerSymbols.IDENTIFIER);
        String identifier = scanner.getCurrentIdentifier();
        scanner.moveToNextSymbol();
        return identifier;
    }

    private void check(ScannerSymbols symbol) {
        if (scanner.getCurrentSymbol() != symbol) {
            throw new ParsingException("expected " + symbol + ", but got: " + scanner.getCurrentSymbol());
        }
    }
}
