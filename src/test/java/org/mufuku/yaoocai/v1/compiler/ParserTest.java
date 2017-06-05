package org.mufuku.yaoocai.v1.compiler;

import org.junit.Test;
import org.mufuku.yaoocai.v1.compiler.parser.Parser;
import org.mufuku.yaoocai.v1.compiler.parser.ParsingException;
import org.mufuku.yaoocai.v1.compiler.scanner.Scanner;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ParserTest {

    @Test(expected = ParsingException.class)
    public void test_invalidIO_parsingException() throws IOException {
        InputStream emptyIn = new FailingInputStream();
        Scanner scanner = new Scanner(emptyIn);
        Parser parser = new Parser(scanner);
        parser.parse();
    }

    private static class FailingInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            throw new IOException("Test");
        }
    }

}