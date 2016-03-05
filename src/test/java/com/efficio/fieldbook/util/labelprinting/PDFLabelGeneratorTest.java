package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.pdf.PdfReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PDFLabelGeneratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(PDFLabelGeneratorTest.class);

    @InjectMocks
    private PDFLabelGenerator unitUnderTest;

    @Test
    public void testEncodeBardcodeInEnglishCharacters() {
        final BitMatrix bitMatrix = this.unitUnderTest.encodeBarcode("Test", 100, 200);
        Assert.assertNotNull("Bit Matrix Barcode should be not null since characters are in English ASCII", bitMatrix);
    }

    @Test
    public void testEncodeBardcodeInNonEnglishCharacters() {
        final BitMatrix bitMatrix = this.unitUnderTest.encodeBarcode("乙七九", 100, 200);
        Assert.assertNull("Bit Matrix Barcode should be null since parameter is non-english ascii", bitMatrix);
    }

    @Test
    public void testTruncateBarcodeLabelForCode(){
        String barcodeLabelForCode = "Nursery Name : SUPER VERY VERY VERY VERY LONG NAME | Nursery Name : SUPER VERY VERY VERY VERY LONG NAME | Year : 2015";
        barcodeLabelForCode = this.unitUnderTest.truncateBarcodeLabelForCode(barcodeLabelForCode);

        String truncatedBarcodeLabelForCode = "Nursery Name : SUPER VERY VERY VERY VERY LONG NAME | Nursery Name : SUPER VERY ";
        Assert.assertEquals("Barcode Label For Code's value should be " + barcodeLabelForCode, truncatedBarcodeLabelForCode, barcodeLabelForCode);
    }

    @Test
    public void testAppendBarcodeIfBarcodeNeededTrue() {
        String mainSelectedFields = "";
        String newFields = unitUnderTest.appendBarcode(true, mainSelectedFields);
        junit.framework.Assert.assertEquals("Should have the id of the Barcode fields", "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt(), newFields);
    }

    @Test
    public void testAppendBarcodeIfBarcodeNeededFalse() {
        String mainSelectedFields = "";
        String newFields = unitUnderTest.appendBarcode(false, mainSelectedFields);
        junit.framework.Assert.assertEquals("Should have the NO id of the Barcode fields", "", newFields);
    }
}
