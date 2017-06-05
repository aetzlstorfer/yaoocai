package org.mufuku.yaoocai.v1.assembler.parser;

import org.mufuku.yaoocai.v1.assembler.ast.ASTAssemblerFunction;
import org.mufuku.yaoocai.v1.assembler.ast.ASTAssemblerScript;
import org.mufuku.yaoocai.v1.assembler.scanner.AssemblerScanner;
import org.mufuku.yaoocai.v1.assembler.scanner.AssemblerScannerSymbols;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class AssemblerParser {

    private final AssemblerScanner scanner;

    private Short mainFunctionIndex = null;

    public AssemblerParser(AssemblerScanner scanner) {
        this.scanner = scanner;
    }

    public ASTAssemblerScript parse() throws IOException {
        ASTAssemblerScript script = new ASTAssemblerScript();
        script.setMajorVersion(InstructionSet.MAJOR_VERSION);
        script.setMinorVersion(InstructionSet.MINOR_VERSION);

        while (scanner.getCurrentSymbol() == AssemblerScannerSymbols.FUNCTION) {
            ASTAssemblerFunction function = parseFunction();
            script.addFunction(function);
        }

        if (scanner.getCurrentSymbol() != AssemblerScannerSymbols.EOI) {
            throw new ParsingException("unexpected " + scanner.getCurrentSymbol());
        }

        if (mainFunctionIndex != null) {
            script.setMainFunctionIndex(mainFunctionIndex);
        } else {
            throw new ParsingException("No main function defined");
        }

        return script;
    }

    private ASTAssemblerFunction parseFunction() throws IOException {

        List<Short> instructions = new ArrayList<>();

        checkAndProceed(AssemblerScannerSymbols.FUNCTION);
        checkAndProceed(AssemblerScannerSymbols.COLON);

        check(AssemblerScannerSymbols.FUNCTION_INDEX);
        short functionIndex = scanner.getCurrentNumber();
        scanner.moveToNextSymbol();

        if (checkOptionalAndProceed(AssemblerScannerSymbols.FUNCTION_PARAM_START)) {
            check(AssemblerScannerSymbols.IDENTIFIER);
            String identifier = scanner.getCurrentString();
            if ("main".equals(identifier)) {
                if (this.mainFunctionIndex != null) {
                    throw new ParsingException("Main function already declared!");
                }
                this.mainFunctionIndex = functionIndex;
            } else {
                throw new ParsingException("Invalid function parameters (main expected)");
            }
            scanner.moveToNextSymbol();
            checkAndProceed(AssemblerScannerSymbols.FUNCTION_PARAM_END);
        }

        while (scanner.getCurrentSymbol() == AssemblerScannerSymbols.LINE_NUMBER) {
            scanner.moveToNextSymbol();
            check(AssemblerScannerSymbols.MNEMONIC);

            InstructionSet.OpCodes opCode = scanner.getCurrentOpCode();
            scanner.moveToNextSymbol();

            instructions.add(opCode.code());

            int params = opCode.opCodeParam();
            if (params > 0) {
                checkAndProceed(AssemblerScannerSymbols.PARAM_BRACKET_START);
                for (int i = 0; i < params; i++) {
                    check(AssemblerScannerSymbols.OPCODE_PARAM);

                    short param = scanner.getCurrentNumber();
                    instructions.add(param);

                    scanner.moveToNextSymbol();
                }
                checkAndProceed(AssemblerScannerSymbols.PARAM_BRACKET_END);
            }
        }
        return new ASTAssemblerFunction(instructions);
    }

    private boolean checkOptionalAndProceed(AssemblerScannerSymbols symbol) throws IOException {
        if (scanner.getCurrentSymbol() == symbol) {
            scanner.moveToNextSymbol();
            return true;
        }
        return false;
    }

    private void checkAndProceed(AssemblerScannerSymbols symbol) throws IOException {
        check(symbol);
        scanner.moveToNextSymbol();
    }

    private void check(AssemblerScannerSymbols symbol) {
        if (scanner.getCurrentSymbol() != symbol) {
            throw new ParsingException("expected " + symbol + ", but got: " + scanner.getCurrentSymbol());
        }
    }
}
