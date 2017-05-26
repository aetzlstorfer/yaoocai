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
    private Short currentNumber;
    private InstructionSet.OpCodes currentOpCode;

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

        while (Character.isWhitespace(currentCharacter)) {
            nextChar();
        }

        if (currentCharacter == 0) {
            currentSymbol = AssemblerScannerSymbols.EOI;
        } else if (currentCharacter == ':') {
            currentSymbol = AssemblerScannerSymbols.COLON;
            nextChar();
        } else if (currentCharacter == '[') {
            currentSymbol = AssemblerScannerSymbols.PARAM_BRACKET_START;
            nextChar();
        } else if (currentCharacter == ']') {
            currentSymbol = AssemblerScannerSymbols.PARAM_BRACKET_END;
            nextChar();
        } else if (currentCharacter == '(') {
            currentSymbol = AssemblerScannerSymbols.FUNCTION_PARAM_START;
            nextChar();
        } else if (currentCharacter == ')') {
            currentSymbol = AssemblerScannerSymbols.FUNCTION_PARAM_END;
            nextChar();
        } else if (currentCharacter == '#') {
            currentSymbol = AssemblerScannerSymbols.FUNCTION_INDEX;
            nextChar();
            StringBuilder index = new StringBuilder();
            while (Character.isDigit(currentCharacter)) {
                index.append(currentCharacter);
                nextChar();
            }
            this.currentNumber = Short.parseShort(index.toString());
            nextChar();
        } else if (Character.isDigit(currentCharacter) || currentCharacter == '+' || currentCharacter == '-') {
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
                if (hex) {
                    throw new ParsingException("Hex number is not allowed as line number");
                }
                this.currentNumber = Short.parseShort(number.toString());
                this.currentSymbol = AssemblerScannerSymbols.LINE_NUMBER;
                nextChar();
            } else {
                if (hex) {
                    this.currentNumber = Short.decode(number.toString());
                } else {
                    this.currentNumber = Short.parseShort(number.toString());
                }
                this.currentSymbol = AssemblerScannerSymbols.OPCODE_PARAM;
            }

        } else if (Character.isJavaIdentifierStart(currentCharacter)) {
            currentString = "";
            StringBuilder tmp = new StringBuilder();
            while (Character.isJavaIdentifierPart(currentCharacter)) {
                tmp.append(currentCharacter);
                nextChar();
            }
            currentString = tmp.toString();

            if ("Function".equals(currentString)) {
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
        } else {
            currentSymbol = AssemblerScannerSymbols.UNKNOWN;
        }
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

    public Short getCurrentNumber() {
        return currentNumber;
    }

    public InstructionSet.OpCodes getCurrentOpCode() {
        return currentOpCode;
    }
}
