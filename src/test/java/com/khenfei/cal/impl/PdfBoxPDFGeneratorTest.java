package com.khenfei.cal.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khenfei.cal.exception.MissingData;
import com.khenfei.cal.exception.PDFGeneratorException;
import com.khenfei.cal.model.AncestorLabel;
import com.khenfei.cal.model.JSONStringEnable;

public class PdfBoxPDFGeneratorTest {
	private static File fontFile;
	private static File imageFile;
	private static List<JSONStringEnable> data;
	@ClassRule
	public static TemporaryFolder testFolder = new TemporaryFolder();

	@BeforeClass
	public static void executedOnceBeforeAll() throws IOException {
		populateFontFile();
		populateImageFile();
		populateData();		
	}

	private static boolean populateFontFile() throws IOException {
		try (InputStream iStream = PdfBoxPDFGeneratorTest.class.getResourceAsStream("/font/gkai00mp.ttf");) {
			File tmp = testFolder.newFile("font.ttf.tmp");
			Files.copy(iStream, Paths.get(tmp.getPath()), StandardCopyOption.REPLACE_EXISTING);
			fontFile = tmp;
			return true;
		}
	}
	
	private static boolean populateImageFile() throws IOException {
		try (InputStream iStream = PdfBoxPDFGeneratorTest.class.getResourceAsStream("/image/plate.png");) {
			File tmp = testFolder.newFile("image.tmp.png");
			Files.copy(iStream, Paths.get(tmp.getPath()), StandardCopyOption.REPLACE_EXISTING);
			imageFile = tmp;
			return true;
		}
	}

	private static boolean populateData() throws JsonParseException, JsonMappingException, IOException {
		
		data = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		List<String> jsons = Arrays.asList(
			new StringBuilder("{'ancestor':{'value':['冤','亲','债','主']}")
			.append(",'requestor':{'value':['小','明']}")
			.append(",'number':{'value':['1']}}")
			.toString()
			, new StringBuilder("{'ancestor':{'value':['冤','亲','债','主']}")
			.append(",'requestor':{'value':['诸','葛','孔','明']}")
			.append(",'number':{'value':['1','0']}}")
			.toString()
			, new StringBuilder("{'ancestor':{'value':['冤','亲','债','主']}")
			.append(",'requestor':{'value':['小','白']}")
			.append(",'number':{'value':['9','9','9']}}")
			.toString());
		
		for (String json : jsons) {
			data.add(objMapper.readValue(json, AncestorLabel.class));
		}
		return true;
	}
	
	@Test
	public void testData_GivenNullValue_ExpectNoException() {
		new PdfBoxPDFGenerator(fontFile, imageFile, null).data(null);
	}

	@Test(expected = MissingData.class)
	public void testExecute_skipCallingData_ExpectNoException() throws PDFGeneratorException, IOException {
		File output = testFolder.newFile("output.pdf");
		new PdfBoxPDFGenerator(fontFile, imageFile, null).data(null).execute(new FileOutputStream(output));
		output.delete();
	}

	@Test
	public void testExecute_GivenValidValue_ExpectPdfCreation() throws PDFGeneratorException, IOException {
		File output = testFolder.newFile("output.pdf");
		double initialSize = output.length();
		new PdfBoxPDFGenerator(fontFile, imageFile, null).data(data).execute(new FileOutputStream(output));
		double finalSize = output.length();
		Assert.assertTrue(finalSize != initialSize);
		output.delete();
	}
}
