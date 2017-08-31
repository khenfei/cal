package com.khenfei.cal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.khenfei.cal.exception.PDFGeneratorException;
import com.khenfei.cal.model.JSONStringEnable;

public interface PDFGenerator {
	PDFGenerator data(final List<JSONStringEnable> source);
	boolean execute(OutputStream oStream) 
			throws IOException, PDFGeneratorException;
}
