package org.mufuku.yaoocai.v1.compiler.scanner;

import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class Scanner {

    private final Reader reader;
    private char currentCharacter;
    private ScannerSymbols currentSymbol;

    private String currentIdentifier;

    private String currentNumber;
    private boolean comment;

    public Scanner(InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
    }

    private void nextChar() throws IOException {
        int ch = reader.read();
        if (ch >= 0) {
            currentCharacter = (char) ch;
        } else {
            currentCharacter = 0;
        }
    }

    public void initialize() throws IOException {
        nextChar();
        moveToNextSymbol();
    }

    public void moveToNextSymbol() throws IOException {
        do {
            comment = false;
            moveToNextSymbol0();
        } while (comment);
    }

    private void moveToNextSymbol0() throws IOException {
        while (Character.isWhitespace(currentCharacter)) {
            nextChar();
        }
        switch (currentCharacter) {
            case 0:
                currentSymbol = ScannerSymbols.EOI;
                break;
            case '+':
                scanAdditionAdditionAssignmentOrIncrement();
                break;
            case '-':
                scanSubtractionBuiltinAssignmentOrDecrement();
                break;
            case '/':
                scanDivisionCommentOrDivisionalAssignment();
                break;
            case '*':
                scanMultiplicationOrMultiplicativeAssignment();
                break;
            case '%':
                currentSymbol = ScannerSymbols.MODULO_OPERATOR;
                nextChar();
                break;
            case ',':
                currentSymbol = ScannerSymbols.COMMA;
                nextChar();
                break;
            case ';':
                currentSymbol = ScannerSymbols.SEMICOLON;
                nextChar();
                break;
            case ':':
                currentSymbol = ScannerSymbols.COLON;
                nextChar();
                break;
            case '!':
                scanBitwiseNotOrInequality();
                break;
            case '=':
                scanEqualOrAssignment();
                break;
            case '>':
                scanGreaterOrGreaterEquals();
                break;
            case '<':
                scanLessThanOrLessThanEquals();
                break;
            case '{':
                currentSymbol = ScannerSymbols.BLOCK_START;
                nextChar();
                break;
            case '}':
                currentSymbol = ScannerSymbols.BLOCK_END;
                nextChar();
                break;
            case '(':
                currentSymbol = ScannerSymbols.PAR_START;
                nextChar();
                break;
            case ')':
                currentSymbol = ScannerSymbols.PAR_END;
                nextChar();
                break;
            case '|':
                scanBitwiseOrOrConditionalOr();
                break;
            case '&':
                scanBitwiseAndOrConditionalAnd();
                break;
            default:
                if (Character.isJavaIdentifierStart(currentCharacter)) {
                    scanIdentifierOrKeyword();
                } else if ((currentCharacter >= '0' && currentCharacter <= '9')) {
                    scanIntegerLiteral();
                } else {
                    currentSymbol = ScannerSymbols.UNKNOWN;
                }
        }
    }

    private void scanAdditionAdditionAssignmentOrIncrement() throws IOException {
        currentSymbol = ScannerSymbols.ADDITION_OPERATOR;
        nextChar();
        if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.ADDITION_ASSIGNMENT_OPERATOR;
            nextChar();
        } else if (currentCharacter == '+') {
            currentSymbol = ScannerSymbols.INCREMENT_OPERATOR;
            nextChar();
        }
    }

    private void scanSubtractionBuiltinAssignmentOrDecrement() throws IOException {
        currentSymbol = ScannerSymbols.SUBTRACTION_OPERATOR;
        nextChar();
        if (currentCharacter == '>') {
            currentSymbol = ScannerSymbols.BUILTIN_ASSIGNMENT;
            nextChar();
        } else if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.SUBTRACTION_ASSIGNMENT_OPERATOR;
            nextChar();
        } else if (currentCharacter == '-') {
            currentSymbol = ScannerSymbols.DECREMENT_OPERATOR;
            nextChar();
        }
    }

    private void scanDivisionCommentOrDivisionalAssignment() throws IOException {
        nextChar();
        if (currentCharacter == '*') { // block comment
            scanBlockComment();
        } else if (currentCharacter == '/') { // line comment
            scanLineComment();
        } else if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.DIVISION_ASSIGNMENT_OPERATOR;
            nextChar();
        } else {
            currentSymbol = ScannerSymbols.DIVISION_OPERATOR;
        }
    }

    private void scanBlockComment() throws IOException {
        nextChar();
        while (currentCharacter != '*' && currentCharacter != 0) {
            nextChar();
        }
        if (currentCharacter == 0) {
            throw new ParsingException("Comment should be closed with a asterisk and slash (*/)");
        }
        nextChar();
        if (currentCharacter != '/') {
            throw new ParsingException("Comment should be closed with a slash (/)");
        }
        nextChar();
        comment = true;
    }

    private void scanLineComment() throws IOException {
        nextChar();
        while (currentCharacter != '\n' && currentCharacter != 0) {
            nextChar();
        }
        if (currentCharacter == 0) {
            currentSymbol = ScannerSymbols.EOI;
            return;
        }
        comment = true;
    }

    private void scanMultiplicationOrMultiplicativeAssignment() throws IOException {
        currentSymbol = ScannerSymbols.MULTIPLICATION_OPERATOR;
        nextChar();
        if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.MULTIPLICATION_ASSIGNMENT_OPERATOR;
            nextChar();
        }
    }

    private void scanBitwiseNotOrInequality() throws IOException {
        currentSymbol = ScannerSymbols.BITWISE_NEGATION_OPERATOR;
        nextChar();
        if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.INEQUALITY_OPERATOR;
            nextChar();
        }
    }

    private void scanEqualOrAssignment() throws IOException {
        nextChar();
        if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.EQUALITY_OPERATOR;
            nextChar();
        } else {
            currentSymbol = ScannerSymbols.ASSIGNMENT_OPERATOR;
        }
    }

    private void scanGreaterOrGreaterEquals() throws IOException {
        currentSymbol = ScannerSymbols.GREATER_OPERATOR;
        nextChar();
        if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.GREATER_OR_EQUAL_OPERATOR;
            nextChar();
        }
    }

    private void scanLessThanOrLessThanEquals() throws IOException {
        currentSymbol = ScannerSymbols.LESS_OPERATOR;
        nextChar();
        if (currentCharacter == '=') {
            currentSymbol = ScannerSymbols.LESS_OR_EQUAL_OPERATOR;
            nextChar();
        }
    }

    private void scanBitwiseOrOrConditionalOr() throws IOException {
        currentSymbol = ScannerSymbols.BITWISE_OR_OPERATOR;
        nextChar();
        if (currentCharacter == '|') {
            currentSymbol = ScannerSymbols.CONDITIONAL_OR_OPERATOR;
            nextChar();
        }
    }

    private void scanBitwiseAndOrConditionalAnd() throws IOException {
        currentSymbol = ScannerSymbols.BITWISE_AND_OPERATOR;
        nextChar();
        if (currentCharacter == '&') {
            currentSymbol = ScannerSymbols.CONDITIONAL_AND_OPERATOR;
            nextChar();
        }
    }

    private void scanIdentifierOrKeyword() throws IOException {
        currentIdentifier = "";
        StringBuilder tmp = new StringBuilder();
        while (Character.isJavaIdentifierPart(currentCharacter)) {
            tmp.append(currentCharacter);
            nextChar();
        }
        currentIdentifier = tmp.toString();

        switch (currentIdentifier) {
            case "builtin":
                currentSymbol = ScannerSymbols.BUILTIN;
                break;
            case "function":
                currentSymbol = ScannerSymbols.FUNCTION;
                break;
            case "var":
                currentSymbol = ScannerSymbols.VARIABLE;
                break;
            case "if":
                currentSymbol = ScannerSymbols.IF;
                break;
            case "else":
                currentSymbol = ScannerSymbols.ELSE;
                break;
            case "while":
                currentSymbol = ScannerSymbols.WHILE;
                break;
            case "return":
                currentSymbol = ScannerSymbols.RETURN;
                break;
            case "true":
                currentSymbol = ScannerSymbols.TRUE;
                break;
            case "false":
                currentSymbol = ScannerSymbols.FALSE;
                break;
            case "integer":
                currentSymbol = ScannerSymbols.INTEGER;
                break;
            case "boolean":
                currentSymbol = ScannerSymbols.BOOLEAN;
                break;
            default:
                currentSymbol = ScannerSymbols.IDENTIFIER;
                break;
        }
    }

    private void scanIntegerLiteral() throws IOException {
        StringBuilder tmp = new StringBuilder();
        while ((currentCharacter >= '0' && currentCharacter <= '9')) {
            tmp.append(currentCharacter);
            nextChar();
        }
        this.currentNumber = tmp.toString();
        currentSymbol = ScannerSymbols.INTEGER_LITERAL;
    }

    public ScannerSymbols getCurrentSymbol() {
        return currentSymbol;
    }

    public String getCurrentIdentifier() {
        return currentIdentifier;
    }

    public short getNumberAsShort() {
        return Short.parseShort(currentNumber);
    }

    public int getNumberAsInteger() {
        return Integer.parseInt(currentNumber);
    }
}
