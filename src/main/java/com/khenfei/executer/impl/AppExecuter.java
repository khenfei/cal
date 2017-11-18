package com.khenfei.executer.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.khenfei.cal.PDFGenerator;
import com.khenfei.cal.SourceProcessor;
import com.khenfei.cal.impl.ExcelSourceProcessor;
import com.khenfei.cal.impl.PdfBoxPDFGenerator;
import com.khenfei.executer.Executer;

public class AppExecuter implements Executer {

	@Override
	public boolean execute(final Map<String, String> args) throws FileNotFoundException, IOException {
		if(args == null) {
			throw new IllegalArgumentException("Map object 'args' must not be null.");
		}
		
		final String inputFilename = args.get("src");
		final String outputFilename = args.get("out");

		if (StringUtils.isBlank(inputFilename)) {
			throw new IllegalArgumentException("Blank inputFilename detected. inputFilename must not be blank.");
		}
		String output = outputFilename;
		if (StringUtils.isBlank(output)) {
			log.warn("Missing output filename detected. Default output filename ({}) is used.", DEFAULT_OUTPUT);
			output = DEFAULT_OUTPUT;
		}
		
		final String font = args.get("font");
		if (StringUtils.isBlank(font)) {
			log.warn("Missing fontFile filename detected. Default fontFile filename ({}) is used.", "gkai00mp.ttf");
			try (InputStream iStream = this.getClass().getResourceAsStream("/font/gkai00mp.ttf");) {

				File tmp = File.createTempFile("font.ttf", ".tmp");
				Files.copy(iStream, Paths.get(tmp.getPath()), StandardCopyOption.REPLACE_EXISTING);
				fontFile = tmp;
			}
		} else {
			fontFile = new File(font);
		}
		try (InputStream iStream = this.getClass().getResourceAsStream("/image/plate.png");) {
			File tmp = File.createTempFile("image.tmp", ".png");
			Files.copy(iStream, Paths.get(tmp.getPath()), StandardCopyOption.REPLACE_EXISTING);
			imageFile = tmp;
		}
		File inputFile = new File(inputFilename);
		SourceProcessor sProcessor = sourceProcessor(inputFile);
		PDFGenerator pdfGenerator = pdfGenerator(fontFile, imageFile);
		return sProcessor.digest().print(pdfGenerator, output);
	}
	
	private SourceProcessor sourceProcessor(final File inputFile) {
		return new ExcelSourceProcessor(inputFile);
	}

	private PDFGenerator pdfGenerator(final File fontFile, final File imageFile) {
		return new PdfBoxPDFGenerator(fontFile, imageFile);
	}
	
	private File fontFile;
	private File imageFile;
	private final static String DEFAULT_OUTPUT = "output.pdf";
	private static final Logger log = LoggerFactory.getLogger(AppExecuter.class);

}
