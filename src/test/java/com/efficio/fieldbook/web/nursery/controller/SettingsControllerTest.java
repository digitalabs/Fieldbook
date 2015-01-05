package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

public class SettingsControllerTest extends AbstractBaseControllerIntegrationTest {
	
	private static final int NO_OF_OBSERVATIONS = 5;
	private SettingsController controller;
	
	@Before
	public void setup () {
		controller = Mockito.mock(SettingsController.class);
	}
		
	@Test
	public void testGetCheckVariables() {
		List<SettingDetail> nurseryLevelConditions = createSettingDetailVariables();
		CreateNurseryForm form = new CreateNurseryForm();
		
		List<SettingDetail> checkVariables = controller.getCheckVariables(nurseryLevelConditions, form);
		
		Assert.assertTrue("Expected only check variables but the list has non check variables as well.", 
				WorkbookTestUtil.areDetailsFilteredVariables(checkVariables, AppConstants.CHECK_VARIABLES.getString()));
	}
	
	@Test
	public void testGetBasicDetails() {
		List<SettingDetail> nurseryLevelConditions = createSettingDetailVariables();
		CreateNurseryForm form = new CreateNurseryForm();
		
		List<SettingDetail> basicDetails = controller.getSettingDetailsOfSection(nurseryLevelConditions, form, AppConstants.FIXED_NURSERY_VARIABLES.getString());
		
		Assert.assertTrue("Expected only basic detail variables but the list has non basic detail variables as well.", 
				WorkbookTestUtil.areDetailsFilteredVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString()));
	}
	
	@Test
	public void testHasMeasurementDataEnteredGivenAListOfMeasurementRowsWithData(){
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);
		
		List<MeasurementRow> measurementRowList = workbook.getObservations();
		
		Assert.assertTrue(SettingsController.hasMeasurementDataEntered(WorkbookDataUtil.CHALK_PCT_ID, measurementRowList));
	}
	
	@Test
	public void testHasMeasurementDataEnteredGivenAListOfMeasurementRowsWithoutData(){
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(getSampleMeasurementData(1, "Sample Data"));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);
		
		Assert.assertFalse("Expecting the measurement row list has no measurement data.",SettingsController.hasMeasurementDataEntered(2, measurementRowList));
	}
	
	@Test
	public void testHasMeasurementDataEnteredForVariablesWithAtLeast1WithData(){
		EditNurseryController editNurseryController = new EditNurseryController();
		List<Integer> variableIds = new ArrayList<Integer>();
		variableIds.add(new Integer(1));
		variableIds.add(new Integer(2));
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(getSampleMeasurementData(1, "Sample Data"));
		dataList.add(getSampleMeasurementData(2, ""));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);
		userSelection.setMeasurementRowList(measurementRowList);
		boolean hasMeasurementData = editNurseryController.hasMeasurementDataEnteredForVariables(variableIds, userSelection);
		Assert.assertTrue("Should return true since there is measuredData", hasMeasurementData);				
	}
	
	@Test
	public void testHasMeasurementDataEnteredForVariablesWithNoData(){
		EditNurseryController editNurseryController = new EditNurseryController();
		List<Integer> variableIds = new ArrayList<Integer>();
		variableIds.add(new Integer(1));
		variableIds.add(new Integer(2));
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(getSampleMeasurementData(1, ""));
		dataList.add(getSampleMeasurementData(2, ""));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);
		userSelection.setMeasurementRowList(measurementRowList);
		boolean hasMeasurementData = editNurseryController.hasMeasurementDataEnteredForVariables(variableIds, userSelection);
		Assert.assertFalse("Should return false since there is measuredData", hasMeasurementData);				
	}
		
	private MeasurementData getSampleMeasurementData(Integer variableTermId, String data){
		MeasurementData measurementData = new MeasurementData();
		measurementData.setLabel("LABEL_" + variableTermId);
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermId);
		measurementData.setValue(data);
		measurementData.setMeasurementVariable(measurementVariable);
		return measurementData;
	}

	private List<SettingDetail> createSettingDetailVariables() {
		List<SettingDetail> variables = new ArrayList<SettingDetail>();
		variables.add(createSettingDetail(TermId.STUDY_NAME.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_TITLE.getId(), ""));
		variables.add(createSettingDetail(TermId.START_DATE.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_OBJECTIVE.getId(), ""));
		variables.add(createSettingDetail(TermId.END_DATE.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_UID.getId(), ""));
		variables.add(createSettingDetail(TermId.STUDY_UPDATE.getId(), ""));
		variables.add(createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), ""));
		variables.add(createSettingDetail(TermId.PI_NAME.getId(), ""));
		variables.add(createSettingDetail(TermId.PI_ID.getId(), ""));
		variables.add(createSettingDetail(TermId.CHECK_INTERVAL.getId(), ""));
		variables.add(createSettingDetail(TermId.CHECK_PLAN.getId(), ""));
		variables.add(createSettingDetail(TermId.CHECK_START.getId(), ""));
		return variables;
	}

	private SettingDetail createSettingDetail(Integer cvTermId, String value) {
		SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
		return settingDetail;
	}

		
}
