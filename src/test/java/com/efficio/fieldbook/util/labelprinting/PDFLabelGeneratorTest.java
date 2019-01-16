package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.util.AppConstants;
import com.google.zxing.common.BitMatrix;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class PDFLabelGeneratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(PDFLabelGeneratorTest.class);

    @InjectMocks
    private LabelPrintingPDFUtil LabelPrintingPDFUtil;
    @InjectMocks
    private LabelPrintingUtil labelPrintingUtil;

    @Test
    public void testEncodeBardcodeInEnglishCharacters() throws LabelPrintingException {
        final BitMatrix bitMatrix = this.LabelPrintingPDFUtil.encodeBarcode("Test");
        Assert.assertNotNull("Bit Matrix Barcode should be not null since characters are in English ASCII", bitMatrix);
    }

    @Test (expected = LabelPrintingException.class)
    public void testEncodeBardcodeInNonEnglishCharacters() throws LabelPrintingException {
        this.LabelPrintingPDFUtil.encodeBarcode("乙七九");
    }

    @Test
    public void testTruncateBarcodeLabelForCode(){
        String barcodeLabelForCode = "Nursery Name : SUPER VERY VERY VERY VERY LONG NAME | Nursery Name : SUPER VERY VERY VERY VERY LONG NAME | Year : 2015";
        barcodeLabelForCode = this.LabelPrintingPDFUtil.truncateBarcodeLabelForCode(barcodeLabelForCode);

        final String truncatedBarcodeLabelForCode = "Nursery Name : SUPER VERY VERY VERY VERY LONG NAME | Nursery Name : SUPER VERY ";
        Assert.assertEquals("Barcode Label For Code's value should be " + barcodeLabelForCode, truncatedBarcodeLabelForCode, barcodeLabelForCode);
    }

    @Test
    public void testAppendBarcodeIfBarcodeNeededTrue() {
        final String mainSelectedFields = "";
        final String newFields = this.labelPrintingUtil.appendBarcode(true, mainSelectedFields);
        junit.framework.Assert.assertEquals("Should have the id of the Barcode fields", "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt(), newFields);
    }

    @Test
    public void testAppendBarcodeIfBarcodeNeededFalse() {
        final String mainSelectedFields = "";
        final String newFields = this.labelPrintingUtil.appendBarcode(false, mainSelectedFields);
        junit.framework.Assert.assertEquals("Should have the NO id of the Barcode fields", "", newFields);
    }
}
