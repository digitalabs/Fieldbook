
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.google.common.collect.Lists;

import junit.framework.Assert;

public class TrialMeasurementsControllerTest {

	private TrialMeasurementsController trialMeasurementsController;
	private MeasurementDataTestDataInitializer measurementDataTestDataInitializer;
	private OntologyVariableDataManager ontologyVariableDataManager;
	private StudyDataManager studyDataManager;
	private ContextUtil contextUtil;
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
	private StudyService studyService;

	@Before
	public void setUp() {
		this.trialMeasurementsController = new TrialMeasurementsController();
		this.measurementDataTestDataInitializer = new MeasurementDataTestDataInitializer();
		this.ontologyVariableDataManager = Mockito.mock(OntologyVariableDataManager.class);
		this.trialMeasurementsController.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		this.studyDataManager = Mockito.mock(StudyDataManager.class);
		this.trialMeasurementsController.setStudyDataManager(this.studyDataManager);
		this.contextUtil = Mockito.mock(ContextUtil.class);
		this.trialMeasurementsController.setContextUtil(this.contextUtil);
		this.fieldbookService = Mockito.mock(FieldbookService.class);
		this.trialMeasurementsController.setFieldbookService(this.fieldbookService);
		this.studyService = Mockito.mock(StudyService.class);
		this.trialMeasurementsController.setStudyService(this.studyService);
	}

