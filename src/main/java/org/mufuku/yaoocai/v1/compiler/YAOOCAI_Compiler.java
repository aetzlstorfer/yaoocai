package org.mufuku.yaoocai.v1.compiler;

import org.mufuku.yaoocai.v1.compiler.ast.ASTScript;
import org.mufuku.yaoocai.v1.compiler.parser.Parser;
import org.mufuku.yaoocai.v1.compiler.scanner.Scanner;
import org.mufuku.yaoocai.v1.compiler.translator.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class YAOOCAI_Compiler {

    private final InputStream in;
    private final OutputStream out;

    public YAOOCAI_Compiler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void compile() throws IOException {
        Scanner scanner = new Scanner(in);
        Parser parser = new Parser(scanner);
        ASTScript script = parser.parse();
        Translator translator = new Translator(script, out);
        translator.translate();
    }
}
