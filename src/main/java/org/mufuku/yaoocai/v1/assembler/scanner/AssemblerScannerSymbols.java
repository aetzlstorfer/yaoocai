package org.mufuku.yaoocai.v1.assembler.scanner;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public enum AssemblerScannerSymbols {
    EOI,
    UNKNOWN,

    IDENTIFIER,

    CONSTANT_POOL,
    CONSTANT_POOL_LINK,

    UNIT,

    BLOCK_START,
    BLOCK_END,

    FUNCTION,
    FUNCTION_PARAM_START,
    FUNCTION_PARAM_END,

    MNEMONIC,
    NUMBER,
    LINE_NUMBER,
    COLON,
    COMMA,
    PARAM_BRACKET_START,
    PARAM_BRACKET_END
}
