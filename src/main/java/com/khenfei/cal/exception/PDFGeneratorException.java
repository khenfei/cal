package com.khenfei.cal.exception;

public class PDFGeneratorException extends RuntimeException {

	private static final long serialVersionUID = -2668913100357483283L;

	public PDFGeneratorException() {}

    public PDFGeneratorException(String message) {
        super(message);
    }

    public PDFGeneratorException(Throwable cause) {
        super(cause);
    }

    public PDFGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
