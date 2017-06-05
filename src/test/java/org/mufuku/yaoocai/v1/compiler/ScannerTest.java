package org.mufuku.yaoocai.v1.compiler;

import org.junit.Test;
import org.mufuku.yaoocai.v1.compiler.ast.ASTScript;
import org.mufuku.yaoocai.v1.compiler.parser.Parser;
import org.mufuku.yaoocai.v1.compiler.scanner.Scanner;
import org.mufuku.yaoocai.v1.compiler.scanner.ScannerSymbols;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ScannerTest {

    @Test
    public void test_oneLineComment_emptyScript() throws IOException {
        InputStream in = new ByteArrayInputStream("//test something".getBytes());
        Scanner scanner = new Scanner(in);
        Parser parser = new Parser(scanner);
        ASTScript script = parser.parse();
        assertThat(script.functions().hasNext(), is(false));
    }

    @Test
    public void test_oneLineBlockComment_emptyScript() throws IOException {
        InputStream in = new ByteArrayInputStream("/*test */".getBytes());
        Scanner scanner = new Scanner(in);
        Parser parser = new Parser(scanner);
        ASTScript script = parser.parse();
        assertThat(script.functions().hasNext(), is(false));
    }

    @Test
    public void test_unknownCharacters_UNKNOWN_symbol() throws IOException {
        InputStream in = new ByteArrayInputStream("@".getBytes());
        Scanner scanner = new Scanner(in);
        scanner.initialize();
        assertThat(scanner.getCurrentSymbol(), is(equalTo(ScannerSymbols.UNKNOWN)));
    }

}
