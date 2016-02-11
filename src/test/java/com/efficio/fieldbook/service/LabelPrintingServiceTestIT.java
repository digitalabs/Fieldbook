
package com.efficio.fieldbook.service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.csvreader.CsvReader;
import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.utils.test.ExcelImportUtil;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.lowagie.text.pdf.PdfReader;

public class LabelPrintingServiceTestIT extends AbstractBaseIntegrationTest {

	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceTestIT.class);

	private static final int[] FIELD_MAP_LABELS = {AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(),
			AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()};


	@Resource
	private LabelPrintingService labelPrintingService;

	@Resource
	private MessageSource messageSource;

	@Test
	public void testGetAvailableLabelFieldsFromTrialWithoutFieldMap() {
		Locale locale = new Locale("en", "US");
		List<LabelFields> labels = this.labelPrintingService.getAvailableLabelFieldsForFieldMap(true, false, locale);
		Assert.assertFalse(this.areFieldsInLabelList(labels));
	}

	@Test
	public void testGetAvailableLabelFieldsFromTrialWithFieldMap() {
		Locale locale = new Locale("en", "US");
		List<LabelFields> labels = this.labelPrintingService.getAvailableLabelFieldsForFieldMap(true, true, locale);
		Assert.assertTrue(this.areFieldsInLabelList(labels));
	}

	@Test
	public void testGetAvailableLabelFieldsFromNurseryWithoutFieldMap() {
		Locale locale = new Locale("en", "US");
		List<LabelFields> labels = this.labelPrintingService.getAvailableLabelFieldsForFieldMap(false, false, locale);
		Assert.assertFalse(this.areFieldsInLabelList(labels));
	}

	@Test
	public void testGetAvailableLabelFieldsFromNurseryWithFieldMap() {
		Locale locale = new Locale("en", "US");
		List<LabelFields> labels = this.labelPrintingService.getAvailableLabelFieldsForFieldMap(false, true, locale);
		Assert.assertTrue(this.areFieldsInLabelList(labels));
	}

	@Test
	public void testGetAvailableLabelFieldsFromFieldMap() {
		Locale locale = new Locale("en", "US");
		List<LabelFields> labels = this.labelPrintingService.getAvailableLabelFieldsForFieldMap(false, true, locale);
		Assert.assertTrue(this.areFieldsInLabelList(labels));
	}

	private boolean areFieldsInLabelList(List<LabelFields> labels) {
		int fieldMapLabelCount = 0;

		if (labels != null) {
			for (LabelFields label : labels) {
				for (int fieldMapLabel : LabelPrintingServiceTestIT.FIELD_MAP_LABELS) {
					if (label.getId() == fieldMapLabel) {
						fieldMapLabelCount++;
					}
				}
			}

			if (fieldMapLabelCount == LabelPrintingServiceTestIT.FIELD_MAP_LABELS.length) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Test
	public void testFieldMapPropertiesOfNurseryWithoutFieldMap() {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(false).get(0);
		this.setFieldmapProperties(fieldMapInfoDetail, false, false);
		boolean hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);

		Assert.assertFalse(hasFieldMap);
		Assert.assertFalse(userLabelPrinting.isFieldMapsExisting());
	}

	@Test
	public void testFieldMapPropertiesOfNurseryWithFieldMap() {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(false).get(0);
		this.setFieldmapProperties(fieldMapInfoDetail, true, false);
		boolean hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);

		Assert.assertTrue(hasFieldMap);
		Assert.assertTrue(userLabelPrinting.isFieldMapsExisting());
	}

	@Test
	public void testFieldMapPropertiesOfTrialWithoutFieldMaps() {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(true).get(0);
		this.setFieldmapProperties(fieldMapInfoDetail, false, false);
		boolean hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);

		Assert.assertFalse(hasFieldMap);
		Assert.assertFalse(userLabelPrinting.isFieldMapsExisting());
	}

	@Test
	public void testFieldMapPropertiesOfTrialWithOneFieldMap() {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(true).get(0);
		this.setFieldmapProperties(fieldMapInfoDetail, false, true);
		boolean hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);

		Assert.assertTrue(hasFieldMap);
		Assert.assertFalse(userLabelPrinting.isFieldMapsExisting());
	}

	@Test
	public void testFieldMapPropertiesOfTrialWithFieldMaps() {
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(true).get(0);
		this.setFieldmapProperties(fieldMapInfoDetail, true, false);
		boolean hasFieldMap = this.labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);

		Assert.assertTrue(hasFieldMap);
		Assert.assertTrue(userLabelPrinting.isFieldMapsExisting());
	}

	private void setFieldmapProperties(FieldMapInfo fieldMapInfoDetail, boolean hasFieldMap, boolean hasOneTrialInstanceWithFieldMap) {
		// set column to null and hasFieldMap to false if study has no fieldmap at all
		// else, don't change the values
		for (FieldMapDatasetInfo dataset : fieldMapInfoDetail.getDatasetsWithFieldMap()) {
			int ctr = 0;
			for (FieldMapTrialInstanceInfo trialInstance : dataset.getTrialInstances()) {
				if (ctr == 0 && hasOneTrialInstanceWithFieldMap || !hasOneTrialInstanceWithFieldMap) {
					trialInstance.setHasFieldMap(hasFieldMap);
					if (!hasFieldMap) {
						for (FieldMapLabel label : trialInstance.getFieldMapLabels()) {
							label.setColumn(null);
						}
					}
				}
				ctr++;
			}
		}
	}






	@Ignore(
			value = "Needs to resolve NPE and other data issues. Method under test is a highly likely candidate for refactoring, given complex logic path")
	@Test
	public void testPopulateUserSpecifiedLabelFieldsForNurseryEnvironmentDataOnly() {
		String testDesigValue = "123";

		String testSelectedFields = Integer.toString(TermId.DESIG.getId()) + "," + Integer.toString(TermId.ENTRY_NO.getId());

		List<FieldMapTrialInstanceInfo> input = new ArrayList<>();
		input.add(LabelPrintingDataUtil.createFieldMapTrialInstanceInfo());

		this.labelPrintingService.populateUserSpecifiedLabelFields(input, this.setupTestWorkbook(), testSelectedFields, false, false);

		Assert.assertEquals(testDesigValue,
				input.get(0).getFieldMapLabel(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO).getUserFields().get(TermId.DESIG.getId()));
		Assert.assertEquals("1",
				input.get(0).getFieldMapLabel(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO).getUserFields().get(TermId.ENTRY_NO.getId()));
	}

	protected Workbook setupTestWorkbook() {
        Workbook workbook = Mockito.mock(Workbook.class);
        Mockito.doReturn(new ArrayList<MeasurementVariable>()).when(workbook).getStudyConditions();
        Mockito.doReturn(new ArrayList<MeasurementVariable>()).when(workbook).getFactors();

        // prepare measurement rows simulating experiment data
        List<MeasurementRow> sampleData = new ArrayList<>();

        // add a row with measurement data for the DESIG and ENTRY_NO terms
        MeasurementRow row = new MeasurementRow();
        // experiment ID here is set to be the same as the one used when creating the sample instance data, since they need to correlate.
        row.setExperimentId(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO);

        List<MeasurementData> dataList = new ArrayList<>();
        MeasurementData data = new MeasurementData();
        MeasurementVariable var = new MeasurementVariable();
        var.setTermId(TermId.DESIG.getId());
        data.setMeasurementVariable(var);
        data.setValue("123");
        dataList.add(data);

        data = new MeasurementData();
        var = new MeasurementVariable();
        var.setTermId(TermId.ENTRY_NO.getId());
        data.setMeasurementVariable(var);
        data.setValue("1");
        dataList.add(data);

        row.setDataList(dataList);
        sampleData.add(row);
        Mockito.doReturn(sampleData).when(workbook).getObservations();

        return workbook;
    }

}
