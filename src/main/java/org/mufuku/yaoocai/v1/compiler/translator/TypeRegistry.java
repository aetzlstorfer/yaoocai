package org.mufuku.yaoocai.v1.compiler.translator;

import org.mufuku.yaoocai.v1.compiler.ast.*;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
class TypeRegistry {

    private final LocalVariableStorage localVariableStorage;

    private final FunctionStorage functionStorage;

    TypeRegistry(LocalVariableStorage localVariableStorage, FunctionStorage functionStorage) {
        this.localVariableStorage = localVariableStorage;
        this.functionStorage = functionStorage;
    }

    ASTType resolveType(ASTExpression expression) {
        ASTType type = null;
        if (expression instanceof ASTUnaryExpression) {
            type = resolveType(((ASTUnaryExpression) expression).getSubExpression());
        } else if (expression instanceof ASTLiteralExpression) {
            type = ((ASTLiteralExpression) expression).getType();
        } else if (expression instanceof ASTVariableExpression) {
            String variableName = ((ASTVariableExpression) expression).getVariableName();
            type = localVariableStorage.getVariableType(variableName);
        } else if (expression instanceof ASTFunctionCallExpression) {
            String functionName = ((ASTFunctionCallExpression) expression).getFunctionName();
            type = functionStorage.getFunctionReturnType(functionName);
        } else if (expression instanceof ASTBinaryExpression) {
            ASTBinaryExpression binaryExpression = (ASTBinaryExpression) expression;
            ASTType leftType = resolveType(binaryExpression.getLeft());
            ASTType rightType = resolveType(binaryExpression.getRight());
            if (compatible(leftType, rightType)) {
                if (ASTOperator.COMPARISON_OPERATORS.contains(binaryExpression.getOperator())) {
                    type = ASTType.BOOLEAN;
                } else {
                    type = leftType;
                }
            } else {
                throw new ParsingException("Incompatible types");
            }
        }
        return type;
    }

    boolean compatible(ASTType leftType, ASTType rightType) {
        return leftType.isPrimitive() && rightType.isPrimitive() && leftType.getTypeName().equals(rightType.getTypeName());
    }
}
