package com.khenfei.cal.exception;

public class SourceProcessorException extends RuntimeException {

	private static final long serialVersionUID = 5308809361079439933L;

	public SourceProcessorException() {}

    public SourceProcessorException(String message) {
        super(message);
    }

    public SourceProcessorException(Throwable cause) {
        super(cause);
    }

    public SourceProcessorException	(String message, Throwable cause) {
        super(message, cause);
    }
}
