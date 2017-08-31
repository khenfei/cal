package com.khenfei.cal.exception;

public class UnsupportedFileFormat extends SourceProcessorException{

	private static final long serialVersionUID = -2435209955150075750L;

	public UnsupportedFileFormat() {}

    public UnsupportedFileFormat(String message) {
        super(message);
    }

    public UnsupportedFileFormat(Throwable cause) {
        super(cause);
    }

    public UnsupportedFileFormat(String message, Throwable cause) {
        super(message, cause);
    }
}
