package org.mufuku.yaoocai.v1.assembler.translator;

import org.mufuku.yaoocai.v1.assembler.ast.ASTAssemblerFunction;
import org.mufuku.yaoocai.v1.assembler.ast.ASTAssemblerScript;
import org.mufuku.yaoocai.v1.bytecode.BasicByteCodeProducer;
import org.mufuku.yaoocai.v1.bytecode.InstructionSet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class AssemblerTranslator extends BasicByteCodeProducer {

    private final ASTAssemblerScript script;

    public AssemblerTranslator(ASTAssemblerScript script, OutputStream out) {
        super(out);
        this.script = script;
    }

    public void translate() throws IOException {
        emitHeader(script.getMajorVersion(), script.getMinorVersion(), script.getMainFunctionIndex());
        for (ASTAssemblerFunction function : script) {
            writeOpCode(InstructionSet.OpCodes.FUNCTION);
            writeFunction(function.getInstructions());
        }
    }

    private void writeFunction(List<Short> instructions) throws IOException {
        for (Short instruction : instructions) {
            out.writeShort(instruction);
        }
    }
}
