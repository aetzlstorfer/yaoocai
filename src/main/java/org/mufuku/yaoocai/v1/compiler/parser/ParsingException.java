package org.mufuku.yaoocai.v1.compiler.parser;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ParsingException extends RuntimeException {

    private final Integer lineNumber;

    public ParsingException(String message) {
        super(message);
        lineNumber = null;
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = null;
    }

    public ParsingException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " (line: " + lineNumber + ")";
    }
}
