package com.khenfei.cal.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

	public PdfBoxPDFGenerator(final File fontFile, final File imageFile, final Properties properties) {
		this.fontFile = fontFile;
		this.imageFile = imageFile;
		config(properties);
	}
	
	private void config(final Properties properties) {
		if (properties == null) {
			if (log.isDebugEnabled()) {
				log.debug("properties object is null.");
			}
			return;
		}
		final float DEFAULT_AXIS = new Float("0");
		final String ancestorXAxisDelta = "ancestor.x.delta";
		this.ancestorXAxisDelta = getFloat(ancestorXAxisDelta, properties.getProperty(ancestorXAxisDelta), DEFAULT_AXIS);
		
		final String requestorXAxisDelta = "requestor.x.delta";
		this.requestorXAxisDelta = getFloat(requestorXAxisDelta, properties.getProperty(requestorXAxisDelta), DEFAULT_AXIS);
		
		final String sequenceXAxisDelta = "sequence.x.delta";
		this.sequenceXAxisDelta = getFloat(sequenceXAxisDelta, properties.getProperty(sequenceXAxisDelta), DEFAULT_AXIS);
		
		final String ancestorYAxisDelta = "ancestor.y.delta";
		this.ancestorYAxisDelta = getFloat(ancestorYAxisDelta, properties.getProperty(ancestorYAxisDelta), DEFAULT_AXIS);
		
		final String requestorYAxisDelta = "requestor.y.delta";
		this.requestorYAxisDelta = getFloat(requestorYAxisDelta, properties.getProperty(requestorYAxisDelta), DEFAULT_AXIS);
		
		final String sequenceYAxisDelta = "sequence.y.delta";
		this.sequenceYAxisDelta = getFloat(sequenceYAxisDelta, properties.getProperty(sequenceYAxisDelta), DEFAULT_AXIS);
		
		final String ancestorCharsThresholdXAxisDelta = "ancestor.chars.threshold.x.delta";
		this.ancestorCharsThresholdXAxisDelta = 
				getFloat(ancestorCharsThresholdXAxisDelta, 
						properties.getProperty(ancestorCharsThresholdXAxisDelta), 
						DEFAULT_AXIS);
		
		final String ancestorCharsSize = "ancestor.chars.size";
		this.ancestorCharsSize = 
				getInt(ancestorCharsSize, 
						properties.getProperty(ancestorCharsSize), 
						this.ancestorCharsSize);
		
		final String ancestorCharsThresholdSize = "ancestor.chars.threshold.size";
		this.ancestorCharsThresholdSize = 
				getInt(ancestorCharsThresholdSize, 
						properties.getProperty(ancestorCharsThresholdSize), 
						this.ancestorCharsThresholdSize);
		
		final String ancestorCharsThreshold = "ancestor.chars.threshold";
		this.ancestorCharsThreshold = 
				getInt(ancestorCharsThreshold, 
						properties.getProperty(ancestorCharsThreshold), 
						this.ancestorCharsThreshold);
	}
	
	private float getFloat(final String key, final String value, final float defaultValue) {
		if (NumberUtils.isCreatable(value)) {
			return Float.parseFloat(value);			
		} 
		log.warn("Properties '{}' is not a valid numeric. Default value ({}) will be used.", key, defaultValue);
		return defaultValue;		
	}
	
	private int getInt(final String key, final String value, final int defaultValue) {
		if (NumberUtils.isCreatable(value)) {
			return Integer.parseInt(value);			
		} 
		log.warn("Properties '{}' is not a valid numeric. Default value ({}) will be used.", key, defaultValue);
		return defaultValue;		
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
		
		final boolean isThresholdReached = aLabel.ancestor().value().size() > this.ancestorCharsThreshold; 
		final float _ancestorXAxisDelta = isThresholdReached? this.ancestorCharsThresholdXAxisDelta : this.ancestorXAxisDelta;
		final float ancestorSize = isThresholdReached? this.ancestorCharsThresholdSize : this.ancestorCharsSize;
		
		final float requestorXAxis = xAxis + point(4.5) + this.requestorXAxisDelta;
		final float ancestorXAxis = xAxis + point(24) + _ancestorXAxisDelta;
		final float sequenceXAxis = xAxis + point(53) + this.sequenceXAxisDelta;
						
		renderField(doc, page, aLabel.requestor(), requestorXAxis, yAxis, 15, VerticalAlignment.REQUESTOR);
		renderField(doc, page, aLabel.ancestor(), ancestorXAxis, yAxis, ancestorSize, VerticalAlignment.ANCESTOR);
		renderField(doc, page, aLabel.number(), sequenceXAxis, yAxis, 10, VerticalAlignment.SEQUENCE);
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
		final float height = height(yAxis, verticalAlignment);
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
					} else {
						throw iae;
					}
				}
			}
			stream.endText();
		}
		return this;
	}

	private float height(final float yAxis, final VerticalAlignment verticalAlignment) {

		switch (verticalAlignment) {
		case ANCESTOR:
			return PAGE_HEIGHT - new Float(120) + yAxis + ancestorYAxisDelta;
		case REQUESTOR:
			return PAGE_HEIGHT - new Float(220) + yAxis + requestorYAxisDelta;
		case SEQUENCE:
			return PAGE_HEIGHT - new Float(300) + yAxis + sequenceYAxisDelta;
		default:
			return yAxis;
		}
	}

	private float ancestorYAxisDelta = new Float(0);
	private float requestorYAxisDelta = new Float(0);
	private float sequenceYAxisDelta = new Float(0);
	private float ancestorXAxisDelta = new Float(0);
	private float requestorXAxisDelta = new Float(0);
	private float sequenceXAxisDelta = new Float(0);
	private float ancestorCharsThresholdXAxisDelta = new Float(0);
	private int ancestorCharsThreshold = 5;
	private int ancestorCharsSize = 30;
	private int ancestorCharsThresholdSize = 20;
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
