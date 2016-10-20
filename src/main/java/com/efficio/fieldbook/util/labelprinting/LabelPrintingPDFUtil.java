package com.efficio.fieldbook.util.labelprinting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

@Component
class LabelPrintingPDFUtil {

	static final String UNSUPPORTED_CHARSET_IMG = "unsupported-char-set.png";

	static final String ARIAL_UNI = "arialuni.ttf";

	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingPDFUtil.class);

	/**
	 * Truncate the barcode label for code instead of throwing an error in pdf
	 * @param barcodeLabelForCode barcode label to truncate
	 * @return truncated barcode label
	 */
	String truncateBarcodeLabelForCode(String barcodeLabelForCode) {
		if (barcodeLabelForCode != null && barcodeLabelForCode.length() > 79) {
			barcodeLabelForCode = barcodeLabelForCode.substring(0, 79);
		}
		return barcodeLabelForCode;
	}

	/**
	 * Encode barcode label for pdf pages
	 * @param barcodeLabelForCode barcode label to encode
	 * @param width width of the barcode
	 * @param height height of the barcode
	 * @return barcode image
	 * @throws LabelPrintingException exception trying to ganarate the barcode image
	 */
	BitMatrix encodeBarcode(final String barcodeLabelForCode, final int width, final int height) throws LabelPrintingException {
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = new Code128Writer().encode(barcodeLabelForCode, BarcodeFormat.CODE_128, width, height, null);
		} catch (final WriterException | IllegalArgumentException e) {
			LOG.debug(e.getMessage(), e);
			throw new LabelPrintingException(e.getMessage());
		}
		return bitMatrix;
	}

}
