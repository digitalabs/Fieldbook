
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class TrialMeasurementsControllerTest {

	private static final String CROSS_VALUE = "ABC12/XYZ34";
	private static final String STOCK_ID_VALUE = "STCK-123";
	private static final String DATA = "data";
	private static final String RECORDS_FILTERED = "recordsFiltered";
	private static final String RECORDS_TOTAL = "recordsTotal";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String DESIGNATION = "DESIGNATION";
	private static final String DRAW = "draw";
	private static final String SORT_ORDER = "sortOrder";
	private static final String SORT_BY = "sortBy";
	private static final String PAGE_SIZE = "pageSize";
	private static final String PAGE_NUMBER = "pageNumber";
	private static final String IS_CATEGORICAL_DESCRIPTION_VIEW = "isCategoricalDescriptionView";
	private static final String VALUE = "value";
	private static final String INDEX = "index";
	private static final String TERM_ID = "termId";
	private static final String IS_DISCARD = "isDiscard";
	private static final String EXPERIMENT_ID = "experimentId";
	private static final String ACTION = "Action";
	private static final String CROSS = "CROSS";
	private static final String STOCK_ID = "StockID";
	private static final String ALEUCOL_1_5_TRAIT_NAME = "ALEUCOL_1_5";
	private static final int ALEUCOL_1_5_TERM_ID = 123;
	private static final String LOCAL = "-Local";
	private static final String FIELDMAP_COLUMN = "FIELDMAP_COLUMN";
	private static final String FIELDMAP_RANGE = "FIELDMAP_RANGE";
	

	@InjectMocks
	private TrialMeasurementsController trialMeasurementsController;
	private MeasurementDataTestDataInitializer measurementDataTestDataInitializer;
	
	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;
	
	@Mock
	private StudyDataManager studyDataManager;
	
	@Mock
	private ContextUtil contextUtil;
	
	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
	
	@Mock
	private StudyService studyService;
	
	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Mock
	private ValidationService validationService;
	
	@Mock
	private FieldbookService fieldbookMiddlewareService;
	
	private List<MeasurementVariable> measurementVariables;

	private final MeasurementDto measurementText = new MeasurementDto(new MeasurementVariableDto(1, "NOTES"), 1, "Text Notes");
	private final MeasurementDto measurementNumeric = new MeasurementDto(new MeasurementVariableDto(2, "Grain Yield"), 2, "500");
	private final MeasurementDto measurementCategorical = new MeasurementDto(new MeasurementVariableDto(3, "CategoricalTrait"), 3, "CategoryValue1");

	private final TermId[] standardFactors = {TermId.GID, TermId.ENTRY_NO, TermId.ENTRY_TYPE, TermId.ENTRY_CODE, TermId.PLOT_NO, TermId.PLOT_ID,
			TermId.BLOCK_NO, TermId.REP_NO, TermId.ROW, TermId.COL, TermId.FIELDMAP_COLUMN, TermId.FIELDMAP_RANGE};

	@Before
	public void setUp() {
		this.measurementDataTestDataInitializer = new MeasurementDataTestDataInitializer();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(new Term(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.name(), "Definition"));
	}

	@Test
	public void testCopyMeasurementValue() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				assertThat("The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(), is(equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				assertThat("The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(), not(equalTo(valueRow.getDataList().get(x).getValue())));
			}

		}

	}

	@Test
	public void testCopyMeasurementValueNullEmptyPossibleValues() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		final MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final List<ValueReference> possibleValues = new ArrayList<>();
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(nullData);
		valueRow.getDataList().add(nullData);

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				assertThat("The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(), is(equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				assertThat("The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(), not(equalTo(valueRow.getDataList().get(x).getValue())));
			}

		}

	}

	@Test
	public void testCopyMeasurementValueNullNullPossibleValuesAndValueIsNotEmpty() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		final MeasurementData data = new MeasurementData();
		data.setcValueId("1234");
		data.setDataType(null);
		data.setEditable(false);
		data.setLabel(null);
		data.setPhenotypeId(null);
		data.setValue(null);

		final MeasurementData data2 = new MeasurementData();
		data2.setcValueId(null);
		data2.setDataType(null);
		data2.setEditable(false);
		data2.setLabel(null);
		data2.setPhenotypeId(null);
		data2.setValue("jjasd");

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		data.setMeasurementVariable(measurementVariable);

		origRow.getDataList().add(data);
		valueRow.getDataList().add(data2);

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				assertThat("The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(), is(equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				assertThat("The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(), not(equalTo(valueRow.getDataList().get(x).getValue())));
			}

		}

	}

	@Test
	public void testCopyMeasurementValueWithCustomCategoricalValue() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());

		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey(origRow.getDataList().get(0).getValue());

		origRow.getDataList().get(0).getMeasurementVariable().setPossibleValues(possibleValues);

		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());
		valueRow.getDataList().get(0).setAccepted(true);

		this.trialMeasurementsController.copyMeasurementValue(origRow, valueRow, true);
		assertThat(origRow.getDataList().get(0).getIsCustomCategoricalValue(),is(true));

	}

	private List<MeasurementData> generateTestDataList() {

		final List<MeasurementData> dataList = new ArrayList<>();

		for (int x = 0; x < 10; x++) {
			final MeasurementData data = new MeasurementData();
			data.setcValueId(UUID.randomUUID().toString());
			data.setDataType(UUID.randomUUID().toString());
			data.setEditable(true);
			data.setLabel(UUID.randomUUID().toString());
			data.setPhenotypeId(x);
			data.setValue(UUID.randomUUID().toString());
			data.setMeasurementVariable(new MeasurementVariable());
			dataList.add(data);
		}

		final MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);
		dataList.add(nullData);

		final MeasurementData emptyData = new MeasurementData();
		emptyData.setcValueId("");
		emptyData.setDataType("");
		emptyData.setEditable(false);
		emptyData.setLabel("");
		emptyData.setPhenotypeId(0);
		emptyData.setValue("");
		emptyData.setMeasurementVariable(measurementVariable);
		dataList.add(emptyData);

		final MeasurementData measurementDataOfAFactor = new MeasurementData();
		final MeasurementVariable measurementVariableOfAFactor = new MeasurementVariable();
		measurementVariableOfAFactor.setFactor(true);
		measurementDataOfAFactor.setcValueId(UUID.randomUUID().toString());
		measurementDataOfAFactor.setDataType(UUID.randomUUID().toString());
		measurementDataOfAFactor.setEditable(false);
		measurementDataOfAFactor.setLabel(UUID.randomUUID().toString());
		measurementDataOfAFactor.setPhenotypeId(0);
		measurementDataOfAFactor.setValue(UUID.randomUUID().toString());
		measurementDataOfAFactor.setMeasurementVariable(measurementVariableOfAFactor);
		dataList.add(measurementDataOfAFactor);

		return dataList;
	}

	@Test
	public void testEditExperimentCells() throws MiddlewareQueryException {
		final int termId = 2000;
		final int experimentId = 1;
		final ExtendedModelMap model = new ExtendedModelMap();
		final UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		this.trialMeasurementsController.setUserSelection(userSelection);
		this.trialMeasurementsController.editExperimentCells(experimentId, termId, null, model);
		assertThat(TermId.CATEGORICAL_VARIABLE.getId(), is(equalTo(model.get("categoricalVarId"))));
		assertThat(TermId.DATE_VARIABLE.getId(), is(equalTo(model.get("dateVarId"))));
		assertThat(TermId.NUMERIC_VARIABLE.getId(), is(equalTo(model.get("numericVarId"))));
		assertThat(variableText, is(equalTo(model.get("variable"))));
		assertThat(experimentId, is(equalTo(model.get(EXPERIMENT_ID))));
		assertThat((List<?>)model.get("possibleValues"), hasSize(0));
		assertThat("", is(equalTo(model.get("phenotypeId"))));
		assertThat("", is(equalTo(model.get("phenotypeValue"))));
	}

	@Test
	public void testEditExperimentCellsImportPreview() throws MiddlewareQueryException {
		final int termId = 2000;
		final int experimentId = 1;
		final ExtendedModelMap model = new ExtendedModelMap();
		final UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);

		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st", TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		final String phenotpevalue = "2nd";
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2", phenotpevalue,
			new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);

		this.trialMeasurementsController.setUserSelection(userSelection);
		this.trialMeasurementsController.editExperimentCells(experimentId, termId, model);
		assertThat(TermId.CATEGORICAL_VARIABLE.getId(), is(equalTo(model.get("categoricalVarId"))));
		assertThat(TermId.DATE_VARIABLE.getId(), is(equalTo(model.get("dateVarId"))));
		assertThat(TermId.NUMERIC_VARIABLE.getId(), is(equalTo(model.get("numericVarId"))));
		assertThat((List<?>)model.get("possibleValues"), hasSize(0));
		assertThat(0, is(equalTo(model.get("phenotypeId"))));
		assertThat(phenotpevalue, is(equalTo(model.get("phenotypeValue"))));
	}

	@Test
	public void testUpdateExperimentCellDataIfNotDiscard() {
		final int termId = 2000;
		final String newValue = "new value";
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		this.trialMeasurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Mockito.any(Variable.class), Mockito.anyString())).thenReturn(true);
		this.trialMeasurementsController.setValidationService(mockValidationService);
		
		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(EXPERIMENT_ID, "1");
		data.put(TERM_ID, Integer.toString(termId));
		data.put(VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(IS_DISCARD)).thenReturn("0");

		final Map<String, Object> results = this.trialMeasurementsController.updateExperimentCellData(data, req);

		assertThat("1", is(equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		assertThat(results.containsKey(TrialMeasurementsController.DATA), is(true));

		// Validation and saving of phenotype must occur when isDiscard flag is off.
		Mockito.verify(mockValidationService).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(Phenotype.class), Mockito.anyInt());

	}

	@Test
	public void testUpdateExperimentCellDataIfNotDiscardInvalidButKeep() {
		final int termId = 2000;
		final String newValue = "new value";
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		this.trialMeasurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Mockito.any(Variable.class), Mockito.anyString())).thenReturn(true);
		this.trialMeasurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(EXPERIMENT_ID, "1");
		data.put(TERM_ID, Integer.toString(termId));
		data.put(VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(IS_DISCARD)).thenReturn("0");
		Mockito.when(req.getParameter("invalidButKeep")).thenReturn("1");

		final Map<String, Object> results = this.trialMeasurementsController.updateExperimentCellData(data, req);

		assertThat("1", is(equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		assertThat(results.containsKey(TrialMeasurementsController.DATA) ,is(true));

		// Validation step should not be invoked when there is a signal to keep the value even if it is invalid.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		// But save step must be invoked.
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(Phenotype.class), Mockito.anyInt());
	}

	@Test
	public void testUpdateExperimentCellDataIfDiscard() {
		final int termId = 2000;
		final String newValue = "new value";
		final UserSelection userSelection = new UserSelection();

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(1234);
		workbook.setStudyDetails(studyDetails);
		userSelection.setWorkbook(workbook);
		this.trialMeasurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Mockito.any(Variable.class), Mockito.anyString())).thenReturn(true);

		this.trialMeasurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(termId), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(EXPERIMENT_ID, "1");
		data.put(TERM_ID, Integer.toString(termId));
		data.put(VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(IS_DISCARD)).thenReturn("1");

		final Map<String, Object> results = this.trialMeasurementsController.updateExperimentCellData(data, req);

		assertThat("1", is(equalTo((results.get(TrialMeasurementsController.SUCCESS)))));
		assertThat(results.containsKey(TrialMeasurementsController.DATA) ,is(true));

		// Validation and saving of phenotype must NOT occur when isDiscard flag is on.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager, Mockito.never()).saveOrUpdatePhenotypeValue(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(Phenotype.class), Mockito.anyInt());
	}

	@Test
	public void testMarkExperimentCellDataAsAccepted() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
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
		final Map<String, String> data = new HashMap<>();

		data.put(INDEX, "1");
		data.put(TERM_ID, Integer.toString(termId));

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		final Map<String, Object> results = this.trialMeasurementsController.markExperimentCellDataAsAccepted(data, req);

		@SuppressWarnings("unchecked") final Map<String, Object> dataMap = (Map<String, Object>) results.get(DATA);

		assertThat("The Accepted flag should be true", (boolean) ((Object[]) dataMap.get("TestVarName2"))[2] ,is(true));

	}

	@Test
	public void testMarkExperimentCellDataAsAcceptedForNumeric() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
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
		final Map<String, String> data = new HashMap<>();

		data.put(INDEX, "1");
		data.put(TERM_ID, Integer.toString(termId));

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		final Map<String, Object> results = this.trialMeasurementsController.markExperimentCellDataAsAccepted(data, req);

		@SuppressWarnings("unchecked") final Map<String, Object> dataMap = (Map<String, Object>) results.get(DATA);

		assertThat("The Accepted flag should be true", (boolean) ((Object[]) dataMap.get("TestVarName2"))[1] ,is(true));

	}

	@Test
	public void testMarkAllExperimentDataAsAccepted() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
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
		this.trialMeasurementsController.markAllExperimentDataAsAccepted();

		for (final MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (final MeasurementData var : measurementRow.getDataList()) {
					if (var != null
						&& !StringUtils.isEmpty(var.getValue())
						&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
						.getMeasurementVariable().getPossibleValues().isEmpty())) {
						Assert.assertTrue(var.isAccepted());
						Assert.assertTrue(var.getIsCustomCategoricalValue());
					} else {
						Assert.assertFalse(var.isAccepted());
						Assert.assertFalse(var.getIsCustomCategoricalValue());
					}
				}
			}
		}

	}

	@Test
	public void testMarkAllExperimentDataAsMissing() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
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

		for (final MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (final MeasurementData var : measurementRow.getDataList()) {
					if (var != null) {
						if (var != null
								&& !StringUtils.isEmpty(var.getValue())
								&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
										.getMeasurementVariable().getPossibleValues().isEmpty())) {
							assertThat(var.isAccepted(),is(true));
							if (this.trialMeasurementsController.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var
									.getMeasurementVariable().getPossibleValues())) {
								assertThat(MeasurementData.MISSING_VALUE, is(equalTo(var.getValue())));
							} else {
								assertThat("0",is(not(equalTo(var.getValue()))));
							}
						} else {
							assertThat(true,is(not((var.isAccepted()))));
						}
					}
				}
			}
		}

	}

	@Test
	public void testIsCategoricalValueOutOfBounds() {
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		possibleValues.add(new ValueReference());
		possibleValues.get(0).setId(1);
		possibleValues.get(0).setKey("1");
		possibleValues.get(1).setId(2);
		possibleValues.get(1).setKey("2");

		assertThat("2 is in possible values so the return value should be false",true,is(not(
				this.trialMeasurementsController.isCategoricalValueOutOfBounds("2", "", possibleValues))));
		assertThat("3 is NOT in possible values so the return value should be true",
				this.trialMeasurementsController.isCategoricalValueOutOfBounds("3", "", possibleValues) ,is(true));
		assertThat("2 is in possible values so the return value should be false",true,is(not(
				this.trialMeasurementsController.isCategoricalValueOutOfBounds(null, "2", possibleValues))));
		assertThat("3 is NOT in possible values so the return value should be true",
				this.trialMeasurementsController.isCategoricalValueOutOfBounds(null, "3", possibleValues) ,is(true));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsRange() {
		final MeasurementVariable var = new MeasurementVariable();
		var.setMinRange(Double.valueOf("1"));
		var.setMaxRange(Double.valueOf("10"));
		assertThat("Should return false since 2 is not out of range",true,is(not(
				this.trialMeasurementsController.isNumericalValueOutOfBounds("2", var))));
		assertThat("Should return true since 21 is out of range",
				this.trialMeasurementsController.isNumericalValueOutOfBounds("21", var));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsNoRange() {
		final MeasurementVariable var = new MeasurementVariable();

		assertThat("Should return false since 2 is not out of range",true,is(not(
				this.trialMeasurementsController.isNumericalValueOutOfBounds("2", var))));
		assertThat("Should return false since 21 is not out of range",true,is(not(
				this.trialMeasurementsController.isNumericalValueOutOfBounds("21", var))));
	}

	@Test
	public void testSetCategoricalDisplayType() throws Exception {
		// default case, api call does not include a value for showCategoricalDescriptionView, since the
		// initial value for the isCategoricalDescriptionView is FALSE, the session value will be toggled
		final HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(IS_CATEGORICAL_DESCRIPTION_VIEW)).thenReturn(Boolean.FALSE);

		final Boolean result = this.trialMeasurementsController.setCategoricalDisplayType(null, session);
		Mockito.verify(session, Mockito.times(1)).setAttribute(IS_CATEGORICAL_DESCRIPTION_VIEW, Boolean.TRUE);
		assertThat("should be true", result);
	}

	@Test
	public void testSetCategoricalDisplayTypeWithForcedCategoricalDisplayValue() throws Exception {
		// Api call includes a value for showCategoricalDescriptionView, we set the session to this value then
		// return this
		final HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(IS_CATEGORICAL_DESCRIPTION_VIEW)).thenReturn(Boolean.FALSE);

		final Boolean result = this.trialMeasurementsController.setCategoricalDisplayType(Boolean.FALSE, session);
		Mockito.verify(session, Mockito.times(1)).setAttribute(IS_CATEGORICAL_DESCRIPTION_VIEW, Boolean.FALSE);
		assertThat("should be false", true,is(not(result)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPlotMeasurementsPaginated() {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter(PAGE_NUMBER, "1");
		request.addParameter(PAGE_SIZE, "10");
		request.addParameter(SORT_BY, String.valueOf(TermId.ENTRY_NO.getId()));
		request.addParameter(SORT_ORDER, "desc");

		final String drawParamValue = "drawParamValue";
		request.addParameter(DRAW, drawParamValue);

		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(), "CategoryValue1Definition");
		// Add CROSS and STOCK measurements
		final boolean doAddNewGermplasmDescriptors = true;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1, doAddNewGermplasmDescriptors);

		this.trialMeasurementsController.setContextUtil(Mockito.mock(ContextUtil.class));

		// Method to test
		final Map<String, Object> plotMeasurementsPaginated = this.trialMeasurementsController.getPlotMeasurementsPaginated(1, 1,
				new CreateTrialForm(), Mockito.mock(Model.class), request);


		// Expecting 4 keys returned by main map: draw, recordsTotal, recordsFiltered, data
		assertThat("Expected a non-null map as return value.", plotMeasurementsPaginated,is(not(nullValue())));

		assertThat("Expected number of entries in the map did not match.", 4, is(equalTo(plotMeasurementsPaginated.size())));

		assertThat("'draw' parameter should be returned in map as per value of request parameter 'draw'.", drawParamValue,is(equalTo(
				plotMeasurementsPaginated.get(DRAW))));
		assertThat("Record count should be returned as per what is returned by studyService.countTotalObservationUnits()",
				recordsCount, is(equalTo(plotMeasurementsPaginated.get(RECORDS_TOTAL))));
		assertThat("Records filtered should be returned as per number of plots on page.", observations.size(),is(equalTo(
				plotMeasurementsPaginated.get(RECORDS_FILTERED))));
		final List<Map<String, Object>> allMeasurementData = (List<Map<String, Object>>) plotMeasurementsPaginated.get(DATA);
		assertThat("Expected a non-null data map.", allMeasurementData,is(not(nullValue())));


		final Map<String, Object> onePlotMeasurementData = allMeasurementData.get(0);
		final ObservationDto observationDto = observations.get(0);

		// Verify the factor names and values were included properly in data map
		assertThat(String.valueOf(observationDto.getMeasurementId()), is(equalTo(onePlotMeasurementData.get(EXPERIMENT_ID))));
		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(onePlotMeasurementData, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors, useDifferentLocalNames);

		this.verifyCorrectValuesForTraits(category1, onePlotMeasurementData);
		final ArgumentCaptor<Integer> pageNumberArg = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> pageSizeArg = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<String> sortByArg = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> sortOrderArg = ArgumentCaptor.forClass(String.class);

		// Verify pagination-related arguments passed to studyService
		Mockito.verify(this.studyService).getObservations(Mockito.anyInt(), Mockito.anyInt(), pageNumberArg.capture(),
				pageSizeArg.capture(), sortByArg.capture(), sortOrderArg.capture());
		assertThat(new Integer(1), is(equalTo(pageNumberArg.getValue())));
		assertThat(new Integer(10), is(equalTo(pageSizeArg.getValue())));
		assertThat(TermId.ENTRY_NO.name(), is(equalTo(sortByArg.getValue())));
		assertThat("desc", is(equalTo(sortOrderArg.getValue())));
	}

	private void verifyCorrectValuesForFactors(final Map<String, Object> onePlotMeasurementData, final ObservationDto observationDto,
		final boolean isGidDesigFactorsIncluded, final boolean isNewGermplasmDescriptorsAdded, final boolean useDifferentLocalNames) {
		// there are tests where GID and DESIGNATION variable headers are not expected to be present
		if (isGidDesigFactorsIncluded) {
			final String designationMapKey = useDifferentLocalNames ? DESIGNATION + LOCAL : DESIGNATION;
			assertThat(observationDto.getDesignation(), is(equalTo(onePlotMeasurementData.get(designationMapKey))));
			final String gidMapKey = useDifferentLocalNames ? TermId.GID.name() + LOCAL : TermId.GID.name();
			assertThat(observationDto.getGid(), is(equalTo(onePlotMeasurementData.get(gidMapKey))));
		}

		final String entryNoMapKey = useDifferentLocalNames ? TermId.ENTRY_NO.name() + LOCAL : TermId.ENTRY_NO.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getEntryNo(), false}, (Object[]) onePlotMeasurementData.get(entryNoMapKey)) ,is(true));

		final String entryCodeMapKey = useDifferentLocalNames ? TermId.ENTRY_CODE.name() + LOCAL : TermId.ENTRY_CODE.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getEntryCode(), false}, (Object[]) onePlotMeasurementData.get(entryCodeMapKey)) ,is(true));

		if (isNewGermplasmDescriptorsAdded) {
			assertThat(Arrays.equals(new Object[] {STOCK_ID_VALUE}, (Object[]) onePlotMeasurementData.get(STOCK_ID)),is(true));
			assertThat(Arrays.equals(new Object[] {CROSS_VALUE}, (Object[]) onePlotMeasurementData.get(CROSS)) ,is(true));
		}

		final String entryTypeMapKey = useDifferentLocalNames ? TermId.ENTRY_TYPE.name() + LOCAL : TermId.ENTRY_TYPE.name();
		assertThat(Arrays.equals(new Object[] {observationDto.getEntryType(), observationDto.getEntryType(), false},
			(Object[]) onePlotMeasurementData.get(entryTypeMapKey)) ,is(true));

		final String plotNoMapKey = useDifferentLocalNames ? TermId.PLOT_NO.name() + LOCAL : TermId.PLOT_NO.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getPlotNumber(), false}, (Object[]) onePlotMeasurementData.get(plotNoMapKey)) ,is(true));

		final String blockNoMapKey = useDifferentLocalNames ? TermId.BLOCK_NO.name() + LOCAL : TermId.BLOCK_NO.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getBlockNumber(), false}, (Object[]) onePlotMeasurementData.get(blockNoMapKey)) ,is(true));

		final String repNoMapKey = useDifferentLocalNames ? TermId.REP_NO.name() + LOCAL : TermId.REP_NO.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getRepitionNumber(), false}, (Object[]) onePlotMeasurementData.get(repNoMapKey)) ,is(true));

		final String trialInstanceMapKey = useDifferentLocalNames ? TRIAL_INSTANCE + LOCAL : TRIAL_INSTANCE;
		assertThat(Arrays
			.equals(new Object[] {observationDto.getTrialInstance(), false}, (Object[]) onePlotMeasurementData.get(trialInstanceMapKey)) ,is(true));

		final String rowMapKey = useDifferentLocalNames ? TermId.ROW.name() + LOCAL : TermId.ROW.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getRowNumber(), false}, (Object[]) onePlotMeasurementData.get(rowMapKey)) ,is(true));

		final String colMapKey = useDifferentLocalNames ? TermId.COL.name() + LOCAL : TermId.COL.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getColumnNumber(), false}, (Object[]) onePlotMeasurementData.get(colMapKey)) ,is(true));

		final String plotIdMapKey = useDifferentLocalNames ? TermId.PLOT_ID.name() + LOCAL : TermId.PLOT_ID.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getPlotId(), false}, (Object[]) onePlotMeasurementData.get(plotIdMapKey)) ,is(true));

		final String fieldMapColumnMapKey = useDifferentLocalNames ? TermId.FIELDMAP_COLUMN.name() + LOCAL : TermId.FIELDMAP_COLUMN.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getFieldMapColumn(), false}, (Object[]) onePlotMeasurementData.get(fieldMapColumnMapKey)) ,is(true));

		final String fieldMapRangeMapKey = useDifferentLocalNames ? TermId.FIELDMAP_RANGE.name() + LOCAL : TermId.FIELDMAP_COLUMN.name();
		assertThat(
			Arrays.equals(new Object[] {observationDto.getFieldMapRange(), false}, (Object[]) onePlotMeasurementData.get(fieldMapRangeMapKey)) ,is(true));
	}

	private List<ObservationDto> setupTestObservations(final int recordsCount, final TermSummary category1, final boolean doAddNewGermplasmDescriptors) {
		final List<MeasurementDto> measurements = Lists.newArrayList(this.measurementText, this.measurementNumeric, this.measurementCategorical);
		final ObservationDto testObservationDto =
				new ObservationDto(123, "1", "Test Entry", 300, "CML123", "5", "Entry Code", "2", "10", "3", measurements);

		if (doAddNewGermplasmDescriptors) {
			testObservationDto.additionalGermplasmDescriptor(STOCK_ID, STOCK_ID_VALUE);
			testObservationDto.additionalGermplasmDescriptor(CROSS, CROSS_VALUE);
		}

		testObservationDto.setRowNumber("11");
		testObservationDto.setColumnNumber("22");
		testObservationDto.setPlotId("9CVRPNHaSlCE1");

		final List<ObservationDto> observations = Lists.newArrayList(testObservationDto);
		Mockito.when(this.studyService.getObservations(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(observations);

		Mockito.when(this.studyService.countTotalObservationUnits(Mockito.anyInt(), Mockito.anyInt())).thenReturn(recordsCount);
		this.trialMeasurementsController.setStudyService(this.studyService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(this.measurementText.getMeasurementVariable().getId()),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableText);

		final Variable variableNumeric = new Variable();
		final Scale scaleNumeric = new Scale();
		scaleNumeric.setDataType(DataType.NUMERIC_VARIABLE);
		variableNumeric.setScale(scaleNumeric);
		Mockito.when(
				this.ontologyVariableDataManager.getVariable(Mockito.anyString(), Mockito.eq(this.measurementNumeric.getMeasurementVariable().getId()),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableNumeric);

		final Variable variableCategorical = new Variable();
		final Scale scaleCategorical = new Scale();
		scaleCategorical.setDataType(DataType.CATEGORICAL_VARIABLE);
		scaleCategorical.addCategory(category1);
		variableCategorical.setScale(scaleCategorical);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Mockito.anyString(),
				Mockito.eq(this.measurementCategorical.getMeasurementVariable().getId()),
				Matchers.eq(true), Matchers.eq(false)))
				.thenReturn(variableCategorical);
		return observations;
	}

	private void setupMeasurementVariablesInMockWorkbook(final boolean useDifferentLocalName) {
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		userSelection.setWorkbook(workbook);

		this.measurementVariables = new ArrayList<>();
		final String trait1Name = this.measurementText.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementText.getMeasurementVariable().getId(), useDifferentLocalName? trait1Name + LOCAL : trait1Name, null));
		final String trait2Name = this.measurementNumeric.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementNumeric.getMeasurementVariable().getId(), useDifferentLocalName? trait2Name + LOCAL : trait2Name, null));
		final String trait3Name = this.measurementCategorical.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementCategorical.getMeasurementVariable().getId(), useDifferentLocalName? trait3Name + LOCAL : trait3Name, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(ALEUCOL_1_5_TERM_ID
				, ALEUCOL_1_5_TRAIT_NAME, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementCategorical.getMeasurementVariable().getId(), useDifferentLocalName? trait3Name + LOCAL : trait3Name, null));

		for (final TermId term : this.standardFactors) {
			this.measurementVariables
				.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(term.getId(), useDifferentLocalName? term.name() + LOCAL : term.name(), null));
		}
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(), useDifferentLocalName? DESIGNATION + LOCAL : DESIGNATION, null));
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), useDifferentLocalName? TRIAL_INSTANCE + LOCAL : TRIAL_INSTANCE, null));

		Mockito.when(workbook.getMeasurementDatasetVariablesView()).thenReturn(this.measurementVariables);
		this.trialMeasurementsController.setUserSelection(userSelection);
	}

	@Test
	public void testConvertForCategoricalVariableBlankMeasurementDto() {

		final Map<String, Object> dataMap = new HashMap<>();

		final Variable measurementVariable = new Variable();
		final int aleucolPhenotypeId = 456;
		final String aleucolPhenotypeTraitValue = "";

		final MeasurementDto measurementDto = new MeasurementDto(new MeasurementVariableDto(ALEUCOL_1_5_TERM_ID, ALEUCOL_1_5_TRAIT_NAME),
				aleucolPhenotypeId, aleucolPhenotypeTraitValue);

		final Object[] values = this.trialMeasurementsController.convertForCategoricalVariable(measurementVariable, measurementDto.getVariableValue(),
				measurementDto.getPhenotypeId(), false);

		assertThat("", is(equalTo(values[0])));
		assertThat("", is(equalTo(values[1])));
		assertThat(false, is(equalTo(values[2])));
		assertThat(aleucolPhenotypeId, is(equalTo(values[3])));

	}


	@Test
	public void testConvertForCategoricalVariableTraitValueIsOutOfRangeFromCategoricalValues() {

		final Map<String, Object> dataMap = new HashMap<>();

		final Variable measurementVariable = this.createTestCategoricalVariable();


		final int aleucolPhenotypeId = 456;
		final String aleucolPhenotypeTraitValue = "DDD";

		final MeasurementDto measurementDto = new MeasurementDto(new MeasurementVariableDto(ALEUCOL_1_5_TERM_ID, ALEUCOL_1_5_TRAIT_NAME), aleucolPhenotypeId, aleucolPhenotypeTraitValue);

		final Object[] values = this.trialMeasurementsController.convertForCategoricalVariable(measurementVariable, measurementDto.getVariableValue(),
				measurementDto.getPhenotypeId(), false);

		assertThat(aleucolPhenotypeTraitValue, is(equalTo(values[0])));
		assertThat(aleucolPhenotypeTraitValue, is(equalTo(values[1])));
		assertThat(true, is(equalTo(values[2])));
		assertThat(aleucolPhenotypeId, is(equalTo(values[3])));

	}


	@Test
	public void testConvertForCategoricalVariableTraitValueIsWithinCategoricalValues() {

		final Map<String, Object> dataMap = new HashMap<>();

		final Variable measurementVariable = this.createTestCategoricalVariable();


		final int aleucolPhenotypeId = 456;
		final String aleucolPhenotypeTraitValue = "AAA";

		final MeasurementDto measurementDto = new MeasurementDto(new MeasurementVariableDto(ALEUCOL_1_5_TERM_ID, ALEUCOL_1_5_TRAIT_NAME), aleucolPhenotypeId, aleucolPhenotypeTraitValue);

		final Object[] values = this.trialMeasurementsController.convertForCategoricalVariable(measurementVariable, measurementDto.getVariableValue(),
				measurementDto.getPhenotypeId(), false);

		assertThat(aleucolPhenotypeTraitValue, is(equalTo(values[0])));
		assertThat("AAA Definition 1", is(equalTo(values[1])));
		assertThat(true, is(equalTo(values[2])));
		assertThat(aleucolPhenotypeId, is(equalTo(values[3])));

	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMap() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = false;
		// null because we are not interested in categorical traits for this test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.trialMeasurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables);

		assertThat(this.standardFactors.length, is(equalTo(dataMap.size())));
		// set to false because GID and DESIGNATION are not expected to be in map
		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors, useDifferentLocalNames);
		assertThat("GID should not be a key in data map.", dataMap.get(TermId.GID.name()),is(nullValue()));
		assertThat("DESIGNATION should not be a key in data map.", dataMap.get(DESIGNATION),is(nullValue()));
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithDifferentLocalNames() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = true;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = false;
		// null because we are not interested in categorical traits for this test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.trialMeasurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables);

		// Expecting that GID-local and DESIGNATION-local were added
		assertThat(this.standardFactors.length + 2, is(equalTo(dataMap.size())));
		assertThat(TermId.GID.name() + LOCAL + " was expected as key in data map but wasn't.", dataMap.get(TermId.GID.name() + LOCAL),is(not(nullValue())));
		assertThat(DESIGNATION + LOCAL + " was expected as key in data map but wasn't.", dataMap.get(DESIGNATION) + LOCAL,is(not(nullValue())));

		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors, useDifferentLocalNames);
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithAdditionalGermplasmDescriptors() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = true;
		// null because we are not interested in categorical traits for this test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.trialMeasurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables);

		// expecting CROSS and STOCK_ID to have been added
		assertThat(this.standardFactors.length + 2, is(equalTo(dataMap.size())));
		assertThat(dataMap,hasKey(CROSS));
		assertThat(dataMap,hasKey(STOCK_ID));

		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors, useDifferentLocalNames);
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithFieldMap() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = true;
		// null because we are not interested in categorical traits for this test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.trialMeasurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables);

		// expecting FIELDMAP_COLUMN and FIELDMAP_RANGE to have been added
		assertThat(this.standardFactors.length + 2, is(equalTo(dataMap.size())));
		assertThat(dataMap,hasKey(FIELDMAP_COLUMN));
		assertThat(dataMap.get(FIELDMAP_COLUMN),is(not(nullValue())));
		assertThat(dataMap,hasKey(FIELDMAP_RANGE));
		assertThat(dataMap.get(FIELDMAP_RANGE),is(not(nullValue())));


		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors, useDifferentLocalNames);
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithAdditionalDesignFactors() {

		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), ALEUCOL_1_5_TERM_ID, true, false))
				.thenReturn(this.createTestCategoricalVariable());

		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = true;
		// null because we are not interested in categorical traits for this test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		final ObservationDto observationDto = observations.get(0);
		// Add categorical design factor
		observations.get(0).additionalDesignFactor(ALEUCOL_1_5_TRAIT_NAME, "1");

		// Method to test
		this.trialMeasurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables);

		assertThat(dataMap, hasKey(ALEUCOL_1_5_TRAIT_NAME));
		assertThat(dataMap.get(ALEUCOL_1_5_TRAIT_NAME), is(not(nullValue())));

	}
	
	@Test
	public void testUpdateTraits() throws WorkbookParserException {
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook= WorkbookTestDataInitializer.getTestWorkbook();
		userSelection.setWorkbook(workbook);
		this.trialMeasurementsController.setUserSelection(userSelection );
		final CreateTrialForm form = new CreateTrialForm();
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final Model model = Mockito.mock(Model.class);

		final Map<String, String> resultMap = this.trialMeasurementsController.updateTraits(form);

		Assert.assertEquals("1", resultMap.get(TrialMeasurementsController.STATUS));
		Mockito.verify(this.validationService).validateObservationValues(workbook);
		Mockito.verify(this.fieldbookMiddlewareService).saveMeasurementRows(workbook, this.contextUtil.getCurrentProgramUUID(), true);
	}

	private Variable createTestCategoricalVariable() {

		final Variable measurementVariable = new Variable();
		final Scale scale = new Scale();
		scale.setDataType(DataType.CATEGORICAL_VARIABLE);
		scale.addCategory(new TermSummary(1, "AAA", "AAA Definition 1"));
		scale.addCategory(new TermSummary(2, "BBB", "AAA Definition 2"));
		scale.addCategory(new TermSummary(3, "CCC", "AAA Definition 3"));
		measurementVariable.setScale(scale);

		return measurementVariable;

	}

	@Test
	public void testGenerateDatatableDataMap() {
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(), "CategoryValue1Definition");
		final boolean doAddNewGermplasmDescriptors = false;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1, doAddNewGermplasmDescriptors);
		final ObservationDto observationDto = observations.get(0);

		// Method to test
		final Map<String, Object> dataMap = this.trialMeasurementsController.generateDatatableDataMap(observationDto);

		assertThat("Expected a non-null data map.", dataMap.size(),is(not(equalTo(0))));
		assertThat(String.valueOf(observationDto.getMeasurementId()), is(equalTo(dataMap.get(EXPERIMENT_ID))));
		assertThat(String.valueOf(observationDto.getMeasurementId()), is(equalTo(dataMap.get(ACTION))));

		// Verify the factor and trait names and values were included properly in data map
		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors, useDifferentLocalNames);
		this.verifyCorrectValuesForTraits(category1, dataMap);
	}

	@Test
	public void testGenerateDatatableDataMapWithDeletedTrait() {
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);
		// Remove from measurement variables the character trait, but it is still in test observations
		this.measurementVariables.remove(0);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(), "CategoryValue1Definition");
		final boolean doAddNewGermplasmDescriptors = false;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1, doAddNewGermplasmDescriptors);
		final ObservationDto observationDto = observations.get(0);

		// Method to test
		final Map<String, Object> dataMap = this.trialMeasurementsController.generateDatatableDataMap(observationDto);

		// Verify that values exist for retained traits but deleted trait is not included in data map
		assertThat(dataMap.get(this.measurementNumeric.getMeasurementVariable().getName()),is(not(nullValue())));
		assertThat(dataMap.get(this.measurementCategorical.getMeasurementVariable().getName()),is(not(nullValue())));
		assertThat(dataMap.get(this.measurementText.getMeasurementVariable().getName()),is(nullValue()));
	}

	private void verifyCorrectValuesForTraits(final TermSummary category1, final Map<String, Object> dataMap) {
		// Character Trait
		assertThat(Arrays.equals(new Object[] {this.measurementText.getVariableValue(), this.measurementText.getPhenotypeId()},
			(Object[]) dataMap.get(this.measurementText.getMeasurementVariable().getName())), is(true));

		// Numeric Trait
		assertThat(Arrays.equals(new Object[] {this.measurementNumeric.getVariableValue(), true, this.measurementNumeric.getPhenotypeId()},
			(Object[]) dataMap.get(this.measurementNumeric.getMeasurementVariable().getName())), is(true));

		// Categorical Trait
		assertThat(Arrays
			.equals(new Object[] {category1.getName(), category1.getDefinition(), true, this.measurementCategorical.getPhenotypeId()},
				(Object[]) dataMap.get(this.measurementCategorical.getMeasurementVariable().getName())), is(true));
	}
}
