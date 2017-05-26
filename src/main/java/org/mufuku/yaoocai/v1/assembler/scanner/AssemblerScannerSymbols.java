package org.mufuku.yaoocai.v1.assembler.scanner;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum AssemblerScannerSymbols {
    EOI,
    UNKNOWN,

    IDENTIFIER,

    FUNCTION,
    FUNCTION_INDEX,

    FUNCTION_PARAM_START,
    FUNCTION_PARAM_END,

    MNEMONIC,
    OPCODE_PARAM,
    LINE_NUMBER,
    COLON,
    PARAM_BRACKET_START,
    PARAM_BRACKET_END
}
