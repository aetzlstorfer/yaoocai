package org.mufuku.yaoocai.v1.compiler.scanner;

import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class Scanner {

    private final Reader reader;
    private char currentCharacter;
    private ScannerSymbols currentSymbol;

    private String currentIdentifier;
    private String currentString;
    private short currentNumber;
    private boolean comment;

    public Scanner(InputStream in) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(in));
        nextChar();
        moveToNextSymbol();
    }

    private void nextChar() throws IOException {
        int ch = reader.read();
        if (ch >= 0) {
            currentCharacter = (char) ch;
        } else {
            currentCharacter = 0;
        }
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

        if (currentCharacter == 0) {
            currentSymbol = ScannerSymbols.EOI;
        } else if (currentCharacter == '+') {
            currentSymbol = ScannerSymbols.PLUS_OPERATOR;
            nextChar();
        } else if (currentCharacter == '-') {
            currentSymbol = ScannerSymbols.MINUS_OPERATOR;
            nextChar();
            if (currentCharacter == '>') {
                currentSymbol = ScannerSymbols.BUILTIN_ASSIGNMENT;
                nextChar();
            }
        } else if (currentCharacter == '/') {
            nextChar();
            if (currentCharacter == '*') { // block comment
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
            } else if (currentCharacter == '/') { // line comment
                nextChar();
                while (currentCharacter != '\n' && currentCharacter != 0) {
                    nextChar();
                }
                if (currentCharacter == 0) {
                    currentSymbol = ScannerSymbols.EOI;
                    return;
                }
                comment = true;
            } else {
                currentSymbol = ScannerSymbols.DIVISION_OPERATOR;
            }
        } else if (currentCharacter == '*') {
            currentSymbol = ScannerSymbols.MULTIPLICATION_OPERATOR;
            nextChar();
        } else if (currentCharacter == ',') {
            currentSymbol = ScannerSymbols.COMMA;
            nextChar();
        } else if (currentCharacter == ';') {
            currentSymbol = ScannerSymbols.SEMICOLON;
            nextChar();
        } else if (currentCharacter == ':') {
            currentSymbol = ScannerSymbols.COLON;
            nextChar();
        } else if (currentCharacter == '!') {
            nextChar();
            if (currentCharacter == '=') {
                currentSymbol = ScannerSymbols.INEQUALITY_OPERATOR;
                nextChar();
            } else {
                throw new ParsingException("Unsupported character. = expected.");
            }
        } else if (currentCharacter == '=') {
            nextChar();
            if (currentCharacter == '=') {
                currentSymbol = ScannerSymbols.EQUALITY_OPERATOR;
                nextChar();
            } else if (currentCharacter == '>') {
                currentSymbol = ScannerSymbols.GREATER_OR_EQUAL_OPERATOR;
                nextChar();
            } else if (currentCharacter == '<') {
                currentSymbol = ScannerSymbols.LESS_OR_EQUAL_OPERATOR;
                nextChar();
            } else {
                currentSymbol = ScannerSymbols.ASSIGNMENT_OPERATOR;
            }
        } else if (currentCharacter == '>') {
            currentSymbol = ScannerSymbols.GREATER_OPERATOR;
            nextChar();
        } else if (currentCharacter == '<') {
            currentSymbol = ScannerSymbols.LESS_OPERATOR;
            nextChar();
        } else if (currentCharacter == '{') {
            currentSymbol = ScannerSymbols.BLOCK_START;
            nextChar();
        } else if (currentCharacter == '}') {
            currentSymbol = ScannerSymbols.BLOCK_END;
            nextChar();
        } else if (currentCharacter == '(') {
            currentSymbol = ScannerSymbols.PAR_START;
            nextChar();
        } else if (currentCharacter == ')') {
            currentSymbol = ScannerSymbols.PAR_END;
            nextChar();
        } else if (currentCharacter == '"') {
            currentSymbol = ScannerSymbols.STRING_LITERAL;
            StringBuilder tmp = new StringBuilder();
            nextChar();
            while (currentCharacter != '"') {
                tmp.append(currentCharacter);
                nextChar(); // TODO add escaping rules
            }
            if (currentCharacter != '"') {
                throw new ParsingException("no quote at the end of the string literal");
            } else {
                nextChar();
            }
            this.currentString = tmp.toString();
        } else if (
                (currentCharacter >= 'a' && currentCharacter <= 'z') ||
                        (currentCharacter >= 'A' && currentCharacter <= 'Z')) {
            currentIdentifier = "";
            StringBuilder tmp = new StringBuilder();
            while ((currentCharacter >= 'a' && currentCharacter <= 'z') ||
                    (currentCharacter >= 'A' && currentCharacter <= 'Z') ||
                    (currentCharacter >= '0' && currentCharacter <= '9')) {
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
//                case "null":
//                    currentSymbol = ScannerSymbols.NULL;
//                    break;
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
        } else if (
                (currentCharacter >= '0' && currentCharacter <= '9')) {
            StringBuilder tmp = new StringBuilder();
            while ((currentCharacter >= '0' && currentCharacter <= '9')) {
                tmp.append(currentCharacter);
                nextChar();
            }
            try {
                this.currentNumber = Short.parseShort(tmp.toString());
            } catch (NumberFormatException nfe) {
                throw new ParsingException("the number " + tmp + " seems to be too long");
            }
            currentSymbol = ScannerSymbols.INTEGER_LITERAL;

        } else {
            currentSymbol = ScannerSymbols.UNKNOWN;
        }
    }

    public ScannerSymbols getCurrentSymbol() {
        return currentSymbol;
    }

    public String getCurrentIdentifier() {
        return currentIdentifier;
    }

    public String getCurrentString() {
        return currentString;
    }

    public short getCurrentNumber() {
        return currentNumber;
    }
}
