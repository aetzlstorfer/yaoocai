package org.mufuku.yaoocai.v1.vm;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public interface VirtualMachine {

    void execute() throws IOException;

    PrintStream getOut();

    void setOut(PrintStream out);
}
