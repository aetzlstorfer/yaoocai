package org.mufuku.yaoocai.v1.assembler.parser;

import org.mufuku.yaoocai.v1.assembler.scanner.AssemblerScanner;
import org.mufuku.yaoocai.v1.assembler.scanner.AssemblerScannerSymbols;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;
import org.mufuku.yaoocai.v1.bytecode.data.*;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class AssemblerParser {

    private final AssemblerScanner scanner;

    private BCConstantPoolBuilder constantPoolBuilder = new BCConstantPoolBuilder();

    public AssemblerParser(AssemblerScanner scanner) {
        this.scanner = scanner;
    }

    public BCFile parse() throws IOException {
        if (checkOptionalAndProceed(AssemblerScannerSymbols.CONSTANT_POOL)) {
            parseConstantPool();
        }
        BCUnits units = parseUnits();

        BCFile file = new BCFile();
        file.setMajorVersion(InstructionSet.MAJOR_VERSION);
        file.setMinorVersion(InstructionSet.MINOR_VERSION);
        file.setPreamble(InstructionSet.PREAMBLE);
        file.setUnits(units);
        file.setConstantPool(constantPoolBuilder.build());

        return file;
    }

    private void parseConstantPool() throws IOException {
        checkAndProceed(AssemblerScannerSymbols.BLOCK_START);

        List<BCConstantPoolItem> items = new ArrayList<>();

        while (checkOptionalAndProceed(AssemblerScannerSymbols.LINE_NUMBER)) {
            long index = scanner.getCurrentNumber();
            String constantPoolItemTypeDisplayName = checkIdentifierAndProceed();
            BCConstantPoolItemType constantPoolItemType =
                    BCConstantPoolItemType.getByDisplayName(constantPoolItemTypeDisplayName);
            checkAndProceed(AssemblerScannerSymbols.CONSTANT_POOL_LINK);
            if (constantPoolItemType == BCConstantPoolItemType.INTEGER) {
                checkAndProceed(AssemblerScannerSymbols.NUMBER);
                long integerLiteral = scanner.getCurrentNumber();

                BCConstantPoolItem<Integer> item = new BCConstantPoolItem<>();
                item.setIndex((short) index);
                item.setValue((int) integerLiteral);
                item.setType(constantPoolItemType);

                items.add(item);
            } else if (constantPoolItemType == BCConstantPoolItemType.STRING) {
                checkAndProceed(AssemblerScannerSymbols.STRING);
                String stringLiteral = scanner.getCurrentString();
                BCConstantPoolItem<String> item = new BCConstantPoolItem<>();
                item.setValue(stringLiteral);
                item.setType(constantPoolItemType);
                items.add(item);
            } else if (constantPoolItemType == BCConstantPoolItemType.SYMBOL) {
                checkAndProceed(AssemblerScannerSymbols.IDENTIFIER);
                String stringLiteral = scanner.getCurrentString();
                BCConstantPoolItem<String> item = new BCConstantPoolItem<>();
                item.setIndex((short) index);
                item.setValue(stringLiteral);
                item.setType(constantPoolItemType);
                items.add(item);
            }
        }
        initConstantBuilder(items);
        checkAndProceed(AssemblerScannerSymbols.BLOCK_END);
    }

    private void initConstantBuilder(List<BCConstantPoolItem> items) {
        OptionalInt max = items
                .stream()
                .mapToInt(BCConstantPoolItem::getIndex)
                .max();
        if (max.isPresent()) {
            int maxIndex = max.getAsInt();
            List<BCConstantPoolItem> convertedItems = mapItemsToCorrectIndexAndFillHoles(items, maxIndex);
            this.constantPoolBuilder = new BCConstantPoolBuilder(convertedItems, (short) convertedItems.size());
        }
    }

    private List<BCConstantPoolItem> mapItemsToCorrectIndexAndFillHoles(List<BCConstantPoolItem> items, int maxIndex) {
        BCConstantPoolItem[] newItems = new BCConstantPoolItem[maxIndex + 1];
        BCConstantPoolItem<Void> empty = new BCConstantPoolItem<>();
        empty.setType(BCConstantPoolItemType.EMPTY);
        Arrays.fill(newItems, empty);
        items.forEach(item -> newItems[item.getIndex()] = item);
        return Arrays.stream(newItems).collect(Collectors.toList());
    }

    private BCUnits parseUnits() throws IOException {
        check(AssemblerScannerSymbols.UNIT);
        List<BCUnit> units = new ArrayList<>();
        while (scanner.getCurrentSymbol() == AssemblerScannerSymbols.UNIT) {
            BCUnit unit = parseUnit();
            units.add(unit);
        }
        return new BCUnits(units);
    }

    private BCUnit parseUnit() throws IOException {
        checkAndProceed(AssemblerScannerSymbols.UNIT);
        String unitName = checkIdentifierAndProceed();
        checkAndProceed(AssemblerScannerSymbols.BLOCK_START);

        short unitNameSymbolIndex = this.constantPoolBuilder.getSymbolIndex(unitName);

        List<BCUnitItem> items = new ArrayList<>();
        while (scanner.getCurrentSymbol() == AssemblerScannerSymbols.FUNCTION) {
            BCUnitItem item = parseFunction();
            items.add(item);
        }
        checkAndProceed(AssemblerScannerSymbols.BLOCK_END);

        BCUnit unit = new BCUnit(unitNameSymbolIndex);
        unit.setItems(items);
        return unit;
    }

    private BCUnitItemFunction parseFunction() throws IOException {
        checkAndProceed(AssemblerScannerSymbols.FUNCTION);

        String functionName = checkIdentifierAndProceed();
        short functionNameSymbolIndex = this.constantPoolBuilder.getSymbolIndex(functionName);

        BCParameters parameters = parseParameters();

        BCType returnType = BCType.NO_TYPE;
        if (checkOptionalAndProceed(AssemblerScannerSymbols.COLON)) {
            String returnTypeName = checkIdentifierAndProceed();
            returnType = convertType(returnTypeName);
        }
        checkAndProceed(AssemblerScannerSymbols.BLOCK_START);
        BCCode code = parseCode();
        checkAndProceed(AssemblerScannerSymbols.BLOCK_END);

        BCUnitItemFunction function = new BCUnitItemFunction(functionNameSymbolIndex);
        function.setCode(code);
        function.setReturnType(returnType);
        function.setParameters(parameters);

        return function;
    }

    private BCParameters parseParameters() throws IOException {
        List<BCNameAndType> nameAndTypes = new ArrayList<>();
        checkAndProceed(AssemblerScannerSymbols.FUNCTION_PARAM_START);
        while (scanner.getCurrentSymbol() != AssemblerScannerSymbols.FUNCTION_PARAM_END) {
            nameAndTypes.add(parseParameter());
            while (checkOptionalAndProceed(AssemblerScannerSymbols.COMMA)) {
                nameAndTypes.add(parseParameter());
            }
        }
        checkAndProceed(AssemblerScannerSymbols.FUNCTION_PARAM_END);
        return new BCParameters(nameAndTypes);
    }

    private BCNameAndType parseParameter() throws IOException {
        String paramName = checkIdentifierAndProceed();
        short paramNameSymbolIndex = constantPoolBuilder.getSymbolIndex(paramName);

        checkAndProceed(AssemblerScannerSymbols.COLON);
        String typeName = checkIdentifierAndProceed();

        return new BCNameAndType(convertType(typeName), paramNameSymbolIndex);
    }

    private BCType convertType(String typeName) {
        if ("integer".equals(typeName)) {
            return new BCType(BCTypeType.INTEGER);
        } else if ("boolean".equals(typeName)) {
            return new BCType(BCTypeType.BOOLEAN);
        } else {
            short typeNameSymbolIndex = constantPoolBuilder.getSymbolIndex(typeName);
            BCType type = new BCType(BCTypeType.REFERENCE_TYPE);
            type.setReferenceNameIndex(typeNameSymbolIndex);
            return type;
        }
    }

    private BCCode parseCode() throws IOException {
        BCCodeBuilder codeBuilder = new BCCodeBuilder();
        while (scanner.getCurrentSymbol() == AssemblerScannerSymbols.LINE_NUMBER) {
            scanner.moveToNextSymbol();
            check(AssemblerScannerSymbols.MNEMONIC);

            InstructionSet.OpCodes opCode = scanner.getCurrentOpCode();
            scanner.moveToNextSymbol();

            int params = opCode.opCodeParam();
            if (params > 0) {
                byte[] paramBytes = new byte[params];
                checkAndProceed(AssemblerScannerSymbols.PARAM_BRACKET_START);
                for (int i = 0; i < params; i++) {
                    if (i > 0) {
                        checkAndProceed(AssemblerScannerSymbols.COMMA);
                    }
                    check(AssemblerScannerSymbols.NUMBER);
                    paramBytes[i] = scanner.getCurrentByte();
                    scanner.moveToNextSymbol();
                }
                checkAndProceed(AssemblerScannerSymbols.PARAM_BRACKET_END);
                codeBuilder.writeOpCode(opCode, paramBytes);
            } else {
                codeBuilder.writeOpCode(opCode);
            }
        }
        return codeBuilder.build();
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

    private String checkIdentifierAndProceed() throws IOException {
        check(AssemblerScannerSymbols.IDENTIFIER);
        String identifier = scanner.getCurrentString();
        scanner.moveToNextSymbol();
        return identifier;
    }
}
