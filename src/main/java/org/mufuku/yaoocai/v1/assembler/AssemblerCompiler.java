package org.mufuku.yaoocai.v1.assembler;

import org.mufuku.yaoocai.v1.assembler.parser.AssemblerParser;
import org.mufuku.yaoocai.v1.assembler.scanner.AssemblerScanner;
import org.mufuku.yaoocai.v1.bytecode.ByteCodeWriter;
import org.mufuku.yaoocai.v1.bytecode.data.BCFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class AssemblerCompiler {

    private final InputStream in;
    private final OutputStream out;

    public AssemblerCompiler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void compile() throws IOException {
        AssemblerScanner scanner = new AssemblerScanner(in);
        AssemblerParser parser = new AssemblerParser(scanner);
        BCFile file = parser.parse();
        ByteCodeWriter byteCodeWriter = new ByteCodeWriter(out);
        byteCodeWriter.writeByteCode(file);
    }
}
