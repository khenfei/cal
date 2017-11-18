package com.khenfei;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.khenfei.executer.Executer;

public class Console {
	public Console(Executer executer) {
		this.executer = executer;		
	}
	
	public boolean run(final String[] args) {
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
				return false;
			}
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				formatter.printHelp(commandTemplate, options);
				return false;
			}
			
			final String src = line.getOptionValue("i");
			final String out = line.getOptionValue("o");
			final Map<String, String> execArgs = new HashMap<>();
			execArgs.put("src", src);
			execArgs.put("out", out);
			
			if (line.hasOption("f")) {
				execArgs.put("font", line.getOptionValue("f"));
			}
			
			if (executer.execute(execArgs)) {
				log.info("File is generated successfully.");
			}
			return true;
		} catch (ParseException | IOException e) {
			log.error(e.getMessage(), e);
		} 
		return false;
	}
	
	private final Executer executer;
	private static final Logger log = LoggerFactory.getLogger(Console.class);
}
