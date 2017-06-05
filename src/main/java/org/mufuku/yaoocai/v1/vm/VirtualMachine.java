package org.mufuku.yaoocai.v1.vm;

import java.io.PrintStream;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public interface VirtualMachine {
    PrintStream getOut();

    void setOut(PrintStream out);
}
