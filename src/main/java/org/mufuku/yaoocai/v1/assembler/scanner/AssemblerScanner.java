package org.mufuku.yaoocai.v1.assembler.scanner;

import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class AssemblerScanner {

    private final Reader reader;
    private char currentCharacter;
    private AssemblerScannerSymbols currentSymbol;
    private String currentString;
    private String currentNumber;
    private byte currentByte;
    private InstructionSet.OpCodes currentOpCode;
    private boolean comment;

    public AssemblerScanner(InputStream in) throws IOException {
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
        switch (currentCharacter) {
            case 0:
                currentSymbol = AssemblerScannerSymbols.EOI;
                break;
            case '/':
                scanLineComment();
                break;
            case ':':
                currentSymbol = AssemblerScannerSymbols.COLON;
                nextChar();
                break;
            case ',':
                currentSymbol = AssemblerScannerSymbols.COMMA;
                nextChar();
                break;
            case '{':
                currentSymbol = AssemblerScannerSymbols.BLOCK_START;
                nextChar();
                break;
            case '}':
                currentSymbol = AssemblerScannerSymbols.BLOCK_END;
                nextChar();
                break;
            case '[':
                currentSymbol = AssemblerScannerSymbols.PARAM_BRACKET_START;
                nextChar();
                break;
            case ']':
                currentSymbol = AssemblerScannerSymbols.PARAM_BRACKET_END;
                nextChar();
                break;
            case '(':
                currentSymbol = AssemblerScannerSymbols.FUNCTION_PARAM_START;
                nextChar();
                break;
            case ')':
                currentSymbol = AssemblerScannerSymbols.FUNCTION_PARAM_END;
                nextChar();
                break;
            case '-':
                handleMinusOrComment();
                break;
            case '\"':
                handleString();
                break;
            default:
                handleOther();
        }
    }


    private void handleMinusOrComment() throws IOException {
        nextChar();
        if (currentCharacter == '>') {
            currentSymbol = AssemblerScannerSymbols.CONSTANT_POOL_LINK;
            nextChar();
        } else {
            scanAddressOffsetOrLineNumber();
        }
    }


    private void handleString() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(currentCharacter);
        nextChar();
        while (currentCharacter != '\"' && currentCharacter != '\n') {
            sb.append(currentCharacter);
            nextChar();
        }
        if (currentCharacter == '\"') {
            this.currentSymbol = AssemblerScannerSymbols.STRING;
            this.currentString = sb.toString();
            nextChar();
        } else {
            throw new ParsingException("String must end with quotes (\")");
        }
    }

    private void handleOther() throws IOException {
        if (Character.isDigit(currentCharacter) || currentCharacter == '+' || currentCharacter == '-') {
            scanAddressOffsetOrLineNumber();
        } else if (Character.isJavaIdentifierStart(currentCharacter)) {
            scanSymbol();
        } else {
            currentSymbol = AssemblerScannerSymbols.UNKNOWN;
        }
    }

    private void scanAddressOffsetOrLineNumber() throws IOException {
        StringBuilder number = new StringBuilder();
        number.append(currentCharacter);
        nextChar();
        boolean hex = false;
        while (isValidNumberCharacter(hex)) {
            if (currentCharacter == 'x') {
                hex = true;
            }
            number.append(currentCharacter);
            nextChar();
        }
        if (currentCharacter == ':') {
            this.currentNumber = number.toString();
            this.currentSymbol = AssemblerScannerSymbols.LINE_NUMBER;
            nextChar();
        } else {
            if (hex) {
                this.currentByte = Byte.decode(number.toString());
            } else {
                this.currentNumber = number.toString();
            }
            this.currentSymbol = AssemblerScannerSymbols.NUMBER;
        }
    }

    private void scanSymbol() throws IOException {
        currentString = "";
        StringBuilder tmp = new StringBuilder();
        while (Character.isJavaIdentifierPart(currentCharacter)) {
            tmp.append(currentCharacter);
            nextChar();
        }
        currentString = tmp.toString();

        if ("constant_pool".equals(currentString)) {
            currentSymbol = AssemblerScannerSymbols.CONSTANT_POOL;
        } else if ("unit".equals(currentString)) {
            currentSymbol = AssemblerScannerSymbols.UNIT;
        } else if ("function".equals(currentString)) {
            currentSymbol = AssemblerScannerSymbols.FUNCTION;
        } else {
            InstructionSet.OpCodes opCode = InstructionSet.OpCodes.getByMnemonic(currentString);
            if (opCode != null) {
                currentOpCode = opCode;
                currentSymbol = AssemblerScannerSymbols.MNEMONIC;
            } else {
                currentSymbol = AssemblerScannerSymbols.IDENTIFIER;
            }
        }
    }

    private void scanLineComment() throws IOException {
        nextChar();
        while (currentCharacter != '\n' && currentCharacter != 0) {
            nextChar();
        }
        comment = true;
    }

    private boolean isValidNumberCharacter(boolean hex) {
        boolean result = true;
        if (!Character.isDigit(currentCharacter)) {
            if (!hex) {
                result = currentCharacter == 'x';
            } else {
                result = isHexCharacter(currentCharacter);
            }
        }
        return result;
    }

    private boolean isHexCharacter(char currentCharacter) {
        return
                (currentCharacter >= 'a' && currentCharacter <= 'f') || (currentCharacter >= 'A' && currentCharacter <= 'F');
    }

    public AssemblerScannerSymbols getCurrentSymbol() {
        return currentSymbol;
    }

    public String getCurrentString() {
        return currentString;
    }

    public long getCurrentNumber() {
        return Long.parseLong(currentNumber);
    }

    public byte getCurrentByte() {
        return currentByte;
    }

    public InstructionSet.OpCodes getCurrentOpCode() {
        return currentOpCode;
    }
}
