package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.lowagie.text.pdf.PdfReader;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class PDFLabelGeneratorIT extends AbstractBaseIntegrationTest {
    @Resource
    private PDFLabelGenerator unitUnderTest;

    @Test
    public void testGenerationOfPdfLabels() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
        UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
        String fileName = "";
        try {
            fileName = this.unitUnderTest.generateLabels(trialInstances, userLabelPrinting, baos);

            PdfReader reader = new PdfReader(fileName);
            junit.framework.Assert.assertNotNull("Expected a new pdf file was created but found none.", reader);
            byte[] streamBytes = reader.getPageContent(1);
            junit.framework.Assert.assertNotNull("Expected a file with content but found none.", streamBytes);
        } catch (LabelPrintingException e) {
            junit.framework.Assert.fail("Error encountered while generating pdf file.");
        } catch (FileNotFoundException e) {
            junit.framework.Assert.fail("File not found.");
        } catch (IOException e) {
            junit.framework.Assert.fail("Error encountered while reading file.");
        }
    }
}