	@Test
	public void testCopyMeasurementValue() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			Assert.assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList()
					.get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}

	}

	@Test
	public void testCopyMeasurementValueNullEmptyPossibleValues() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(nullData);
		valueRow.getDataList().add(nullData);

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			Assert.assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList()
					.get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}

	}

	@Test
	public void testCopyMeasurementValueNullNullPossibleValuesAndValueIsNotEmpty() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		MeasurementData data = new MeasurementData();
		data.setcValueId("1234");
		data.setDataType(null);
		data.setEditable(false);
		data.setLabel(null);
		data.setPhenotypeId(null);
		data.setValue(null);

		MeasurementData data2 = new MeasurementData();
		data2.setcValueId(null);
		data2.setDataType(null);
		data2.setEditable(false);
		data2.setLabel(null);
		data2.setPhenotypeId(null);
		data2.setValue("jjasd");

		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		data.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(data);
		valueRow.getDataList().add(data2);

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			Assert.assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList()
					.get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}

	}

	@Test
	public void testCopyMeasurementValueWithCustomCategoricalValue() {

		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());

		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey(origRow.getDataList().get(0).getValue());

		origRow.getDataList().get(0).getMeasurementVariable().setPossibleValues(possibleValues);

		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());
		valueRow.getDataList().get(0).setAccepted(true);

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow, true);

		Assert.assertTrue(origRow.getDataList().get(0).isCustomCategoricalValue());

	}

	private List<MeasurementData> generateTestDataList() {

		List<MeasurementData> dataList = new ArrayList<>();

		for (int x = 0; x < 10; x++) {
			MeasurementData data = new MeasurementData();
			data.setcValueId(UUID.randomUUID().toString());
			data.setDataType(UUID.randomUUID().toString());
			data.setEditable(true);
			data.setLabel(UUID.randomUUID().toString());
			data.setPhenotypeId(x);
			data.setValue(UUID.randomUUID().toString());
			data.setMeasurementVariable(new MeasurementVariable());
			dataList.add(data);
		}

		MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);
		dataList.add(nullData);

		MeasurementData emptyData = new MeasurementData();
		emptyData.setcValueId("");
		emptyData.setDataType("");
		emptyData.setEditable(false);
		emptyData.setLabel("");
		emptyData.setPhenotypeId(0);
		emptyData.setValue("");
		emptyData.setMeasurementVariable(measurementVariable);
		dataList.add(emptyData);

		return dataList;
	}

	@Test
	public void testEditExperimentCells() throws MiddlewareQueryException {
		int termId = 2000;
		int experimentId = 1;
		ExtendedModelMap model = new ExtendedModelMap();
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		Variable variableText = new Variable();
		Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		this.trialMeasurementsController.setUserSelection(userSelection);
		this.trialMeasurementsController.editExperimentCells(experimentId, termId, null, model);
		Assert.assertEquals(TermId.CATEGORICAL_VARIABLE.getId(), model.get("categoricalVarId"));
		Assert.assertEquals(TermId.DATE_VARIABLE.getId(), model.get("dateVarId"));
		Assert.assertEquals(TermId.NUMERIC_VARIABLE.getId(), model.get("numericVarId"));
		Assert.assertEquals(false, model.get("isNursery"));
		Assert.assertEquals(variableText, model.get("variable"));
		Assert.assertEquals(experimentId, model.get("experimentId"));
		Assert.assertTrue(((List<?>) model.get("possibleValues")).isEmpty());
		Assert.assertEquals("", model.get("phenotypeId"));
		Assert.assertEquals("", model.get("phenotypeValue"));
	}

	@Test
	public void testUpdateExperimentCellDataIfNotDiscard() {
		int termId = 2000;
		String newValue = "new value";
		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		this.trialMeasurementsController.setUserSelection(userSelection);

		ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Mockito.any(Variable.class), Mockito.anyString())).thenReturn(true);
		this.trialMeasurementsController.setValidationService(mockValidationService);
		
		Variable variableText = new Variable();
		Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		Map<String, String> data = new HashMap<String, String>();
		data.put("experimentId", "1");
		data.put("termId", Integer.toString(termId));
		data.put("value", newValue);

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("isDiscard")).thenReturn("0");

		Map<String, Object> results = this.trialMeasurementsController.updateExperimentCellData(data, req);

		Assert.assertEquals("1", results.get(TrialMeasurementsController.SUCCESS));
		Assert.assertTrue(results.containsKey(TrialMeasurementsController.DATA));

		// Validation and saving of phenotype must occur when isDiscard flag is off.
		Mockito.verify(mockValidationService).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(Phenotype.class), Mockito.anyInt());

	}

	@Test
	public void testUpdateExperimentCellDataIfNotDiscardInvalidButKeep() {
		int termId = 2000;
		String newValue = "new value";
		UserSelection userSelection = new UserSelection();
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		this.trialMeasurementsController.setUserSelection(userSelection);

		ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Mockito.any(Variable.class), Mockito.anyString())).thenReturn(true);
		this.trialMeasurementsController.setValidationService(mockValidationService);

		Variable variableText = new Variable();
		Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		Map<String, String> data = new HashMap<String, String>();
		data.put("experimentId", "1");
		data.put("termId", Integer.toString(termId));
		data.put("value", newValue);

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("isDiscard")).thenReturn("0");
		Mockito.when(req.getParameter("invalidButKeep")).thenReturn("1");

		Map<String, Object> results = this.trialMeasurementsController.updateExperimentCellData(data, req);

		Assert.assertEquals("1", results.get(TrialMeasurementsController.SUCCESS));
		Assert.assertTrue(results.containsKey(TrialMeasurementsController.DATA));

		// Validation step should not be invoked when there is a signal to keep the value even if it is invalid.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		// But save step must be invoked.
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(Phenotype.class), Mockito.anyInt());
	}

	@Test
	public void testUpdateExperimentCellDataIfDiscard() {
		int termId = 2000;
		String newValue = "new value";
		UserSelection userSelection = new UserSelection();

		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		this.trialMeasurementsController.setUserSelection(userSelection);

		ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Mockito.any(Variable.class), Mockito.anyString())).thenReturn(true);

		this.trialMeasurementsController.setValidationService(mockValidationService);

		Variable variableText = new Variable();
		Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		Map<String, String> data = new HashMap<String, String>();
		data.put("experimentId", "1");
		data.put("termId", Integer.toString(termId));
		data.put("value", newValue);

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter("isDiscard")).thenReturn("1");

		Map<String, Object> results = this.trialMeasurementsController.updateExperimentCellData(data, req);

		Assert.assertEquals("1", results.get(TrialMeasurementsController.SUCCESS));
		Assert.assertTrue(results.containsKey(TrialMeasurementsController.DATA));

		// Validation and saving of phenotype must NOT occur when isDiscard flag is on.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager, Mockito.never()).saveOrUpdatePhenotypeValue(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(Phenotype.class), Mockito.anyInt());
	}

	@Test
	public void testMarkExperimentCellDataAsAccepted() {
		int termId = 2000;
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st", TermId.CHARACTER_VARIABLE));

		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2", "2nd",
				new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.trialMeasurementsController.setUserSelection(userSelection);
		this.trialMeasurementsController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap<>();

		data.put("index", "1");
		data.put("termId", Integer.toString(termId));

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		Map<String, Object> results = this.trialMeasurementsController.markExperimentCellDataAsAccepted(data, req);

		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) results.get("data");

		Assert.assertTrue("The Accepted flag should be true", (boolean) ((Object[]) dataMap.get("TestVarName2"))[2]);

	}

	@Test
	public void testMarkExperimentCellDataAsAcceptedForNumeric() {
		int termId = 2000;
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st", TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(termId, "TestVarName2", "1", TermId.NUMERIC_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.trialMeasurementsController.setUserSelection(userSelection);
		this.trialMeasurementsController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap<>();

		data.put("index", "1");
		data.put("termId", Integer.toString(termId));

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		Map<String, Object> results = this.trialMeasurementsController.markExperimentCellDataAsAccepted(data, req);

		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) results.get("data");

		Assert.assertTrue("The Accepted flag should be true", (boolean) ((Object[]) dataMap.get("TestVarName2"))[1]);

	}

	@Test
	public void testMarkAllExperimentDataAsMissing() {
		int termId = 2000;
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st", TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2", "2nd",
				new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName3", "3rd",
				new ArrayList<ValueReference>()));

		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		this.trialMeasurementsController.setUserSelection(userSelection);
		this.trialMeasurementsController.markAllExperimentDataAsMissing();

		for (MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (MeasurementData var : measurementRow.getDataList()) {
					if (var != null) {
						if (var != null
								&& !StringUtils.isEmpty(var.getValue())
								&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
										.getMeasurementVariable().getPossibleValues().isEmpty())) {
							Assert.assertTrue(var.isAccepted());
							if (this.trialMeasurementsController.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var
									.getMeasurementVariable().getPossibleValues())) {
								Assert.assertEquals(MeasurementData.MISSING_VALUE, var.getValue());
							} else {
								Assert.assertFalse("0".equals(var.getValue()));
							}
						} else {
							Assert.assertFalse(var.isAccepted());
						}
					}
				}
			}
		}

	}

	@Test
	public void testIsCategoricalValueOutOfBounds() {
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey("2");

		Assert.assertFalse("2 is in possible values so the return value should be false",
				this.trialMeasurementsController.isCategoricalValueOutOfBounds("2", "", possibleValues));
		Assert.assertTrue("3 is NOT in possible values so the return value should be true",
				this.trialMeasurementsController.isCategoricalValueOutOfBounds("3", "", possibleValues));
		Assert.assertFalse("2 is in possible values so the return value should be false",
				this.trialMeasurementsController.isCategoricalValueOutOfBounds(null, "2", possibleValues));
		Assert.assertTrue("3 is NOT in possible values so the return value should be true",
				this.trialMeasurementsController.isCategoricalValueOutOfBounds(null, "3", possibleValues));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsRange() {
		MeasurementVariable var = new MeasurementVariable();
		var.setMinRange(Double.valueOf("1"));
		var.setMaxRange(Double.valueOf("10"));
		Assert.assertFalse("Should return false since 2 is not out of range",
				this.trialMeasurementsController.isNumericalValueOutOfBounds("2", var));
		Assert.assertTrue("Should return true since 21 is out of range",
				this.trialMeasurementsController.isNumericalValueOutOfBounds("21", var));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsNoRange() {
		MeasurementVariable var = new MeasurementVariable();

		Assert.assertFalse("Should return false since 2 is not out of range",
				this.trialMeasurementsController.isNumericalValueOutOfBounds("2", var));
		Assert.assertFalse("Should return false since 21 is not out of range",
				this.trialMeasurementsController.isNumericalValueOutOfBounds("21", var));
	}

	@Test
	public void testSetCategoricalDisplayType() throws Exception {
		// default case, api call does not include a value for showCategoricalDescriptionView, since the
		// initial value for the isCategoricalDescriptionView is FALSE, the session value will be toggled
		HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute("isCategoricalDescriptionView")).thenReturn(Boolean.FALSE);

		Boolean result = this.trialMeasurementsController.setCategoricalDisplayType(null, session);
		Mockito.verify(session, Mockito.times(1)).setAttribute("isCategoricalDescriptionView", Boolean.TRUE);
		Assert.assertTrue("should be true", result);
	}

	@Test
	public void testSetCategoricalDisplayTypeWithForcedCategoricalDisplayValue() throws Exception {
		// Api call includes a value for showCategoricalDescriptionView, we set the session to this value then
		// return this
		HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute("isCategoricalDescriptionView")).thenReturn(Boolean.FALSE);

		Boolean result = this.trialMeasurementsController.setCategoricalDisplayType(Boolean.FALSE, session);
		Mockito.verify(session, Mockito.times(1)).setAttribute("isCategoricalDescriptionView", Boolean.FALSE);
		Assert.assertFalse("should be false", result);
	}

	@Test
	public void testGetPlotMeasurementsPaginated() {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("pageNumber", "1");
		request.addParameter("pageSize", "10");
		request.addParameter("sortBy", "ENTRY_NO");
		request.addParameter("sortOrder", "desc");

		String drawParam = "drawParamValue";
		request.addParameter("draw", drawParam);

		StudyService studyService = Mockito.mock(StudyService.class);
		MeasurementDto measurementText = new MeasurementDto(new TraitDto(1, "Notes"), 1, "Text Notes");
		MeasurementDto measurementNumeric = new MeasurementDto(new TraitDto(2, "Grain Yield"), 2, "500");
		MeasurementDto mesaurementCategorical = new MeasurementDto(new TraitDto(3, "CategoricalTrait"), 3, "CategoryValue1");

		List<MeasurementDto> measurements = Lists.newArrayList(measurementText, measurementNumeric, mesaurementCategorical);
		ObservationDto observationDto =
				new ObservationDto(123, "1", "Test Entry", 300, "CML123", "5", "Entry Code", "2", "10", "3", measurements);

		observationDto.additionalGermplasmDescriptor("StockID", "STCK-123");
		observationDto.additionalGermplasmDescriptor("CROSS", "ABC12/XYZ34");

		List<ObservationDto> observations = Lists.newArrayList(observationDto);
		Mockito.when(studyService.getObservations(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(observations);

		int recordsCount = 1;
		Mockito.when(studyService.countTotalObservationUnits(Mockito.anyInt(), Mockito.anyInt())).thenReturn(recordsCount);
		this.trialMeasurementsController.setStudyService(studyService);

		Variable variableText = new Variable();
		Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(measurementText.getTrait().getTraitId()),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableText);

		Variable variableNumeric = new Variable();
		Scale scaleNumeric = new Scale();
		scaleNumeric.setDataType(DataType.NUMERIC_VARIABLE);
		variableNumeric.setScale(scaleNumeric);
		Mockito.when(
				this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(measurementNumeric.getTrait().getTraitId()),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableNumeric);

		Variable variableCategorical = new Variable();
		Scale scaleCategorical = new Scale();
		scaleCategorical.setDataType(DataType.CATEGORICAL_VARIABLE);
		TermSummary category1 = new TermSummary(111, "CategoryValue1", "CategoryValue1Definition");
		scaleCategorical.addCategory(category1);
		variableCategorical.setScale(scaleCategorical);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(),
				Mockito.eq(mesaurementCategorical.getTrait().getTraitId()),
				Matchers.eq(true), Matchers.eq(false)))
				.thenReturn(variableCategorical);

		this.trialMeasurementsController.setContextUtil(Mockito.mock(ContextUtil.class));

		final Map<String, Object> plotMeasurementsPaginated = this.trialMeasurementsController.getPlotMeasurementsPaginated(1, 1,
				new CreateNurseryForm(), Mockito.mock(Model.class), request);

		Assert.assertNotNull("Expected a non-null map as return value.", plotMeasurementsPaginated);
		Assert.assertEquals("Expected number of entries in the map did not match.", 4, plotMeasurementsPaginated.size());

		Assert.assertEquals("'draw' parameter should be returned in map as per value of request parameter 'draw'.", drawParam,
				plotMeasurementsPaginated.get("draw"));
		Assert.assertEquals("Record count should be returned as per what is returned by studyService.countTotalObservationUnits()",
				recordsCount, plotMeasurementsPaginated.get("recordsTotal"));
		Assert.assertEquals("Records filtered should be returned as per number of plots on page.", observations.size(),
				plotMeasurementsPaginated.get("recordsFiltered"));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> allMeasurementData = (List<Map<String, Object>>) plotMeasurementsPaginated.get("data");
		Map<String, Object> onePlotMeasurementData = allMeasurementData.get(0);

		Assert.assertEquals(String.valueOf(observationDto.getMeasurementId()), onePlotMeasurementData.get("experimentId"));
		Assert.assertEquals(observationDto.getDesignation(), onePlotMeasurementData.get("DESIGNATION"));
		Assert.assertEquals(observationDto.getGid(), onePlotMeasurementData.get("GID"));

		Assert.assertTrue(
				Arrays.equals(new Object[] {observationDto.getEntryNo(), false}, (Object[]) onePlotMeasurementData.get("ENTRY_NO")));

		Assert.assertTrue(
				Arrays.equals(new Object[] {observationDto.getEntryCode(), false}, (Object[]) onePlotMeasurementData.get("ENTRY_CODE")));

		Assert.assertTrue(Arrays.equals(new Object[] {"STCK-123"}, (Object[]) onePlotMeasurementData.get("StockID")));
		Assert.assertTrue(Arrays.equals(new Object[] {"ABC12/XYZ34"}, (Object[]) onePlotMeasurementData.get("CROSS")));

		Assert.assertTrue(
				Arrays.equals(new Object[] {observationDto.getEntryType(), observationDto.getEntryType(), false},
						(Object[]) onePlotMeasurementData.get("ENTRY_TYPE")));

		Assert.assertTrue(
				Arrays.equals(new Object[] {observationDto.getPlotNumber(), false}, (Object[]) onePlotMeasurementData.get("PLOT_NO")));

		Assert.assertTrue(
				Arrays.equals(new Object[] {observationDto.getBlockNumber(), false}, (Object[]) onePlotMeasurementData.get("BLOCK_NO")));

		Assert.assertTrue(
				Arrays.equals(new Object[] {observationDto.getRepitionNumber(), false}, (Object[]) onePlotMeasurementData.get("REP_NO")));

		Assert.assertTrue(Arrays.equals(new Object[] {observationDto.getTrialInstance(), false},
				(Object[]) onePlotMeasurementData.get("TRIAL_INSTANCE")));


		// Character Trait
		Assert.assertTrue(Arrays.equals(new Object[] {measurementText.getTriatValue(), measurementText.getPhenotypeId()},
				(Object[]) onePlotMeasurementData.get(measurementText.getTrait().getTraitName())));

		// Numeric Trait
		Assert.assertTrue(Arrays.equals(new Object[] {measurementNumeric.getTriatValue(), true, measurementNumeric.getPhenotypeId()},
				(Object[]) onePlotMeasurementData.get(measurementNumeric.getTrait().getTraitName())));

		// Categorical Trait
		Assert.assertTrue(
				Arrays.equals(new Object[] {category1.getName(), category1.getDefinition(), true, mesaurementCategorical.getPhenotypeId()},
				(Object[]) onePlotMeasurementData.get(mesaurementCategorical.getTrait().getTraitName())));

		ArgumentCaptor<Integer> pageNumberArg = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> pageSizeArg = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> sortByArg = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> sortOrderArg = ArgumentCaptor.forClass(String.class);

		Mockito.verify(studyService).getObservations(Mockito.anyInt(), Mockito.anyInt(), pageNumberArg.capture(),
				pageSizeArg.capture(), sortByArg.capture(), sortOrderArg.capture());
		Assert.assertEquals(new Integer(1), pageNumberArg.getValue());
		Assert.assertEquals(new Integer(10), pageSizeArg.getValue());
		Assert.assertEquals("ENTRY_NO", sortByArg.getValue());
		Assert.assertEquals("desc", sortOrderArg.getValue());
	}
}
