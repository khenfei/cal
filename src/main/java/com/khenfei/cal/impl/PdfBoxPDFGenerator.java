package com.khenfei.cal.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khenfei.cal.PDFGenerator;
import com.khenfei.cal.exception.MissingData;
import com.khenfei.cal.exception.PDFGeneratorException;
import com.khenfei.cal.model.AncestorLabel;
import com.khenfei.cal.model.Field;
import com.khenfei.cal.model.JSONStringEnable;

public class PdfBoxPDFGenerator implements PDFGenerator {

	public PdfBoxPDFGenerator(final File fontFile, final File imageFile) {
		this.fontFile = fontFile;
		this.imageFile = imageFile;
	}

	@Override
	public PDFGenerator data(List<JSONStringEnable> source) {
		this.source = source;
		return this;
	}

	@Override
	public boolean execute(OutputStream oStream) throws IOException, PDFGeneratorException {
		if (this.source == null) {
			throw new MissingData(
					new StringBuilder("Missing data detected. ").append("Please supply data value with data() method. ")
							.append("Value must not be null").toString());
		}
		List<AncestorLabel> records = digest(this.source);
		if (log.isDebugEnabled()) {
			log.debug("Number of records: {}", records.size());
		}
		PDDocument doc = new PDDocument();
		this.font = PDType0Font.load(doc, this.fontFile);
		renderPage(doc, records);
		doc.save(oStream);
		doc.close();
		return true;
	}

	private List<AncestorLabel> digest(List<JSONStringEnable> jsonObject)
			throws JsonParseException, JsonMappingException, IOException {
		final List<AncestorLabel> records = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		for (JSONStringEnable json : jsonObject) {
			records.add(objMapper.readValue(json.toJSON(), AncestorLabel.class));
		}
		return records;
	}

	private boolean renderPage(final PDDocument doc, List<AncestorLabel> records) throws IOException {

		PDPage page = null;
		final float columnModifier = new Float(59.3);
		final float maxColumnSize = columnModifier * new Float(4);
		int column = 0;
		for (int i = 0; i < records.size(); i++) {
			column = i % 5;
			if (column == 0) {
				page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
				doc.addPage(page);
				renderBackground(doc, page);
			}
			renderColumn(doc, page, records.get(i), point(maxColumnSize - (column * columnModifier)), point(0));
		}
		return true;
	}
	
	private PdfBoxPDFGenerator renderBackground(final PDDocument doc, final PDPage page) throws IOException {
		PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), doc);
		PDPageContentStream contents = new PDPageContentStream(doc, page);
		contents.drawImage(pdImage, 0, 0, PAGE_WIDTH, PAGE_HEIGHT);
		contents.close();
		return this;
	}

	private float point(final double value) {
		return new Float(value * POINTS_PER_MM);
	}

	private PdfBoxPDFGenerator renderColumn(final PDDocument doc, final PDPage page, final AncestorLabel aLabel,
			final float xAxis, final float yAxis) throws IOException {

		renderField(doc, page, aLabel.requestor(), xAxis + point(4.5), yAxis, 15, VerticalAlignment.REQUESTOR);
		renderField(doc, page, aLabel.ancestor(), xAxis + point(27), yAxis, 20, VerticalAlignment.ANCESTOR);
		renderField(doc, page, aLabel.number(), xAxis + point(53), yAxis, 10, VerticalAlignment.SEQUENCE);
		return this;
	}

	private PdfBoxPDFGenerator renderField(final PDDocument doc, final PDPage page, final Field field,
			final float xAxis, final float yAxis, final float fontSize, final VerticalAlignment verticalAlignment)
			throws IOException {

		return renderCharacter(doc, page, field.value(), xAxis, yAxis, fontSize, verticalAlignment);
	}

	private PdfBoxPDFGenerator renderCharacter(final PDDocument doc, final PDPage page, final List<Character> text,
			final float xAxis, final float yAxis, final float fontSize, final VerticalAlignment verticalAlignment)
			throws IOException {

		final List<Character> characters = new ArrayList<>(text);
		final PDType0Font font = this.font;
		final float fontLeading = fontSize * new Float(1.1);
		final float fontHeight = new Float(1) * fontLeading;
		final float textHeightLength = fontHeight * characters.size();
		final float height = height(textHeightLength, verticalAlignment);
		try (PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
				true)) {

			stream.beginText();
			stream.setFont(font, fontSize);
			stream.setLeading(fontLeading);
			stream.newLineAtOffset(xAxis, height);
			for (Character c : characters) {
				stream.newLine();
				try {
					stream.showText(Character.toString(c));
				} catch(IllegalArgumentException iae) {
					if(StringUtils.isNotEmpty(iae.getMessage()) && iae.getMessage().contains("No glyph for")) {
						log.error("No glyph for this character '"+c.toString()+"'");
					}
					throw iae;
				}
			}
			stream.endText();
		}
		return this;
	}

	private float height(final float textHeightLength, final VerticalAlignment verticalAlignment) {

		switch (verticalAlignment) {
		case ANCESTOR:
			return PAGE_HEIGHT - new Float(120);
		case REQUESTOR:
			return PAGE_HEIGHT - new Float(220);
		case SEQUENCE:
			return PAGE_HEIGHT - new Float(300);
		default:
			return new Float(0);
		}
	}

	private File fontFile;
	private File imageFile;
	private PDType0Font font;
	private List<JSONStringEnable> source;
	private static final float POINTS_PER_INCH = 72;
	private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
	private static final float PAGE_HEIGHT = 210 * POINTS_PER_MM;
	private static final float PAGE_WIDTH = 297 * POINTS_PER_MM;

	private static enum VerticalAlignment {
		ANCESTOR, REQUESTOR, SEQUENCE;
	}

	private final Logger log = LoggerFactory.getLogger(PdfBoxPDFGenerator.class);

}
