package org.mufuku.yaoocai.v1.bytecode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public interface InstructionSet {

    String PREAMBLE = "yaoocai";

    enum OpCodes {
        // 0. functions
        FUNCTION(0x0000, "function", 0),
        // 1. constants
        I_CONST(0x0100, "i_const", 1),
        B_CONST_TRUE(0x0101, "b_const_true", 0),
        B_CONST_FALSE(0x0102, "b_const_false", 0),
        // 2. stack
        STORE(0x0200, "store", 1),
        LOAD(0x0201, "load", 1),
        // 3. invoke
        INVOKE(0x0300, "invoke", 1),
        INVOKE_BUILTIN(0x0301, "invoke_builtin", 1),
        // 4. arithmetical operators
        ADD(0x0400, "add", 0),
        SUB(0x0401, "sub", 0),
        MUL(0x0402, "mul", 0),
        DIV(0x0403, "div", 0),
        // 5. compare operators
        CMP_LT(0x0500, "cmp_lt", 0),
        CMP_LTE(0x0501, "cmp_lte", 0),
        CMP_GT(0x0502, "cmp_gt", 0),
        CMP_GTE(0x0503, "cmp_gte", 0),
        // 6. control structure
        IF(0x0600, "if", 1, true),
        GOTO(0x0601, "goto", 1, true),
        RETURN(0x0602, "return", 0),
        POP_PARAMS(0x0603, "pop_params", 1);

        private static final Map<Short, OpCodes> mapping = new HashMap<>();
        private static final Map<String, OpCodes> mnemonic_mapping = new HashMap<>();

        protected final short code;
        protected final String disassembleCode;
        protected final int opCodeParam;
        protected final boolean addressOpCode;

        OpCodes(int code, String disassembleCode, int opCodeParam) {
            this(code, disassembleCode, opCodeParam, false);
        }

        OpCodes(int code, String disassembleCode, int opCodeParam, boolean addressOpCode) {
            this.code = (short) code;
            this.disassembleCode = disassembleCode;
            this.opCodeParam = opCodeParam;
            this.addressOpCode = addressOpCode;
        }

        public static OpCodes get(short opCode) {
            if (mapping.isEmpty()) {
                for (OpCodes opCodes : OpCodes.values()) {
                    mapping.put(opCodes.code, opCodes);
                }
            }
            return mapping.get(opCode);
        }

        public static OpCodes getByMnemonic(String mnemonic) {
            if (mnemonic_mapping.isEmpty()) {
                for (OpCodes opCodes : OpCodes.values()) {
                    mnemonic_mapping.put(opCodes.disassembleCode(), opCodes);
                }
            }
            return mnemonic_mapping.get(mnemonic);
        }

        public short code() {
            return code;
        }

        public String disassembleCode() {
            return disassembleCode;
        }

        public int opCodeParam() {
            return opCodeParam;
        }

        public boolean isAddressOpCode() {
            return addressOpCode;
        }
    }
}
