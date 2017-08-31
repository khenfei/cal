package com.khenfei.cal.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.khenfei.cal.PDFGenerator;
import com.khenfei.cal.SourceProcessor;
import com.khenfei.cal.exception.SourceProcessorException;
import com.khenfei.cal.exception.UnsupportedFileFormat;
import com.khenfei.cal.model.JSONStringEnable;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
@RunWith(JMockit.class)
public class ExcelSourceProcessorTest {

	private static String validXml = null;

	@ClassRule
	public static TemporaryFolder testFolder = new TemporaryFolder();

	@BeforeClass
	public static void executedOnceBeforeAll() throws IOException {
		populateExcelFile();
	}

	private static void populateExcelFile() throws IOException {
		File xmlFile = testFolder.newFile("valid.xlsx");
		try(XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet("Ancestor Label List");
			Object[][] datatypes = { 
					{ "Requestor", "Ancestor", "Number" }
					, { "小明", "冤亲债主", 1}
					, { "诸葛孔明", "冤亲债主", 2 }
					, { "张飞", "冤亲债主", 3 }
					, { "赵云", "冤亲债主", 4.0 }
					, { "关羽", "冤亲债主", "50" }
					, { "白起", "冤亲债主", 600 }
					, { "小白", "冤亲债主", "9999" } };
	
			int rowNum = 0;
			for (Object[] datatype : datatypes) {
				Row row = sheet.createRow(rowNum++);
				int colNum = 0;
				for (Object field : datatype) {
					Cell cell = row.createCell(colNum++);
					if (field instanceof String) {
						cell.setCellValue((String) field);
					} else if (field instanceof Integer) {
						cell.setCellValue((Integer) field);
					} else if (field instanceof Double) {
						cell.setCellValue((Double) field);
					}
				}
			}
			FileOutputStream outputStream = new FileOutputStream(xmlFile);
			workbook.write(outputStream);
		}
		validXml = xmlFile.getAbsolutePath();
	}
	@Test( expected=IllegalArgumentException.class )
	public void testDigest_GivenNullExcelFile_ExpectIllegalArgumentEx() 
			throws IOException, SourceProcessorException {
		new ExcelSourceProcessor(null).digest();
	}
	@Test( expected=UnsupportedFileFormat.class )
	public void testDigest_GivenInvalidExcelFile_ExpectIOException() 
			throws IOException, SourceProcessorException {
		File emptyFile = testFolder.newFile("emptyfile.txt");
		new ExcelSourceProcessor(emptyFile).digest();
	}
	@SuppressWarnings("unchecked")
	@Test
	public void testPrint_GivenValidExcelFile_ExpectPdfFileCreation(
			@Mocked PDFGenerator mock) 
			throws IOException, SourceProcessorException {
		File output = testFolder.newFile("output.pdf");
		new Expectations() {{
			mock.data((List<JSONStringEnable>) any); minTimes=1; result=mock;
			mock.execute((OutputStream) any); times=1; result=true;
		}};		
		new ExcelSourceProcessor(new File(validXml))
			.digest()
			.print(mock, output.getAbsolutePath());
		output.delete();
	}
	@SuppressWarnings("unchecked")
	@Test
	public void testPrint_SkipCallingDigest_ExpectEmptyEntries(
			@Mocked PDFGenerator mock) 
			throws IOException, SourceProcessorException {
		File output = testFolder.newFile("output.pdf");
		new Expectations() {{
			mock.data((List<JSONStringEnable>) any); result=mock;
			mock.execute((OutputStream) any); result=true;
		}};
		SourceProcessor sProcessor = new ExcelSourceProcessor(new File(validXml));
		sProcessor.print(mock, output.getAbsolutePath());
		output.delete();
		List<JSONStringEnable> entries = Deencapsulation.getField(sProcessor, "entries");
		Assert.assertEquals(0, entries.size());
	}
	@Test( expected=IllegalArgumentException.class )
	public void testPrint_GivenNullOutputFilePath_ExpectPdfFileCreation(
			@Mocked PDFGenerator mock) 
			throws IOException, SourceProcessorException {	
		new ExcelSourceProcessor(new File(validXml)).digest().print(mock, null);
	}
}
