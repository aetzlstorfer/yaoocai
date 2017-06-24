package org.mufuku.yaoocai.v1.bytecode;

import java.util.*;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
@SuppressWarnings("squid:S1214")
public interface InstructionSet {

    String PREAMBLE = "yaoocai";
    byte MAJOR_VERSION = 1;
    byte MINOR_VERSION = 1;

    enum OpCodes {

        // 1. constants
        // 1.1 boolean
        B_CONST_TRUE(0x00, "b_const_true", 0),
        B_CONST_FALSE(0x01, "b_const_false", 0),
        // 1.2 integer
        I_CONST_0(0x02, "i_const_0", 0),
        I_CONST_1(0x03, "i_const_1", 0),
        // 1.3 constant pool
        CONST_P1B(0x04, "cp1b", 1),
        // restructure necessary: CONST_P2B(0x05, "cp2b", 2),

        // 2. stack
        STORE(0x06, "store", 1),
        LOAD(0x07, "load", 1),
        POP(0x08, "pop", 0),

        // 3. invoke
        INVOKE(0x09, "invoke", 2),
        INVOKE_BUILTIN(0x0A, "invoke_builtin", 2),

        // 4. arithmetical operators
        I_ADD(0x0B, "i_add", 0),
        I_SUB(0x0C, "i_sub", 0),
        I_MUL(0x0D, "i_mul", 0),
        I_DIV(0x0E, "i_div", 0),
        I_MOD(0x0F, "i_mod", 0),
        I_NEG(0x10, "i_neg", 0),

        // 5. compare operators
        I_CMP_LT(0x11, "i_cmp_lt", 0),
        I_CMP_LTE(0x12, "i_cmp_lte", 0),
        I_CMP_GT(0x13, "i_cmp_gt", 0),
        I_CMP_GTE(0x14, "i_cmp_gte", 0),
        I_CMP_EQ(0x15, "i_cmp_eq", 0),
        I_CMP_NE(0x16, "i_cmp_ne", 0),

        // 6. control structure
        IF(0x17, "if", 1, true),
        GOTO(0x18, "goto", 1, true),
        RETURN(0x19, "return", 0),

        // 7. bitwise operations
        AND(0x20, "and", 0),
        OR(0x21, "or", 0),
        NOT(0x22, "not", 0);

        public static final Set<OpCodes> LOCAL_VARIABLES_TABLE_AWARE = Collections.unmodifiableSet(EnumSet.of(
                LOAD,
                STORE
        ));
        public static final Set<OpCodes> CONSTANT_POOL_SINGLE = Collections.unmodifiableSet(EnumSet.of(
                CONST_P1B
        ));
        public static final Set<OpCodes> CONSTANT_POOL_WIDE = Collections.unmodifiableSet(EnumSet.of(
//                CONST_P2B,
                INVOKE,
                INVOKE_BUILTIN
        ));
        private static final Map<Byte, OpCodes> mapping = new HashMap<>();
        private static final Map<String, OpCodes> mnemonic_mapping = new HashMap<>();
        final byte code;
        final String disassembleCode;
        final int opCodeParam;
        final boolean addressOpCode;

        OpCodes(int code, String disassembleCode, int opCodeParam) {
            this(code, disassembleCode, opCodeParam, false);
        }

        OpCodes(int code, String disassembleCode, int opCodeParam, boolean addressOpCode) {
            this.code = (byte) code;
            this.disassembleCode = disassembleCode;
            this.opCodeParam = opCodeParam;
            this.addressOpCode = addressOpCode;
        }

        public static OpCodes get(byte opCode) {
            if (mapping.isEmpty()) {
                for (OpCodes opCodes : OpCodes.values()) {
                    mapping.put(opCodes.code, opCodes);
                }
            }
            return mapping.computeIfAbsent(opCode, k -> {
                throw new IllegalStateException("Invalid op code: " + opCode);
            });
        }

        public static OpCodes getByMnemonic(String mnemonic) {
            if (mnemonic_mapping.isEmpty()) {
                for (OpCodes opCodes : OpCodes.values()) {
                    mnemonic_mapping.put(opCodes.disassembleCode(), opCodes);
                }
            }
            return mnemonic_mapping.get(mnemonic);
        }

        public byte code() {
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
