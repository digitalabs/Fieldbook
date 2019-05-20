package com.efficio.fieldbook.util.labelprinting;

import com.csvreader.CsvReader;
import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import org.generationcp.commons.constant.AppConstants;
import junit.framework.Assert;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CSVLabelGeneratorIT extends AbstractBaseIntegrationTest{

    @Resource
    private CSVLabelGenerator unitUnderTest;
    @Resource
    private LabelPrintingUtil labelPrintingUtil;

    @Test
    public void testGenerationOfCsvLabels() {
        final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
        final UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
        String fileName = "";
        try {
            fileName = this.unitUnderTest.generateLabels(trialInstances, userLabelPrinting);

            CsvReader csvReader = new CsvReader(fileName);

            csvReader.readHeaders();
            String[] headers = csvReader.getHeaders();

            Assert.assertNotNull("Expected a new csv file was created but found none.", csvReader);
            Assert.assertNotNull("Expecting a csv file with headers but no header was found.", headers);
            Assert.assertTrue("Expected all headers but only got " + headers.length, this.areHeadersEqual(headers, userLabelPrinting));
            Assert.assertTrue("Expected " + " rows but got " + " instead.", this.areRowsEqual(csvReader, headers, userLabelPrinting));
        } catch (LabelPrintingException | IOException e) {
            Assert.fail("Excountered error while exporting/reading csv file.");
        }
    }

    private boolean areHeadersEqual(String[] headers, UserLabelPrinting userLabelPrinting) {
        String calculatedHeader =
                userLabelPrinting.getMainSelectedLabelFields()
                        + (userLabelPrinting.getBarcodeNeeded().equals("1") ? "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt() : "");
        int headerLength = calculatedHeader.split(",").length;
        return headers.length == headerLength;
    }

    private boolean areRowsEqual(CsvReader csvReader, String[] headers, UserLabelPrinting userLabelPrinting) {
        try {
            int rowNum = 0, rowNum2 = 0;
            while (csvReader.readRecord()) {
                rowNum++;
            }

            for (FieldMapDatasetInfo dataset : userLabelPrinting.getFieldMapInfo().getDatasets()) {
                for (FieldMapTrialInstanceInfo trialInstance : dataset.getTrialInstancesWithFieldMap()) {
                    rowNum2 += trialInstance.getFieldMapLabels().size();
                }
            }
            return rowNum == rowNum2;
        } catch (IOException e) {
            Assert.fail("Error encountered while reading the file.");
            return false;
        }
    }

    @Test
    public void testGenerateAddedInformationField() {
        FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = new FieldMapTrialInstanceInfo();
        StudyTrialInstanceInfo trialInstance = new StudyTrialInstanceInfo(fieldMapTrialInstanceInfo, "TestStudy");
        String barCode = "testBarcode";
        fieldMapTrialInstanceInfo.setLocationName("Loc1");
        fieldMapTrialInstanceInfo.setBlockName("Block1");
        fieldMapTrialInstanceInfo.setFieldName("Field1");
        trialInstance.setFieldbookName("Fieldbook1");
        fieldMapTrialInstanceInfo.setTrialInstanceNo("1");

        Map<String, String> dataResults =
                this.labelPrintingUtil.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, barCode);
        Assert.assertEquals("Should have the same location name", fieldMapTrialInstanceInfo.getLocationName(),
                dataResults.get("locationName"));
        Assert.assertEquals("Should have the same Block name", fieldMapTrialInstanceInfo.getBlockName(), dataResults.get("blockName"));
        Assert.assertEquals("Should have the same Field name", fieldMapTrialInstanceInfo.getFieldName(), dataResults.get("fieldName"));
        Assert.assertEquals("Should have the same Fieldbook name", trialInstance.getFieldbookName(), dataResults.get("selectedName"));
        Assert.assertEquals("Should have the same Trial Instance Number", fieldMapTrialInstanceInfo.getTrialInstanceNo(),
                dataResults.get("trialInstanceNumber"));
        Assert.assertEquals("Should have the same Barcode string", barCode, dataResults.get("barcode"));
    }
}
