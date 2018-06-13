package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.ExcelImportUtil;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import junit.framework.Assert;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcelLabelGeneratorIT extends AbstractBaseIntegrationTest{
    
    private static final Logger LOG = LoggerFactory.getLogger(ExcelLabelGeneratorIT.class);

    private static final int[] FIELD_MAP_LABELS = {AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(),
            AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()};

    private static final String PLOT_COORDINATES = "Plot Coordinates";

    @Resource
    private MessageSource messageSource;

    @Resource
    protected FieldbookService fieldbookService;

    @Resource
    private ExcelLabelGenerator unitUnderTest;

	@Resource
	private LabelPrintingUtil labelPrintingUtil;

	@Test
	public void testFieldmapFieldsInGeneratedXls() throws Exception {
		final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		final UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
		String labels = "";
		String fileName = "";

		fileName = this.unitUnderTest.generateLabels(trialInstances, userLabelPrinting);
		final org.apache.poi.ss.usermodel.Workbook xlsBook = ExcelImportUtil.parseFile(fileName);

		final Sheet sheet = xlsBook.getSheetAt(0);

		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			final Row row = sheet.getRow(i);
			if (row != null) {
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					final Cell cell = row.getCell(j);

					if (cell != null && cell.getStringCellValue() != null) {
						labels = labels + " " + cell.getStringCellValue();
					}
				}
			}
		}

		Assert.assertTrue(this.areFieldsinGeneratedLabels(labels, false));
	}

    private boolean areFieldsinGeneratedLabels(final String labels, final boolean isPdf) {
        boolean areFieldsInLabels = true;
        final Locale locale = new Locale("en", "US");

        for (final int label : FIELD_MAP_LABELS) {
            String fieldName = "";
            if (label == AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
                fieldName = this.messageSource.getMessage("label.printing.available.fields.block.name", null, locale);
            } else if (label == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
                fieldName = this.messageSource.getMessage("label.printing.available.fields.field.name", null, locale);
            } else if (label == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
                fieldName = PLOT_COORDINATES;
            }

            final String[] field = labels.split(fieldName);
            if (field.length <= 1) {
                areFieldsInLabels = false;
                break;
            }
        }

        return areFieldsInLabels;
    }

    @Test
    public void testGenerationOfXlsLabels() {
        final List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
        final UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
        String fileName = "";
        try {
            fileName = this.unitUnderTest.generateLabels(trialInstances, userLabelPrinting);
            final org.apache.poi.ss.usermodel.Workbook xlsBook = ExcelImportUtil.parseFile(fileName);

            Assert.assertNotNull("Expected a new workbook file was created but found none.", xlsBook);

            final Sheet sheet = xlsBook.getSheetAt(0);

            Assert.assertNotNull("Expecting an xls file with 1 sheet but found none.", sheet);

            Assert.assertTrue("Expected at least one row but got 0", sheet.getLastRowNum() > 0);
        } catch (final MiddlewareQueryException e) {
            Assert.fail("Encountered error while exporting to xls.");
        } catch (final Exception e) {
            Assert.fail("Excountered error while reading xls file.");
        }
    }

    @Test
    public void testGetLabelHeadersFromTrialInstancesWithMultipleTrialInstances(){

        final List<StudyTrialInstanceInfo> trialInstances = new ArrayList<>();
        final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfoFirst = LabelPrintingDataUtil.createFieldMapTrialInstanceInfo();

        final StudyTrialInstanceInfo trialInstanceFirst =
                new StudyTrialInstanceInfo(fieldMapTrialInstanceInfoFirst, "Study");
        trialInstances.add(trialInstanceFirst);

        final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfoSecond = LabelPrintingDataUtil.createFieldMapTrialInstanceInfo();
        final Map<Integer, String> labelHeadersForTrialStock = LabelPrintingDataUtil.createLabelHeadersForStudyStock();
        // Setting LabelHeader in second Trial instance only
        fieldMapTrialInstanceInfoSecond.setLabelHeaders(labelHeadersForTrialStock);

        final StudyTrialInstanceInfo trialInstanceSecond =
                new StudyTrialInstanceInfo(fieldMapTrialInstanceInfoSecond, "Study");
        trialInstances.add(trialInstanceSecond);


        final Map<Integer, String> labelHeadersFromTrialInstances = this.labelPrintingUtil.getLabelHeadersFromTrialInstances(trialInstances);

        Assert.assertNotNull(labelHeadersFromTrialInstances);
        Assert.assertEquals("Number of Label Headers are not equal", 5, labelHeadersFromTrialInstances.size());

        for(final Map.Entry<Integer,String> entrySet : labelHeadersForTrialStock.entrySet()){
            final Integer keyTermId = entrySet.getKey();
            final String valueHeaderLabel = entrySet.getValue();

            if(labelHeadersFromTrialInstances.containsKey(keyTermId)){
                final String actualHeaderText = labelHeadersFromTrialInstances.get(keyTermId);
                Assert.assertEquals("Label Header Text is not equal", valueHeaderLabel , actualHeaderText);
            } else {
                Assert.assertNull("Expected Label Header not found", keyTermId);
            }

        }

    }

}
