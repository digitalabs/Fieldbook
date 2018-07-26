
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.ProjectPropertyTestDataInitializer;
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
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
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

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class MeasurementsControllerTest {

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
	private TrialMeasurementsController measurementsController;
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

	@Mock
	private UserSelection userSelection;

	private List<MeasurementVariable> measurementVariables;

	private final MeasurementDto measurementText = new MeasurementDto(new MeasurementVariableDto(1, "NOTES"), 1,
			"Text Notes");
	private final MeasurementDto measurementNumeric = new MeasurementDto(new MeasurementVariableDto(2, "Grain Yield"),
			2, "500");
	private final MeasurementDto measurementCategorical = new MeasurementDto(
			new MeasurementVariableDto(3, "CategoricalTrait"), 3, "CategoryValue1");

	private final TermId[] standardFactors = { TermId.GID, TermId.ENTRY_NO, TermId.ENTRY_TYPE, TermId.ENTRY_CODE,
			TermId.PLOT_NO, TermId.PLOT_ID, TermId.BLOCK_NO, TermId.REP_NO, TermId.ROW, TermId.COL,
			TermId.FIELDMAP_COLUMN, TermId.FIELDMAP_RANGE };

	@Before
	public void setUp() {
		this.measurementDataTestDataInitializer = new MeasurementDataTestDataInitializer();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId()))
				.thenReturn(new Term(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.name(), "Definition"));
	}

	@Test
	public void testCopyMeasurementValue() {

		final MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(this.generateTestDataList());
		final MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(this.generateTestDataList());

		this.measurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				MatcherAssert.assertThat(
						"The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(),
						Is.is(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				MatcherAssert.assertThat(
						"The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(),
						CoreMatchers.not(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
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

		this.measurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				MatcherAssert.assertThat(
						"The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(),
						Is.is(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				MatcherAssert.assertThat(
						"The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(),
						CoreMatchers.not(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
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

		this.measurementsController.copyMeasurementValue(origRow, valueRow);

		for (int x = 0; x < origRow.getDataList().size(); x++) {
			if (!origRow.getDataList().get(x).getMeasurementVariable().isFactor()) {
				MatcherAssert.assertThat(
						"The origRow's measurement value must be equal to the valueRow's measurement value if the variable is not a factor",
						origRow.getDataList().get(x).getValue(),
						Is.is(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
			} else {
				MatcherAssert.assertThat(
						"The origRow's measurement value must not equal to the valueRow's measurement value if the variable is a factor",
						origRow.getDataList().get(x).getValue(),
						CoreMatchers.not(CoreMatchers.equalTo(valueRow.getDataList().get(x).getValue())));
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

		this.measurementsController.copyMeasurementValue(origRow, valueRow, true);
		MatcherAssert.assertThat(origRow.getDataList().get(0).getIsCustomCategoricalValue(), Is.is(true));

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
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(), Matchers.eq(termId),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableText);

		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.editExperimentCells(experimentId, termId, null, model);
		MatcherAssert.assertThat(TermId.CATEGORICAL_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("categoricalVarId"))));
		MatcherAssert.assertThat(TermId.DATE_VARIABLE.getId(), Is.is(CoreMatchers.equalTo(model.get("dateVarId"))));
		MatcherAssert.assertThat(TermId.NUMERIC_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("numericVarId"))));
		MatcherAssert.assertThat(variableText, Is.is(CoreMatchers.equalTo(model.get("variable"))));
		MatcherAssert.assertThat(experimentId,
				Is.is(CoreMatchers.equalTo(model.get(MeasurementsControllerTest.EXPERIMENT_ID))));
		MatcherAssert.assertThat((List<?>) model.get("possibleValues"), hasSize(0));
		MatcherAssert.assertThat("", Is.is(CoreMatchers.equalTo(model.get("phenotypeId"))));
		MatcherAssert.assertThat("", Is.is(CoreMatchers.equalTo(model.get("phenotypeValue"))));
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
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		final String phenotpevalue = "2nd";
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				phenotpevalue, new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);

		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.editExperimentCells(experimentId, termId, model);
		MatcherAssert.assertThat(TermId.CATEGORICAL_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("categoricalVarId"))));
		MatcherAssert.assertThat(TermId.DATE_VARIABLE.getId(), Is.is(CoreMatchers.equalTo(model.get("dateVarId"))));
		MatcherAssert.assertThat(TermId.NUMERIC_VARIABLE.getId(),
				Is.is(CoreMatchers.equalTo(model.get("numericVarId"))));
		MatcherAssert.assertThat((List<?>) model.get("possibleValues"), hasSize(0));
		MatcherAssert.assertThat(0, Is.is(CoreMatchers.equalTo(model.get("phenotypeId"))));
		MatcherAssert.assertThat(phenotpevalue, Is.is(CoreMatchers.equalTo(model.get("phenotypeValue"))));
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
		workbook.setVariates(new ArrayList<MeasurementVariable>());
		userSelection.setWorkbook(workbook);
		this.measurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Matchers.any(Variable.class), Matchers.anyString()))
				.thenReturn(true);
		this.measurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(), Matchers.eq(termId),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(MeasurementsControllerTest.EXPERIMENT_ID, "1");
		data.put(MeasurementsControllerTest.TERM_ID, Integer.toString(termId));
		data.put(MeasurementsControllerTest.VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(MeasurementsControllerTest.IS_DISCARD)).thenReturn("0");

		final Map<String, Object> results = this.measurementsController.updateExperimentCellData(data, req);

		MatcherAssert.assertThat("1", Is.is(CoreMatchers.equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		MatcherAssert.assertThat(results.containsKey(TrialMeasurementsController.DATA), Is.is(true));

		// Validation and saving of phenotype must occur when isDiscard flag is
		// off.
		Mockito.verify(mockValidationService).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Matchers.anyInt(), Matchers.anyInt(),
				Matchers.anyString(), Matchers.any(Phenotype.class), Matchers.anyInt(), Matchers.any(Phenotype.ValueStatus.class));

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
		workbook.setVariates(new ArrayList<MeasurementVariable>());
		userSelection.setWorkbook(workbook);
		this.measurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Matchers.any(Variable.class), Matchers.anyString()))
				.thenReturn(true);
		this.measurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(), Matchers.eq(termId),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(MeasurementsControllerTest.EXPERIMENT_ID, "1");
		data.put(MeasurementsControllerTest.TERM_ID, Integer.toString(termId));
		data.put(MeasurementsControllerTest.VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(MeasurementsControllerTest.IS_DISCARD)).thenReturn("0");
		Mockito.when(req.getParameter("invalidButKeep")).thenReturn("1");

		final Map<String, Object> results = this.measurementsController.updateExperimentCellData(data, req);

		MatcherAssert.assertThat("1", Is.is(CoreMatchers.equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		MatcherAssert.assertThat(results.containsKey(TrialMeasurementsController.DATA), Is.is(true));

		// Validation step should not be invoked when there is a signal to keep
		// the value even if it is invalid.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		// But save step must be invoked.
		Mockito.verify(this.studyDataManager).saveOrUpdatePhenotypeValue(Matchers.anyInt(), Matchers.anyInt(),
				Matchers.anyString(), Matchers.any(Phenotype.class), Matchers.anyInt(), Matchers.any(Phenotype.ValueStatus.class));
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
		this.measurementsController.setUserSelection(userSelection);

		final ValidationService mockValidationService = Mockito.mock(ValidationService.class);
		Mockito.when(mockValidationService.validateObservationValue(Matchers.any(Variable.class), Matchers.anyString()))
				.thenReturn(true);

		this.measurementsController.setValidationService(mockValidationService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(), Matchers.eq(termId),
				Matchers.eq(true), Matchers.eq(false))).thenReturn(variableText);

		final Map<String, String> data = new HashMap<String, String>();
		data.put(MeasurementsControllerTest.EXPERIMENT_ID, "1");
		data.put(MeasurementsControllerTest.TERM_ID, Integer.toString(termId));
		data.put(MeasurementsControllerTest.VALUE, newValue);

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameter(MeasurementsControllerTest.IS_DISCARD)).thenReturn("1");

		final Map<String, Object> results = this.measurementsController.updateExperimentCellData(data, req);

		MatcherAssert.assertThat("1", Is.is(CoreMatchers.equalTo(results.get(TrialMeasurementsController.SUCCESS))));
		MatcherAssert.assertThat(results.containsKey(TrialMeasurementsController.DATA), Is.is(true));

		// Validation and saving of phenotype must NOT occur when isDiscard flag
		// is on.
		Mockito.verify(mockValidationService, Mockito.never()).validateObservationValue(variableText, newValue);
		Mockito.verify(this.studyDataManager, Mockito.never()).saveOrUpdatePhenotypeValue(Matchers.anyInt(),
				Matchers.anyInt(), Matchers.anyString(), Matchers.any(Phenotype.class), Matchers.anyInt(), Matchers.any(Phenotype.ValueStatus.class));
	}

	@Test
	public void testMarkExperimentCellDataAsAccepted() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));

		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				"2nd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.setValidationService(Mockito.mock(ValidationService.class));
		final Map<String, String> data = new HashMap<>();

		data.put(MeasurementsControllerTest.INDEX, "1");
		data.put(MeasurementsControllerTest.TERM_ID, Integer.toString(termId));

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		final Map<String, Object> results = this.measurementsController.markExperimentCellDataAsAccepted(data,
				req);

		@SuppressWarnings("unchecked")
		final Map<String, Object> dataMap = (Map<String, Object>) results.get(MeasurementsControllerTest.DATA);

		MatcherAssert.assertThat("The Accepted flag should be true",
				(boolean) ((Object[]) dataMap.get("TestVarName2"))[2], Is.is(true));

	}

	@Test
	public void testMarkExperimentCellDataAsAcceptedForNumeric() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(termId, "TestVarName2", "1",
				TermId.NUMERIC_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.setValidationService(Mockito.mock(ValidationService.class));
		final Map<String, String> data = new HashMap<>();

		data.put(MeasurementsControllerTest.INDEX, "1");
		data.put(MeasurementsControllerTest.TERM_ID, Integer.toString(termId));

		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		final Map<String, Object> results = this.measurementsController.markExperimentCellDataAsAccepted(data,
				req);

		@SuppressWarnings("unchecked")
		final Map<String, Object> dataMap = (Map<String, Object>) results.get(MeasurementsControllerTest.DATA);

		MatcherAssert.assertThat("The Accepted flag should be true",
				(boolean) ((Object[]) dataMap.get("TestVarName2"))[1], Is.is(true));

	}

	@Test
	public void testMarkAllExperimentDataAsAccepted() {
		final int termId = 2000;
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				"2nd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName3",
				"3rd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.markAllExperimentDataAsAccepted();

		for (final MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (final MeasurementData var : measurementRow.getDataList()) {
					if (var != null && !StringUtils.isEmpty(var.getValue())
							&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
									|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
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
		dataList.add(this.measurementDataTestDataInitializer.createMeasurementData(1000, "TestVarName1", "1st",
				TermId.CHARACTER_VARIABLE));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName2",
				"2nd", new ArrayList<ValueReference>()));
		row.setDataList(dataList);
		measurementRowList.add(row);
		dataList = new ArrayList<>();
		dataList.add(this.measurementDataTestDataInitializer.createCategoricalMeasurementData(termId, "TestVarName3",
				"3rd", new ArrayList<ValueReference>()));

		row.setDataList(dataList);
		measurementRowList.add(row);

		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));

		this.measurementsController.setUserSelection(userSelection);
		this.measurementsController.markAllExperimentDataAsMissing();

		for (final MeasurementRow measurementRow : userSelection.getMeasurementRowList()) {
			if (measurementRow != null && measurementRow.getMeasurementVariables() != null) {
				for (final MeasurementData var : measurementRow.getDataList()) {
					if (var != null) {
						if (var != null && !StringUtils.isEmpty(var.getValue())
								&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
										|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
							MatcherAssert.assertThat(var.isAccepted(), Is.is(true));
							if (this.measurementsController.isCategoricalValueOutOfBounds(var.getcValueId(),
									var.getValue(), var.getMeasurementVariable().getPossibleValues())) {
								MatcherAssert.assertThat(MeasurementData.MISSING_VALUE,
										Is.is(CoreMatchers.equalTo(var.getValue())));
							} else {
								MatcherAssert.assertThat("0",
										Is.is(CoreMatchers.not(CoreMatchers.equalTo(var.getValue()))));
							}
						} else {
							MatcherAssert.assertThat(true, Is.is(CoreMatchers.not(var.isAccepted())));
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

		MatcherAssert.assertThat("2 is in possible values so the return value should be false", true, Is.is(CoreMatchers
				.not(this.measurementsController.isCategoricalValueOutOfBounds("2", "", possibleValues))));
		MatcherAssert.assertThat("3 is NOT in possible values so the return value should be true",
				this.measurementsController.isCategoricalValueOutOfBounds("3", "", possibleValues), Is.is(true));
		MatcherAssert.assertThat("2 is in possible values so the return value should be false", true, Is.is(CoreMatchers
				.not(this.measurementsController.isCategoricalValueOutOfBounds(null, "2", possibleValues))));
		MatcherAssert.assertThat("3 is NOT in possible values so the return value should be true",
				this.measurementsController.isCategoricalValueOutOfBounds(null, "3", possibleValues), Is.is(true));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsRange() {
		final MeasurementVariable var = new MeasurementVariable();
		var.setMinRange(Double.valueOf("1"));
		var.setMaxRange(Double.valueOf("10"));
		MatcherAssert.assertThat("Should return false since 2 is not out of range", true,
				Is.is(CoreMatchers.not(this.measurementsController.isNumericalValueOutOfBounds("2", var))));
		MatcherAssert.assertThat("Should return true since 21 is out of range",
				this.measurementsController.isNumericalValueOutOfBounds("21", var));
	}

	@Test
	public void testIsNumericalValueOutOfBoundsWhenThereIsNoRange() {
		final MeasurementVariable var = new MeasurementVariable();

		MatcherAssert.assertThat("Should return false since 2 is not out of range", true,
				Is.is(CoreMatchers.not(this.measurementsController.isNumericalValueOutOfBounds("2", var))));
		MatcherAssert.assertThat("Should return false since 21 is not out of range", true,
				Is.is(CoreMatchers.not(this.measurementsController.isNumericalValueOutOfBounds("21", var))));
	}

	@Test
	public void testSetCategoricalDisplayType() throws Exception {
		// default case, api call does not include a value for
		// showCategoricalDescriptionView, since the
		// initial value for the isCategoricalDescriptionView is FALSE, the
		// session value will be toggled
		final HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(MeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW))
				.thenReturn(Boolean.FALSE);

		final Boolean result = this.measurementsController.setCategoricalDisplayType(null, session);
		Mockito.verify(session, Mockito.times(1))
				.setAttribute(MeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW, Boolean.TRUE);
		MatcherAssert.assertThat("should be true", result);
	}

	@Test
	public void testSetCategoricalDisplayTypeWithForcedCategoricalDisplayValue() throws Exception {
		// Api call includes a value for showCategoricalDescriptionView, we set
		// the session to this value then
		// return this
		final HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(MeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW))
				.thenReturn(Boolean.FALSE);

		final Boolean result = this.measurementsController.setCategoricalDisplayType(Boolean.FALSE, session);
		Mockito.verify(session, Mockito.times(1))
				.setAttribute(MeasurementsControllerTest.IS_CATEGORICAL_DESCRIPTION_VIEW, Boolean.FALSE);
		MatcherAssert.assertThat("should be false", true, Is.is(CoreMatchers.not(result)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPlotMeasurementsPaginated() {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter(MeasurementsControllerTest.PAGE_NUMBER, "1");
		request.addParameter(MeasurementsControllerTest.PAGE_SIZE, "10");
		request.addParameter(MeasurementsControllerTest.SORT_BY, String.valueOf(TermId.ENTRY_NO.getId()));
		request.addParameter(MeasurementsControllerTest.SORT_ORDER, "desc");

		final String drawParamValue = "drawParamValue";
		request.addParameter(MeasurementsControllerTest.DRAW, drawParamValue);

		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(),
				"CategoryValue1Definition");
		// Add CROSS and STOCK measurements
		final boolean doAddNewGermplasmDescriptors = true;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1,
				doAddNewGermplasmDescriptors);

		this.measurementsController.setContextUtil(Mockito.mock(ContextUtil.class));

		// Method to test
		final Map<String, Object> plotMeasurementsPaginated = this.measurementsController.getPlotMeasurementsPaginated(1, 1,
				new CreateTrialForm(), Mockito.mock(Model.class), request);


		// Expecting 4 keys returned by main map: draw, recordsTotal,
		// recordsFiltered, data
		MatcherAssert.assertThat("Expected a non-null map as return value.", plotMeasurementsPaginated,
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		MatcherAssert.assertThat("Expected number of entries in the map did not match.", 4,
				Is.is(CoreMatchers.equalTo(plotMeasurementsPaginated.size())));

		MatcherAssert.assertThat("'draw' parameter should be returned in map as per value of request parameter 'draw'.",
				drawParamValue,
				Is.is(CoreMatchers.equalTo(plotMeasurementsPaginated.get(MeasurementsControllerTest.DRAW))));
		MatcherAssert.assertThat(
				"Record count should be returned as per what is returned by studyService.countTotalObservationUnits()",
				recordsCount, Is.is(CoreMatchers
						.equalTo(plotMeasurementsPaginated.get(MeasurementsControllerTest.RECORDS_TOTAL))));
		MatcherAssert.assertThat("Records filtered should be returned as per number of plots on page.",
				observations.size(), Is.is(CoreMatchers
						.equalTo(plotMeasurementsPaginated.get(MeasurementsControllerTest.RECORDS_FILTERED))));
		final List<Map<String, Object>> allMeasurementData = (List<Map<String, Object>>) plotMeasurementsPaginated
				.get(MeasurementsControllerTest.DATA);
		MatcherAssert.assertThat("Expected a non-null data map.", allMeasurementData,
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		final Map<String, Object> onePlotMeasurementData = allMeasurementData.get(0);
		final ObservationDto observationDto = observations.get(0);

		// Verify the factor names and values were included properly in data map
		MatcherAssert.assertThat(String.valueOf(observationDto.getMeasurementId()),
				Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(MeasurementsControllerTest.EXPERIMENT_ID))));
		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(onePlotMeasurementData, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);

		this.verifyCorrectValuesForTraits(category1, onePlotMeasurementData);
		final ArgumentCaptor<Integer> pageNumberArg = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> pageSizeArg = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<String> sortByArg = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> sortOrderArg = ArgumentCaptor.forClass(String.class);

		// Verify pagination-related arguments passed to studyService
		Mockito.verify(this.studyService).getObservations(Matchers.anyInt(), Matchers.anyInt(), pageNumberArg.capture(),
				pageSizeArg.capture(), sortByArg.capture(), sortOrderArg.capture());
		MatcherAssert.assertThat(new Integer(1), Is.is(CoreMatchers.equalTo(pageNumberArg.getValue())));
		MatcherAssert.assertThat(new Integer(10), Is.is(CoreMatchers.equalTo(pageSizeArg.getValue())));
		MatcherAssert.assertThat(TermId.ENTRY_NO.name(), Is.is(CoreMatchers.equalTo(sortByArg.getValue())));
		MatcherAssert.assertThat("desc", Is.is(CoreMatchers.equalTo(sortOrderArg.getValue())));
	}

	private void verifyCorrectValuesForFactors(final Map<String, Object> onePlotMeasurementData,
			final ObservationDto observationDto, final boolean isGidDesigFactorsIncluded,
			final boolean isNewGermplasmDescriptorsAdded, final boolean useDifferentLocalNames) {
		// there are tests where GID and DESIGNATION variable headers are not
		// expected to be present
		if (isGidDesigFactorsIncluded) {
			final String designationMapKey = useDifferentLocalNames
					? MeasurementsControllerTest.DESIGNATION + MeasurementsControllerTest.LOCAL
					: MeasurementsControllerTest.DESIGNATION;
			MatcherAssert.assertThat(observationDto.getDesignation(),
					Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(designationMapKey))));
			final String gidMapKey = useDifferentLocalNames ? TermId.GID.name() + MeasurementsControllerTest.LOCAL
					: TermId.GID.name();
			MatcherAssert.assertThat(observationDto.getGid(),
					Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(gidMapKey))));
		}

		final String entryNoMapKey = useDifferentLocalNames
				? TermId.ENTRY_NO.name() + MeasurementsControllerTest.LOCAL : TermId.ENTRY_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getEntryNo(), false },
				(Object[]) onePlotMeasurementData.get(entryNoMapKey)), Is.is(true));

		final String entryCodeMapKey = useDifferentLocalNames
				? TermId.ENTRY_CODE.name() + MeasurementsControllerTest.LOCAL : TermId.ENTRY_CODE.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getEntryCode(), false },
				(Object[]) onePlotMeasurementData.get(entryCodeMapKey)), Is.is(true));

		if (isNewGermplasmDescriptorsAdded) {
			MatcherAssert.assertThat(
					Arrays.equals(new Object[] { MeasurementsControllerTest.STOCK_ID_VALUE },
							(Object[]) onePlotMeasurementData.get(MeasurementsControllerTest.STOCK_ID)),
					Is.is(true));
			MatcherAssert.assertThat(Arrays.equals(new Object[] { MeasurementsControllerTest.CROSS_VALUE },
					(Object[]) onePlotMeasurementData.get(MeasurementsControllerTest.CROSS)), Is.is(true));
		}

		final String entryTypeMapKey = useDifferentLocalNames
				? TermId.ENTRY_TYPE.name() + MeasurementsControllerTest.LOCAL : TermId.ENTRY_TYPE.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] { observationDto.getEntryType(), observationDto.getEntryType(), false },
						(Object[]) onePlotMeasurementData.get(entryTypeMapKey)),
				Is.is(true));

		final String plotNoMapKey = useDifferentLocalNames
				? TermId.PLOT_NO.name() + MeasurementsControllerTest.LOCAL : TermId.PLOT_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getPlotNumber(), false },
				(Object[]) onePlotMeasurementData.get(plotNoMapKey)), Is.is(true));

		final String blockNoMapKey = useDifferentLocalNames
				? TermId.BLOCK_NO.name() + MeasurementsControllerTest.LOCAL : TermId.BLOCK_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getBlockNumber(), false },
				(Object[]) onePlotMeasurementData.get(blockNoMapKey)), Is.is(true));

		final String repNoMapKey = useDifferentLocalNames ? TermId.REP_NO.name() + MeasurementsControllerTest.LOCAL
				: TermId.REP_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getRepitionNumber(), false },
				(Object[]) onePlotMeasurementData.get(repNoMapKey)), Is.is(true));

		final String trialInstanceMapKey = useDifferentLocalNames
				? MeasurementsControllerTest.TRIAL_INSTANCE + MeasurementsControllerTest.LOCAL
				: MeasurementsControllerTest.TRIAL_INSTANCE;
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getTrialInstance(), false },
				(Object[]) onePlotMeasurementData.get(trialInstanceMapKey)), Is.is(true));

		final String rowMapKey = useDifferentLocalNames ? TermId.ROW.name() + MeasurementsControllerTest.LOCAL
				: TermId.ROW.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getRowNumber(), false },
				(Object[]) onePlotMeasurementData.get(rowMapKey)), Is.is(true));

		final String colMapKey = useDifferentLocalNames ? TermId.COL.name() + MeasurementsControllerTest.LOCAL
				: TermId.COL.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getColumnNumber(), false },
				(Object[]) onePlotMeasurementData.get(colMapKey)), Is.is(true));

		final String plotIdMapKey = useDifferentLocalNames
				? TermId.PLOT_ID.name() + MeasurementsControllerTest.LOCAL : TermId.PLOT_ID.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getPlotId(), false },
				(Object[]) onePlotMeasurementData.get(plotIdMapKey)), Is.is(true));

		final String fieldMapColumnMapKey = useDifferentLocalNames
				? TermId.FIELDMAP_COLUMN.name() + MeasurementsControllerTest.LOCAL : TermId.FIELDMAP_COLUMN.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getFieldMapColumn(), false },
				(Object[]) onePlotMeasurementData.get(fieldMapColumnMapKey)), Is.is(true));

		final String fieldMapRangeMapKey = useDifferentLocalNames
				? TermId.FIELDMAP_RANGE.name() + MeasurementsControllerTest.LOCAL : TermId.FIELDMAP_COLUMN.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getFieldMapRange(), false },
				(Object[]) onePlotMeasurementData.get(fieldMapRangeMapKey)), Is.is(true));
	}

	private List<ObservationDto> setupTestObservations(final int recordsCount, final TermSummary category1,
			final boolean doAddNewGermplasmDescriptors) {
		final List<MeasurementDto> measurements = Lists.newArrayList(this.measurementText, this.measurementNumeric,
				this.measurementCategorical);
		final ObservationDto testObservationDto = new ObservationDto(123, "1", "Test Entry", 300, "CML123", "5",
				"Entry Code", "2", "10", "3", measurements);

		if (doAddNewGermplasmDescriptors) {
			testObservationDto.additionalGermplasmDescriptor(MeasurementsControllerTest.STOCK_ID,
					MeasurementsControllerTest.STOCK_ID_VALUE);
			testObservationDto.additionalGermplasmDescriptor(MeasurementsControllerTest.CROSS,
					MeasurementsControllerTest.CROSS_VALUE);
		}

		testObservationDto.setRowNumber("11");
		testObservationDto.setColumnNumber("22");
		testObservationDto.setPlotId("9CVRPNHaSlCE1");

		final List<ObservationDto> observations = Lists.newArrayList(testObservationDto);
		Mockito.when(this.studyService.getObservations(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(),
				Matchers.anyInt(), Matchers.anyString(), Matchers.anyString())).thenReturn(observations);

		Mockito.when(this.studyService.countTotalObservationUnits(Matchers.anyInt(), Matchers.anyInt()))
				.thenReturn(recordsCount);
		this.measurementsController.setStudyService(this.studyService);

		final Variable variableText = new Variable();
		final Scale scaleText = new Scale();
		scaleText.setDataType(DataType.CHARACTER_VARIABLE);
		variableText.setScale(scaleText);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(),
				Matchers.eq(this.measurementText.getMeasurementVariable().getId()), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableText);

		final Variable variableNumeric = new Variable();
		final Scale scaleNumeric = new Scale();
		scaleNumeric.setDataType(DataType.NUMERIC_VARIABLE);
		variableNumeric.setScale(scaleNumeric);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(),
				Matchers.eq(this.measurementNumeric.getMeasurementVariable().getId()), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableNumeric);

		final Variable variableCategorical = new Variable();
		final Scale scaleCategorical = new Scale();
		scaleCategorical.setDataType(DataType.CATEGORICAL_VARIABLE);
		scaleCategorical.addCategory(category1);
		variableCategorical.setScale(scaleCategorical);
		Mockito.when(this.ontologyVariableDataManager.getVariable(Matchers.anyString(),
				Matchers.eq(this.measurementCategorical.getMeasurementVariable().getId()), Matchers.eq(true),
				Matchers.eq(false))).thenReturn(variableCategorical);
		return observations;
	}

	private void setupMeasurementVariablesInMockWorkbook(final boolean useDifferentLocalName) {
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		userSelection.setWorkbook(workbook);

		this.measurementVariables = new ArrayList<>();
		final String trait1Name = this.measurementText.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementText.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait1Name + MeasurementsControllerTest.LOCAL : trait1Name, null));
		final String trait2Name = this.measurementNumeric.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementNumeric.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait2Name + MeasurementsControllerTest.LOCAL : trait2Name, null));
		final String trait3Name = this.measurementCategorical.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementCategorical.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait3Name + MeasurementsControllerTest.LOCAL : trait3Name, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				MeasurementsControllerTest.ALEUCOL_1_5_TERM_ID,
				MeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementCategorical.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait3Name + MeasurementsControllerTest.LOCAL : trait3Name, null));

		for (final TermId term : this.standardFactors) {
			this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(term.getId(),
					useDifferentLocalName ? term.name() + MeasurementsControllerTest.LOCAL : term.name(), null));
		}
		this.measurementVariables
				.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(),
						useDifferentLocalName
								? MeasurementsControllerTest.DESIGNATION + MeasurementsControllerTest.LOCAL
								: MeasurementsControllerTest.DESIGNATION,
						null));
		this.measurementVariables.add(
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),
						useDifferentLocalName
								? MeasurementsControllerTest.TRIAL_INSTANCE + MeasurementsControllerTest.LOCAL
								: MeasurementsControllerTest.TRIAL_INSTANCE,
						null));

		Mockito.when(workbook.getMeasurementDatasetVariablesView()).thenReturn(this.measurementVariables);
		this.measurementsController.setUserSelection(userSelection);
	}

	@Test
	public void testConvertForCategoricalVariableBlankMeasurementDto() {
		final Variable measurementVariable = new Variable();
		final int aleucolPhenotypeId = 456;
		final String aleucolPhenotypeTraitValue = "";

		final MeasurementDto measurementDto = new MeasurementDto(
				new MeasurementVariableDto(MeasurementsControllerTest.ALEUCOL_1_5_TERM_ID,
						MeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME),
				aleucolPhenotypeId, aleucolPhenotypeTraitValue);

		final Object[] values = this.measurementsController.convertForCategoricalVariable(measurementVariable,
				measurementDto.getVariableValue(), measurementDto.getPhenotypeId(), false);

		MatcherAssert.assertThat("", Is.is(CoreMatchers.equalTo(values[0])));
		MatcherAssert.assertThat("", Is.is(CoreMatchers.equalTo(values[1])));
		MatcherAssert.assertThat(false, Is.is(CoreMatchers.equalTo(values[2])));
		MatcherAssert.assertThat(aleucolPhenotypeId, Is.is(CoreMatchers.equalTo(values[3])));

	}

	@Test
	public void testConvertForCategoricalVariableTraitValueIsOutOfRangeFromCategoricalValues() {
		final Variable measurementVariable = this.createTestCategoricalVariable();

		final int aleucolPhenotypeId = 456;
		final String aleucolPhenotypeTraitValue = "DDD";

		final MeasurementDto measurementDto = new MeasurementDto(
				new MeasurementVariableDto(MeasurementsControllerTest.ALEUCOL_1_5_TERM_ID,
						MeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME),
				aleucolPhenotypeId, aleucolPhenotypeTraitValue);

		final Object[] values = this.measurementsController.convertForCategoricalVariable(measurementVariable,
				measurementDto.getVariableValue(), measurementDto.getPhenotypeId(), false);

		MatcherAssert.assertThat(aleucolPhenotypeTraitValue, Is.is(CoreMatchers.equalTo(values[0])));
		MatcherAssert.assertThat(aleucolPhenotypeTraitValue, Is.is(CoreMatchers.equalTo(values[1])));
		MatcherAssert.assertThat(true, Is.is(CoreMatchers.equalTo(values[2])));
		MatcherAssert.assertThat(aleucolPhenotypeId, Is.is(CoreMatchers.equalTo(values[3])));

	}

	@Test
	public void testConvertForCategoricalVariableTraitValueIsWithinCategoricalValues() {
		final Variable measurementVariable = this.createTestCategoricalVariable();

		final int aleucolPhenotypeId = 456;
		final String aleucolPhenotypeTraitValue = "AAA";

		final MeasurementDto measurementDto = new MeasurementDto(
				new MeasurementVariableDto(MeasurementsControllerTest.ALEUCOL_1_5_TERM_ID,
						MeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME),
				aleucolPhenotypeId, aleucolPhenotypeTraitValue);

		final Object[] values = this.measurementsController.convertForCategoricalVariable(measurementVariable,
				measurementDto.getVariableValue(), measurementDto.getPhenotypeId(), false);

		MatcherAssert.assertThat(aleucolPhenotypeTraitValue, Is.is(CoreMatchers.equalTo(values[0])));
		MatcherAssert.assertThat("AAA Definition 1", Is.is(CoreMatchers.equalTo(values[1])));
		MatcherAssert.assertThat(true, Is.is(CoreMatchers.equalTo(values[2])));
		MatcherAssert.assertThat(aleucolPhenotypeId, Is.is(CoreMatchers.equalTo(values[3])));

	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMap() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = false;
		// null because we are not interested in categorical traits for this
		// test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.measurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap,
				this.measurementVariables, new HashMap<String, String>());

		MatcherAssert.assertThat(this.standardFactors.length, Is.is(CoreMatchers.equalTo(dataMap.size())));
		// set to false because GID and DESIGNATION are not expected to be in
		// map
		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);
		MatcherAssert.assertThat("GID should not be a key in data map.", dataMap.get(TermId.GID.name()),
				Is.is(CoreMatchers.nullValue()));
		MatcherAssert.assertThat("DESIGNATION should not be a key in data map.",
				dataMap.get(MeasurementsControllerTest.DESIGNATION), Is.is(CoreMatchers.nullValue()));
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithDifferentLocalNames() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = true;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = false;
		// null because we are not interested in categorical traits for this
		// test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.measurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap,
				this.measurementVariables, new HashMap<String, String>());

		// Expecting that GID-local and DESIGNATION-local were added
		MatcherAssert.assertThat(this.standardFactors.length + 2, Is.is(CoreMatchers.equalTo(dataMap.size())));
		MatcherAssert.assertThat(
				TermId.GID.name() + MeasurementsControllerTest.LOCAL
						+ " was expected as key in data map but wasn't.",
				dataMap.get(TermId.GID.name() + MeasurementsControllerTest.LOCAL),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(
				MeasurementsControllerTest.DESIGNATION + MeasurementsControllerTest.LOCAL
						+ " was expected as key in data map but wasn't.",
				dataMap.get(MeasurementsControllerTest.DESIGNATION) + MeasurementsControllerTest.LOCAL,
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithAdditionalGermplasmDescriptors() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = true;
		// null because we are not interested in categorical traits for this
		// test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.measurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap,
				this.measurementVariables, new HashMap<String, String>());

		// expecting CROSS and STOCK_ID to have been added
		MatcherAssert.assertThat(this.standardFactors.length + 2, Is.is(CoreMatchers.equalTo(dataMap.size())));
		MatcherAssert.assertThat(dataMap, hasKey(MeasurementsControllerTest.CROSS));
		MatcherAssert.assertThat(dataMap, hasKey(MeasurementsControllerTest.STOCK_ID));

		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithFieldMap() {
		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = true;
		// null because we are not interested in categorical traits for this
		// test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		// Method to test
		final ObservationDto observationDto = observations.get(0);
		this.measurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap,
				this.measurementVariables, new HashMap<String, String>());

		// expecting FIELDMAP_COLUMN and FIELDMAP_RANGE to have been added
		MatcherAssert.assertThat(this.standardFactors.length + 2, Is.is(CoreMatchers.equalTo(dataMap.size())));
		MatcherAssert.assertThat(dataMap, hasKey(MeasurementsControllerTest.FIELDMAP_COLUMN));
		MatcherAssert.assertThat(dataMap.get(MeasurementsControllerTest.FIELDMAP_COLUMN),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(dataMap, hasKey(MeasurementsControllerTest.FIELDMAP_RANGE));
		MatcherAssert.assertThat(dataMap.get(MeasurementsControllerTest.FIELDMAP_RANGE),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithAdditionalDesignFactors() {

		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				MeasurementsControllerTest.ALEUCOL_1_5_TERM_ID, true, false))
				.thenReturn(this.createTestCategoricalVariable());

		final Map<String, Object> dataMap = new HashMap<>();
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final boolean doAddNewGermplasmDescriptors = true;
		// null because we are not interested in categorical traits for this
		// test method
		final List<ObservationDto> observations = this.setupTestObservations(1, null, doAddNewGermplasmDescriptors);

		final ObservationDto observationDto = observations.get(0);
		// Add categorical design factor
		observations.get(0).additionalDesignFactor(MeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME, "1");

		// Method to test
		this.measurementsController.addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap,
				this.measurementVariables, new HashMap<String, String>());

		MatcherAssert.assertThat(dataMap, hasKey(MeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME));
		MatcherAssert.assertThat(dataMap.get(MeasurementsControllerTest.ALEUCOL_1_5_TRAIT_NAME),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

	}

	@Test
	public void testUpdateTraits() throws WorkbookParserException {
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		userSelection.setWorkbook(workbook);
		this.measurementsController.setUserSelection(userSelection );
		final CreateTrialForm form = new CreateTrialForm();
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final Model model = Mockito.mock(Model.class);

		final Map<String, String> resultMap = this.measurementsController.updateTraits(form);

		Assert.assertEquals("1", resultMap.get(TrialMeasurementsController.STATUS));
		Mockito.verify(this.validationService).validateObservationValues(workbook);
		Mockito.verify(this.fieldbookMiddlewareService).saveMeasurementRows(workbook,
				this.contextUtil.getCurrentProgramUUID(), true);
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
	public void testCreateNameToAliasMap() {
		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(workbook.getMeasurementDatasetVariablesView())
				.thenReturn(Arrays.asList(MeasurementVariableTestDataInitializer
						.createMeasurementVariable(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name(), "1-1")));
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementDatasetId(Matchers.anyInt(), Matchers.anyString()))
				.thenReturn(1);
		final String alias = "PlotCode";
		Mockito.when(this.ontologyDataManager.getProjectPropertiesByProjectId(Matchers.anyInt())).thenReturn(Arrays
				.asList(ProjectPropertyTestDataInitializer.createProjectProperty(alias, TermId.PLOT_CODE.getId())));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.PLOT_CODE.getId()))
				.thenReturn(new Term(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name(), TermId.PLOT_CODE.name()));

		final Map<String, String> nameToAliasMap = this.measurementsController.createNameToAliasMap(1);
		Assert.assertEquals(1, nameToAliasMap.size());
		Assert.assertTrue(nameToAliasMap.keySet().contains(TermId.PLOT_CODE.name()));
		Assert.assertEquals(alias, nameToAliasMap.get(TermId.PLOT_CODE.name()));
	}

	@Test
	public void testGenerateDatatableDataMap() {
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(),
				"CategoryValue1Definition");
		final boolean doAddNewGermplasmDescriptors = false;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1,
				doAddNewGermplasmDescriptors);
		final ObservationDto observationDto = observations.get(0);

		// Method to test
		final Map<String, Object> dataMap = this.measurementsController.generateDatatableDataMap(observationDto,
				new HashMap<String, String>());

		MatcherAssert.assertThat("Expected a non-null data map.", dataMap.size(),
				Is.is(CoreMatchers.not(CoreMatchers.equalTo(0))));
		MatcherAssert.assertThat(String.valueOf(observationDto.getMeasurementId()),
				Is.is(CoreMatchers.equalTo(dataMap.get(MeasurementsControllerTest.EXPERIMENT_ID))));
		MatcherAssert.assertThat(String.valueOf(observationDto.getMeasurementId()),
				Is.is(CoreMatchers.equalTo(dataMap.get(MeasurementsControllerTest.ACTION))));

		// Verify the factor and trait names and values were included properly
		// in data map
		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);
		this.verifyCorrectValuesForTraits(category1, dataMap);
	}

	@Test
	public void testGenerateDatatableDataMapWithDeletedTrait() {
		final boolean useDifferentLocalNames = false;
		this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);
		// Remove from measurement variables the character trait, but it is
		// still in test observations
		this.measurementVariables.remove(0);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(),
				"CategoryValue1Definition");
		final boolean doAddNewGermplasmDescriptors = false;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1,
				doAddNewGermplasmDescriptors);
		final ObservationDto observationDto = observations.get(0);

		// Method to test
		final Map<String, Object> dataMap = this.measurementsController.generateDatatableDataMap(observationDto,
				new HashMap<String, String>());

		// Verify that values exist for retained traits but deleted trait is not
		// included in data map
		MatcherAssert.assertThat(dataMap.get(this.measurementNumeric.getMeasurementVariable().getName()),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(dataMap.get(this.measurementCategorical.getMeasurementVariable().getName()),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(dataMap.get(this.measurementText.getMeasurementVariable().getName()),
				Is.is(CoreMatchers.nullValue()));
	}

	private void verifyCorrectValuesForTraits(final TermSummary category1, final Map<String, Object> dataMap) {
		// Character Trait
		MatcherAssert
				.assertThat(
						Arrays.equals(
								new Object[] { this.measurementText.getVariableValue(),
										this.measurementText.getPhenotypeId() },
								(Object[]) dataMap.get(this.measurementText.getMeasurementVariable().getName())),
						Is.is(true));

		// Numeric Trait
		MatcherAssert.assertThat(
				Arrays.equals(
						new Object[] { this.measurementNumeric.getVariableValue(), true,
								this.measurementNumeric.getPhenotypeId() },
						(Object[]) dataMap.get(this.measurementNumeric.getMeasurementVariable().getName())),
				Is.is(true));

		// Categorical Trait
		MatcherAssert.assertThat(
				Arrays.equals(
						new Object[] { category1.getName(), category1.getDefinition(), true,
								this.measurementCategorical.getPhenotypeId() },
						(Object[]) dataMap.get(this.measurementCategorical.getMeasurementVariable().getName())),
				Is.is(true));
	}
}
