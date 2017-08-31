package com.khenfei.cal.exception;

public class MissingData extends PDFGeneratorException{

	private static final long serialVersionUID = -8430087572865556184L;

	public MissingData() {}

    public MissingData(String message) {
        super(message);
    }

    public MissingData(Throwable cause) {
        super(cause);
    }

    public MissingData(String message, Throwable cause) {
        super(message, cause);
    }
}