package org.mufuku.yaoocai.v1.assembler;

import org.mufuku.yaoocai.v1.assembler.ast.ASTAssemblerScript;
import org.mufuku.yaoocai.v1.assembler.parser.AssemblerParser;
import org.mufuku.yaoocai.v1.assembler.scanner.AssemblerScanner;
import org.mufuku.yaoocai.v1.assembler.translator.AssemblerTranslator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class YAOOCAI_AssemblerCompiler {

    private final InputStream in;
    private final OutputStream out;

    public YAOOCAI_AssemblerCompiler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void compile() throws IOException {
        AssemblerScanner scanner = new AssemblerScanner(in);
        AssemblerParser parser = new AssemblerParser(scanner);
        ASTAssemblerScript script = parser.parse();
        AssemblerTranslator translator = new AssemblerTranslator(script, out);
        translator.translate();
    }
}
