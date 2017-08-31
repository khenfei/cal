package com.khenfei.cal;

import java.io.IOException;

import com.khenfei.cal.exception.SourceProcessorException;

public interface SourceProcessor {
	SourceProcessor digest() throws IOException, SourceProcessorException;
	boolean print(final PDFGenerator pdfGenerator, final String outputFilename) throws IOException;
}
