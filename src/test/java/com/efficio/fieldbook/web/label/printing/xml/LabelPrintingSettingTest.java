
package com.efficio.fieldbook.web.label.printing.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.junit.Test;

public class LabelPrintingSettingTest {

	private static final Integer[] SELECTED_FIELDS_1 = {100, 300, 500, 700, 900};
	private static final Integer[] SELECTED_FIELDS_2 = {200, 400, 600, 800, 500};
	private static final Integer[] BARCODE_FIELDS = {1, 2, 3};

	@Test
	public void testPDFOutputXMLConfiguration() throws JAXBException {
		final String rightFieldsString = this.stringify(LabelPrintingSettingTest.SELECTED_FIELDS_2, PDFLabelPrintingSetting.DELIMITER);
		final String leftFieldsString = this.stringify(LabelPrintingSettingTest.SELECTED_FIELDS_1, PDFLabelPrintingSetting.DELIMITER);
		final String barcodeFieldsString = this.stringify(LabelPrintingSettingTest.BARCODE_FIELDS, BarcodeLabelPrintingSetting.DELIMITER);

		final PDFLabelPrintingSetting pdfSetting = new PDFLabelPrintingSetting("A4", 7, leftFieldsString, rightFieldsString);
		final BarcodeLabelPrintingSetting barcodeSetting = new BarcodeLabelPrintingSetting(true, "Barcode", barcodeFieldsString);
		final LabelPrintingSetting labelPrintingSetting = new LabelPrintingSetting("PDF setting", "PDF", null, pdfSetting, barcodeSetting,
				"stockId", "3");

		final JAXBContext context = JAXBContext.newInstance(LabelPrintingSetting.class);
		final Marshaller marshaller = context.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(labelPrintingSetting, writer);

		final String xmlToRead =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><labelPrintingSetting name=\"PDF setting\" "
						+ "outputType=\"PDF\" sorting = \"stockId\" numberOfCopies = \"3\" >"
						+ "<barcodeSetting barcodeFieldsString=\"" + barcodeFieldsString
						+ "\" barcodeFormat=\"Barcode\" barcodeNeeded=\"true\"/> "
						+ "<pdfSetting numberOfRowsPerPage=\"7\" selectedLeftFieldsString=\"" + leftFieldsString + "\" "
						+ "selectedRightFieldsString=\"" + rightFieldsString + "\" sizeOfLabelSheet=\"A4\" /></labelPrintingSetting>";

		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final LabelPrintingSetting parsedSetting = (LabelPrintingSetting) unmarshaller.unmarshal(new StringReader(xmlToRead));

		Assert.assertEquals(labelPrintingSetting, parsedSetting);
		Assert.assertEquals(Arrays.asList(LabelPrintingSettingTest.SELECTED_FIELDS_1), parsedSetting.getPdfSetting()
				.getSelectedLeftFieldsList());
		Assert.assertEquals(Arrays.asList(LabelPrintingSettingTest.SELECTED_FIELDS_2), parsedSetting.getPdfSetting()
				.getSelectedRightFieldsList());
		Assert.assertEquals(Arrays.asList(LabelPrintingSettingTest.BARCODE_FIELDS), parsedSetting.getBarcodeSetting()
				.getBarcodeFieldsList());

	}

	@Test
	public void testCSVExcelOutputXMLConfiguration() throws JAXBException {
		final String selectedFieldsString = this.stringify(LabelPrintingSettingTest.SELECTED_FIELDS_1, PDFLabelPrintingSetting.DELIMITER);
		final String barcodeFieldsString = this.stringify(LabelPrintingSettingTest.BARCODE_FIELDS, BarcodeLabelPrintingSetting.DELIMITER);

		final CSVExcelLabelPrintingSetting csvExcelSetting = new CSVExcelLabelPrintingSetting(false, selectedFieldsString);
		final BarcodeLabelPrintingSetting barcodeSetting = new BarcodeLabelPrintingSetting(true, "Barcode", barcodeFieldsString);
		final LabelPrintingSetting labelPrintingSetting = new LabelPrintingSetting("PDF setting", "PDF", csvExcelSetting, null, barcodeSetting, "stockId", "3");

		final JAXBContext context = JAXBContext.newInstance(LabelPrintingSetting.class);
		final Marshaller marshaller = context.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(labelPrintingSetting, writer);

		final String xmlToRead =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><labelPrintingSetting name=\"PDF setting\" "
						+ "outputType=\"PDF\" sorting = \"stockId\" numberOfCopies = \"3\" >"
						+ "<barcodeSetting barcodeFieldsString=\"" + barcodeFieldsString
						+ "\" barcodeFormat=\"Barcode\" barcodeNeeded=\"true\"/> "
						+ "<csvExcelSetting includeColumnHeadingsInOutput=\"false\" selectedFieldsString=\"" + selectedFieldsString
						+ "\"/>" + "</labelPrintingSetting>";

		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final LabelPrintingSetting parsedSetting = (LabelPrintingSetting) unmarshaller.unmarshal(new StringReader(xmlToRead));

		Assert.assertEquals(labelPrintingSetting, parsedSetting);
		Assert.assertEquals(Arrays.asList(LabelPrintingSettingTest.SELECTED_FIELDS_1), parsedSetting.getCsvExcelSetting()
				.getSelectedFieldsList());
		Assert.assertEquals(Arrays.asList(LabelPrintingSettingTest.BARCODE_FIELDS), parsedSetting.getBarcodeSetting()
				.getBarcodeFieldsList());
	}

	private String stringify(final Object[] values, final String delimiter) {
		final StringBuilder sb = new StringBuilder();
		for (final Object value : values) {
			if (sb.length() > 0) {
				sb.append(delimiter);
			}
			sb.append(value);
		}
		return sb.toString();
	}

}
