package com.khenfei.cal.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.khenfei.cal.PDFGenerator;
import com.khenfei.cal.SourceProcessor;
import com.khenfei.cal.exception.SourceProcessorException;
import com.khenfei.cal.exception.UnsupportedFileFormat;
import com.khenfei.cal.model.AncestorLabel;
import com.khenfei.cal.model.JSONStringEnable;

public class ExcelSourceProcessor implements SourceProcessor {

	public ExcelSourceProcessor(File excelFile) {
		this.excelFile = excelFile;
	}

	public SourceProcessor digest() throws IOException, SourceProcessorException {
		if (excelFile == null) {
			throw new IllegalArgumentException("Missing excel File detected. Excel file must not be null.");
		}
		entries = new ArrayList<>();
		try (FileInputStream excelInputStream = new FileInputStream(excelFile);
				XSSFWorkbook workbook = new XSSFWorkbook(excelInputStream)) {
			Sheet datatypeSheet = workbook.getSheetAt(0);
			for (int i = 1; i < datatypeSheet.getLastRowNum() + 1; i++) {
				Row currentRow = datatypeSheet.getRow(i);
				final String requestorName = new CellSanitizer(
						currentRow.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK)).toString();
				final String ancestorName = new CellSanitizer(
						currentRow.getCell(1, MissingCellPolicy.CREATE_NULL_AS_BLANK)).toString();
				final int number = new CellSanitizer(currentRow.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK))
						.toInteger();
				entries.add(new AncestorLabel.AncestorLabelBuilder(number).requestor(requestorName)
						.ancestor(ancestorName).build());
			}
		} catch (UnsupportedFileFormatException e) {
			throw new UnsupportedFileFormat(e);
		}

		if (log.isDebugEnabled()) {
			entries.stream().forEach(x -> log.debug(x.toJSON()));
		}
		return this;
	}

	public boolean print(final PDFGenerator generator, final String outputFilename) throws IOException {

		if (StringUtils.isBlank(outputFilename)) {
			throw new IllegalArgumentException("A valid outputFilename (String) is required. "
					+ "Value of outputFilename must not be null or empty.");
		}
		try (OutputStream oStream = new FileOutputStream(new File(outputFilename))) {
			return generator.data(this.entries).execute(oStream);
		}
	}

	private List<JSONStringEnable> entries = Collections.emptyList();
	private final File excelFile;
	private final Logger log = LoggerFactory.getLogger(ExcelSourceProcessor.class);

	static class CellSanitizer {
		private Cell cell;

		public CellSanitizer(Cell cell) {
			this.cell = cell;
		}

		private static final int INVALID_VALUE = -1;

		public String toString() {
			if (cell == null) {
				return StringUtils.EMPTY;
			}
			switch (cell.getCellTypeEnum()) {
			case STRING:
				return cell.getStringCellValue();
			case NUMERIC:
				return sanitize(cell.getNumericCellValue());
			default:
				return StringUtils.EMPTY;
			}
		}

		public int toInteger() {
			if (cell == null) {
				return INVALID_VALUE;
			}
			switch (cell.getCellTypeEnum()) {
			case STRING:
				return sanitize2Int(cell.getStringCellValue());
			case NUMERIC:
				return sanitize2Int(cell.getNumericCellValue());
			default:
				return INVALID_VALUE;
			}
		}

		private int sanitize2Int(String value) {
			if (NumberUtils.isCreatable(value)) {
				return Integer.parseInt(value);
			}
			return INVALID_VALUE;
		}

		private int sanitize2Int(Double value) {
			if (value != null) {
				return value.intValue();
			}
			return INVALID_VALUE;
		}

		private String sanitize(Double value) {
			if (value != null) {
				return String.valueOf(value.intValue());
			}
			return StringUtils.EMPTY;
		}
	}
}
