package com.khenfei;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.khenfei.cal.PDFGenerator;
import com.khenfei.cal.SourceProcessor;
import com.khenfei.cal.impl.ExcelSourceProcessor;
import com.khenfei.cal.impl.PdfBoxPDFGenerator;

public class App {
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Option.builder("i").desc("Input file. Specify excel (xlsx) file path").hasArg()
				.argName("file").required().optionalArg(false).build());
		options.addOption(Option.builder("o").desc("Output file. Specify output file path").hasArg().argName("file")
				.required(false).optionalArg(false).build());
		options.addOption(Option.builder("f").desc("Font file. Specify custom True Type Font (TTF) file path").hasArg()
				.argName("file").required(false).optionalArg(false).build());
		options.addOption(Option.builder("h").longOpt("help").desc("Print this help message").hasArg(false)
				.required(false).build());

		HelpFormatter formatter = new HelpFormatter();
		final String commandTemplate = "cal -i xlsxfile -o output.pdf [-f fontfile]";
		try {
			if (args.length < 1) {
				formatter.printHelp(commandTemplate, options);
				return;
			}
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				formatter.printHelp(commandTemplate, options);
				return;
			}
			File font = null;
			if (line.hasOption("f")) {
				font = new File(line.getOptionValue("f"));
			}
			final String src = line.getOptionValue("i");
			final String out = line.getOptionValue("o");
			if (new App(font).execute(src, out)) {
				log.info("File is generated successfully.");
			}
		} catch (ParseException | IOException e) {
			log.error(e.getMessage(), e);
		}

	}

	public App(File fontfile) {
		this.fontFile = fontfile;
	}

	public boolean execute(final String inputFilename, final String outputFilename)
			throws FileNotFoundException, IOException {

		if (StringUtils.isBlank(inputFilename)) {
			throw new IllegalArgumentException("Blank inputFilename detected. inputFilename must not be blank.");
		}
		String output = outputFilename;
		if (StringUtils.isBlank(output)) {
			log.warn("Missing output filename detected. Default output filename ({}) is used.", DEFAULT_OUTPUT);
			output = DEFAULT_OUTPUT;
		}
		if (fontFile == null) {
			log.warn("Missing fontFile filename detected. Default fontFile filename ({}) is used.", "gkai00mp.ttf");
			try (InputStream iStream = this.getClass().getResourceAsStream("/font/gkai00mp.ttf");) {

				File tmp = File.createTempFile("font.ttf", ".tmp");
				Files.copy(iStream, Paths.get(tmp.getPath()), StandardCopyOption.REPLACE_EXISTING);
				fontFile = tmp;
			}
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
	private static final Logger log = LoggerFactory.getLogger(App.class);
}
