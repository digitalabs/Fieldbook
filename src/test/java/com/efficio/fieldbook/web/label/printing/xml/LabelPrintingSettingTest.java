
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
		String rightFieldsString = this.stringify(LabelPrintingSettingTest.SELECTED_FIELDS_2, PDFLabelPrintingSetting.DELIMITER);
		String leftFieldsString = this.stringify(LabelPrintingSettingTest.SELECTED_FIELDS_1, PDFLabelPrintingSetting.DELIMITER);
		String barcodeFieldsString = this.stringify(LabelPrintingSettingTest.BARCODE_FIELDS, BarcodeLabelPrintingSetting.DELIMITER);

		PDFLabelPrintingSetting pdfSetting = new PDFLabelPrintingSetting("A4", 7, leftFieldsString, rightFieldsString);
		BarcodeLabelPrintingSetting barcodeSetting = new BarcodeLabelPrintingSetting(true, "Barcode", barcodeFieldsString);
		LabelPrintingSetting labelPrintingSetting = new LabelPrintingSetting("PDF setting", "PDF", null, pdfSetting, barcodeSetting);

		JAXBContext context = JAXBContext.newInstance(LabelPrintingSetting.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(labelPrintingSetting, writer);

		String xmlToRead =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><labelPrintingSetting name=\"PDF setting\" outputType=\"PDF\">"
						+ "<barcodeSetting barcodeFieldsString=\"" + barcodeFieldsString
						+ "\" barcodeFormat=\"Barcode\" barcodeNeeded=\"true\"/> "
						+ "<pdfSetting numberOfRowsPerPage=\"7\" selectedLeftFieldsString=\"" + leftFieldsString + "\" "
						+ "selectedRightFieldsString=\"" + rightFieldsString + "\" sizeOfLabelSheet=\"A4\" /></labelPrintingSetting>";

		Unmarshaller unmarshaller = context.createUnmarshaller();
		LabelPrintingSetting parsedSetting = (LabelPrintingSetting) unmarshaller.unmarshal(new StringReader(xmlToRead));

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
		String selectedFieldsString = this.stringify(LabelPrintingSettingTest.SELECTED_FIELDS_1, PDFLabelPrintingSetting.DELIMITER);
		String barcodeFieldsString = this.stringify(LabelPrintingSettingTest.BARCODE_FIELDS, BarcodeLabelPrintingSetting.DELIMITER);

		CSVExcelLabelPrintingSetting csvExcelSetting = new CSVExcelLabelPrintingSetting(false, selectedFieldsString);
		BarcodeLabelPrintingSetting barcodeSetting = new BarcodeLabelPrintingSetting(true, "Barcode", barcodeFieldsString);
		LabelPrintingSetting labelPrintingSetting = new LabelPrintingSetting("PDF setting", "PDF", csvExcelSetting, null, barcodeSetting);

		JAXBContext context = JAXBContext.newInstance(LabelPrintingSetting.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(labelPrintingSetting, writer);

		String xmlToRead =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><labelPrintingSetting name=\"PDF setting\" outputType=\"PDF\">"
						+ "<barcodeSetting barcodeFieldsString=\"" + barcodeFieldsString
						+ "\" barcodeFormat=\"Barcode\" barcodeNeeded=\"true\"/> "
						+ "<csvExcelSetting includeColumnHeadingsInOutput=\"false\" selectedFieldsString=\"" + selectedFieldsString
						+ "\"/>" + "</labelPrintingSetting>";

		Unmarshaller unmarshaller = context.createUnmarshaller();
		LabelPrintingSetting parsedSetting = (LabelPrintingSetting) unmarshaller.unmarshal(new StringReader(xmlToRead));

		Assert.assertEquals(labelPrintingSetting, parsedSetting);
		Assert.assertEquals(Arrays.asList(LabelPrintingSettingTest.SELECTED_FIELDS_1), parsedSetting.getCsvExcelSetting()
				.getSelectedFieldsList());
		Assert.assertEquals(Arrays.asList(LabelPrintingSettingTest.BARCODE_FIELDS), parsedSetting.getBarcodeSetting()
				.getBarcodeFieldsList());
	}

	private String stringify(Object[] values, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (Object value : values) {
			if (sb.length() > 0) {
				sb.append(delimiter);
			}
			sb.append(value);
		}
		return sb.toString();
	}

}
