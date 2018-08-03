package com.efficio.fieldbook.web.common.util;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.controller.TrialMeasurementsController;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.google.common.collect.Lists;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
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
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;

@RunWith(MockitoJUnitRunner.class)
public class DataMapUtilTest {


	private static final String CROSS_VALUE = "ABC12/XYZ34";
	private static final String STOCK_ID_VALUE = "STCK-123";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String DESIGNATION = "DESIGNATION";
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
			"Text Notes", null);
	private final MeasurementDto measurementNumeric = new MeasurementDto(new MeasurementVariableDto(2, "Grain Yield"),
			2, "500", null);
	private final MeasurementDto measurementCategorical = new MeasurementDto(
			new MeasurementVariableDto(3, "CategoricalTrait"), 3, "CategoryValue1", null);

	private final TermId[] standardFactors = { TermId.GID, TermId.ENTRY_NO, TermId.ENTRY_TYPE, TermId.ENTRY_CODE,
			TermId.PLOT_NO, TermId.PLOT_ID, TermId.BLOCK_NO, TermId.REP_NO, TermId.ROW, TermId.COL,
			TermId.FIELDMAP_COLUMN, TermId.FIELDMAP_RANGE };

	@Before
	public void setUp() {
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId()))
				.thenReturn(new Term(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.name(), "Definition"));
	}

	private List<ObservationDto> setupTestObservations(final int recordsCount, final TermSummary category1,
			final boolean doAddNewGermplasmDescriptors) {
		final List<MeasurementDto> measurements = Lists
				.newArrayList(this.measurementText, this.measurementNumeric, this.measurementCategorical);
		final ObservationDto testObservationDto = new ObservationDto(123, "1", "Test Entry", 300, "CML123", "5",
				"Entry Code", "2", "10", "3", measurements);

		if (doAddNewGermplasmDescriptors) {
			testObservationDto.additionalGermplasmDescriptor(DataMapUtilTest.STOCK_ID,
					DataMapUtilTest.STOCK_ID_VALUE);
			testObservationDto.additionalGermplasmDescriptor(DataMapUtilTest.CROSS,
					DataMapUtilTest.CROSS_VALUE);
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


	private void verifyCorrectValuesForFactors(final Map<String, Object> onePlotMeasurementData,
			final ObservationDto observationDto, final boolean isGidDesigFactorsIncluded,
			final boolean isNewGermplasmDescriptorsAdded, final boolean useDifferentLocalNames) {
		// there are tests where GID and DESIGNATION variable headers are not
		// expected to be present
		if (isGidDesigFactorsIncluded) {
			final String designationMapKey = useDifferentLocalNames
					? DataMapUtilTest.DESIGNATION + DataMapUtilTest.LOCAL
					: DataMapUtilTest.DESIGNATION;
			MatcherAssert.assertThat(observationDto.getDesignation(),
					Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(designationMapKey))));
			final String gidMapKey = useDifferentLocalNames ? TermId.GID.name() + DataMapUtilTest.LOCAL
					: TermId.GID.name();
			MatcherAssert.assertThat(observationDto.getGid(),
					Is.is(CoreMatchers.equalTo(onePlotMeasurementData.get(gidMapKey))));
		}

		final String entryNoMapKey = useDifferentLocalNames
				? TermId.ENTRY_NO.name() + DataMapUtilTest.LOCAL : TermId.ENTRY_NO.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] {observationDto.getEntryNo(), false}, (Object[]) onePlotMeasurementData.get(entryNoMapKey)), Is.is(true));

		final String entryCodeMapKey = useDifferentLocalNames
				? TermId.ENTRY_CODE.name() + DataMapUtilTest.LOCAL : TermId.ENTRY_CODE.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] {observationDto.getEntryCode(), false}, (Object[]) onePlotMeasurementData.get(entryCodeMapKey)),
				Is.is(true));

		if (isNewGermplasmDescriptorsAdded) {
			MatcherAssert.assertThat(
					Arrays.equals(new Object[] { DataMapUtilTest.STOCK_ID_VALUE },
							(Object[]) onePlotMeasurementData.get(DataMapUtilTest.STOCK_ID)),
					Is.is(true));
			MatcherAssert.assertThat(Arrays.equals(new Object[] { DataMapUtilTest.CROSS_VALUE },
					(Object[]) onePlotMeasurementData.get(DataMapUtilTest.CROSS)), Is.is(true));
		}

		final String entryTypeMapKey = useDifferentLocalNames
				? TermId.ENTRY_TYPE.name() + DataMapUtilTest.LOCAL : TermId.ENTRY_TYPE.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] {observationDto.getEntryType(), observationDto.getEntryType(), false},
				(Object[]) onePlotMeasurementData.get(entryTypeMapKey)), Is.is(true));

		final String plotNoMapKey = useDifferentLocalNames
				? TermId.PLOT_NO.name() + DataMapUtilTest.LOCAL : TermId.PLOT_NO.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getPlotNumber(), false },
				(Object[]) onePlotMeasurementData.get(plotNoMapKey)), Is.is(true));

		final String blockNoMapKey = useDifferentLocalNames
				? TermId.BLOCK_NO.name() + DataMapUtilTest.LOCAL : TermId.BLOCK_NO.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] {observationDto.getBlockNumber(), false}, (Object[]) onePlotMeasurementData.get(blockNoMapKey)),
				Is.is(true));

		final String repNoMapKey = useDifferentLocalNames ? TermId.REP_NO.name() + DataMapUtilTest.LOCAL
				: TermId.REP_NO.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] {observationDto.getRepitionNumber(), false}, (Object[]) onePlotMeasurementData.get(repNoMapKey)),
				Is.is(true));

		final String trialInstanceMapKey = useDifferentLocalNames
				? DataMapUtilTest.TRIAL_INSTANCE + DataMapUtilTest.LOCAL
				: DataMapUtilTest.TRIAL_INSTANCE;
		MatcherAssert.assertThat(Arrays.equals(new Object[] {observationDto.getTrialInstance(), false},
				(Object[]) onePlotMeasurementData.get(trialInstanceMapKey)), Is.is(true));

		final String rowMapKey = useDifferentLocalNames ? TermId.ROW.name() + DataMapUtilTest.LOCAL
				: TermId.ROW.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] { observationDto.getRowNumber(), false },
				(Object[]) onePlotMeasurementData.get(rowMapKey)), Is.is(true));

		final String colMapKey = useDifferentLocalNames ? TermId.COL.name() + DataMapUtilTest.LOCAL
				: TermId.COL.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] {observationDto.getColumnNumber(), false}, (Object[]) onePlotMeasurementData.get(colMapKey)),
				Is.is(true));

		final String plotIdMapKey = useDifferentLocalNames
				? TermId.PLOT_ID.name() + DataMapUtilTest.LOCAL : TermId.PLOT_ID.name();
		MatcherAssert.assertThat(
				Arrays.equals(new Object[] {observationDto.getPlotId(), false}, (Object[]) onePlotMeasurementData.get(plotIdMapKey)),
				Is.is(true));

		final String fieldMapColumnMapKey = useDifferentLocalNames
				? TermId.FIELDMAP_COLUMN.name() + DataMapUtilTest.LOCAL : TermId.FIELDMAP_COLUMN.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] {observationDto.getFieldMapColumn(), false},
				(Object[]) onePlotMeasurementData.get(fieldMapColumnMapKey)), Is.is(true));

		final String fieldMapRangeMapKey = useDifferentLocalNames
				? TermId.FIELDMAP_RANGE.name() + DataMapUtilTest.LOCAL : TermId.FIELDMAP_COLUMN.name();
		MatcherAssert.assertThat(Arrays.equals(new Object[] {observationDto.getFieldMapRange(), false},
				(Object[]) onePlotMeasurementData.get(fieldMapRangeMapKey)), Is.is(true));
	}

	private UserSelection setupMeasurementVariablesInMockWorkbook(final boolean useDifferentLocalName) {
		final UserSelection userSelection = new UserSelection();
		final Workbook workbook = Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		userSelection.setWorkbook(workbook);

		this.measurementVariables = new ArrayList<>();
		final String trait1Name = this.measurementText.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer
				.createMeasurementVariable(this.measurementText.getMeasurementVariable().getId(),
						useDifferentLocalName ? trait1Name + DataMapUtilTest.LOCAL : trait1Name, null));
		final String trait2Name = this.measurementNumeric.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementNumeric.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait2Name + DataMapUtilTest.LOCAL : trait2Name, null));
		final String trait3Name = this.measurementCategorical.getMeasurementVariable().getName();
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				this.measurementCategorical.getMeasurementVariable().getId(),
				useDifferentLocalName ? trait3Name + DataMapUtilTest.LOCAL : trait3Name, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(
				DataMapUtilTest.ALEUCOL_1_5_TERM_ID,
				DataMapUtilTest.ALEUCOL_1_5_TRAIT_NAME, null));

		this.measurementVariables.add(MeasurementVariableTestDataInitializer
				.createMeasurementVariable(this.measurementCategorical.getMeasurementVariable().getId(),
						useDifferentLocalName ? trait3Name + DataMapUtilTest.LOCAL : trait3Name, null));

		for (final TermId term : this.standardFactors) {
			this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(term.getId(),
					useDifferentLocalName ? term.name() + DataMapUtilTest.LOCAL : term.name(), null));
		}
		this.measurementVariables
				.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(),
						useDifferentLocalName ? DataMapUtilTest.DESIGNATION + DataMapUtilTest.LOCAL : DataMapUtilTest.DESIGNATION, null));
		this.measurementVariables.add(MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),
						useDifferentLocalName ? DataMapUtilTest.TRIAL_INSTANCE + DataMapUtilTest.LOCAL : DataMapUtilTest.TRIAL_INSTANCE,
						null));

		Mockito.when(workbook.getMeasurementDatasetVariablesView()).thenReturn(this.measurementVariables);
		return userSelection;
	}


	@Test
	public void testConvertForCategoricalVariableBlankMeasurementDto() {
		final Variable measurementVariable = new Variable();
		final int aleucolPhenotypeId = 456;
		final String aleucolPhenotypeTraitValue = "";

		final MeasurementDto measurementDto = new MeasurementDto(
				new MeasurementVariableDto(DataMapUtilTest.ALEUCOL_1_5_TERM_ID,
						DataMapUtilTest.ALEUCOL_1_5_TRAIT_NAME),
				aleucolPhenotypeId, aleucolPhenotypeTraitValue, Phenotype.ValueStatus.OUT_OF_SYNC);

		final Object[] values = (new DataMapUtil()).convertForCategoricalVariable(measurementVariable,
				measurementDto.getVariableValue(), measurementDto.getPhenotypeId(), false, null);

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
				new MeasurementVariableDto(DataMapUtilTest.ALEUCOL_1_5_TERM_ID,
						DataMapUtilTest.ALEUCOL_1_5_TRAIT_NAME),
				aleucolPhenotypeId, aleucolPhenotypeTraitValue, Phenotype.ValueStatus.MANUALLY_EDITED);

		final Object[] values = (new DataMapUtil()).convertForCategoricalVariable(measurementVariable, measurementDto.getVariableValue(),
				measurementDto.getPhenotypeId(), false, null);

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
				new MeasurementVariableDto(DataMapUtilTest.ALEUCOL_1_5_TERM_ID,
						DataMapUtilTest.ALEUCOL_1_5_TRAIT_NAME),
				aleucolPhenotypeId, aleucolPhenotypeTraitValue, Phenotype.ValueStatus.OUT_OF_SYNC);

		final Object[] values = (new DataMapUtil()).convertForCategoricalVariable(measurementVariable, measurementDto.getVariableValue(),
				measurementDto.getPhenotypeId(), false, null);

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
		(new DataMapUtil()).addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables,
				new HashMap<String, String>(), ontologyVariableDataManager, this.contextUtil.getCurrentProgramUUID());

		MatcherAssert.assertThat(this.standardFactors.length, Is.is(CoreMatchers.equalTo(dataMap.size())));
		// set to false because GID and DESIGNATION are not expected to be in
		// map
		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors,
				useDifferentLocalNames);
		MatcherAssert.assertThat("GID should not be a key in data map.", dataMap.get(TermId.GID.name()),
				Is.is(CoreMatchers.nullValue()));
		MatcherAssert.assertThat("DESIGNATION should not be a key in data map.",
				dataMap.get(DataMapUtilTest.DESIGNATION), Is.is(CoreMatchers.nullValue()));
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
		(new DataMapUtil()).addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables,
				new HashMap<String, String>(), ontologyVariableDataManager, this.contextUtil.getCurrentProgramUUID());

		// Expecting that GID-local and DESIGNATION-local were added
		MatcherAssert.assertThat(this.standardFactors.length + 2, Is.is(CoreMatchers.equalTo(dataMap.size())));
		MatcherAssert.assertThat(TermId.GID.name() + DataMapUtilTest.LOCAL + " was expected as key in data map but wasn't.",
				dataMap.get(TermId.GID.name() + DataMapUtilTest.LOCAL), Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(
				DataMapUtilTest.DESIGNATION + DataMapUtilTest.LOCAL
						+ " was expected as key in data map but wasn't.",
				dataMap.get(DataMapUtilTest.DESIGNATION) + DataMapUtilTest.LOCAL,
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors,
				useDifferentLocalNames);
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
		(new DataMapUtil()).addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables,
				new HashMap<String, String>(), ontologyVariableDataManager, this.contextUtil.getCurrentProgramUUID());

		// expecting CROSS and STOCK_ID to have been added
		MatcherAssert.assertThat(this.standardFactors.length + 2, Is.is(CoreMatchers.equalTo(dataMap.size())));
		MatcherAssert.assertThat(dataMap, hasKey(DataMapUtilTest.CROSS));
		MatcherAssert.assertThat(dataMap, hasKey(DataMapUtilTest.STOCK_ID));

		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors,
				useDifferentLocalNames);
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
		(new DataMapUtil()).addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables,
				new HashMap<String, String>(), ontologyVariableDataManager, this.contextUtil.getCurrentProgramUUID());

		// expecting FIELDMAP_COLUMN and FIELDMAP_RANGE to have been added
		MatcherAssert.assertThat(this.standardFactors.length + 2, Is.is(CoreMatchers.equalTo(dataMap.size())));
		MatcherAssert.assertThat(dataMap, hasKey(DataMapUtilTest.FIELDMAP_COLUMN));
		MatcherAssert.assertThat(dataMap.get(DataMapUtilTest.FIELDMAP_COLUMN),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(dataMap, hasKey(DataMapUtilTest.FIELDMAP_RANGE));
		MatcherAssert.assertThat(dataMap.get(DataMapUtilTest.FIELDMAP_RANGE),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

		final boolean isGidDesigFactorsIncluded = false;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded,
				doAddNewGermplasmDescriptors, useDifferentLocalNames);
	}

	@Test
	public void testAddGermplasmAndPlotFactorsDataToDataMapWithAdditionalDesignFactors() {

		Mockito.when(this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
				DataMapUtilTest.ALEUCOL_1_5_TERM_ID, true, false))
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
		observations.get(0).additionalDesignFactor(DataMapUtilTest.ALEUCOL_1_5_TRAIT_NAME, "1");

		// Method to test
		(new DataMapUtil()).addGermplasmAndPlotFactorsDataToDataMap(observationDto, dataMap, this.measurementVariables,
				new HashMap<String, String>(), ontologyVariableDataManager, this.contextUtil.getCurrentProgramUUID());

		MatcherAssert.assertThat(dataMap, hasKey(DataMapUtilTest.ALEUCOL_1_5_TRAIT_NAME));
		MatcherAssert.assertThat(dataMap.get(DataMapUtilTest.ALEUCOL_1_5_TRAIT_NAME), Is.is(CoreMatchers.not(CoreMatchers.nullValue())));

	}


	@Test
	public void testGenerateDatatableDataMap() {

		final boolean useDifferentLocalNames = false;
		final UserSelection userSelection = this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(),
				"CategoryValue1Definition");
		final boolean doAddNewGermplasmDescriptors = false;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1,
				doAddNewGermplasmDescriptors);
		final ObservationDto observationDto = observations.get(0);

		// Method to test
		final Map<String, Object> dataMap = (new DataMapUtil()).generateDatatableDataMap(observationDto, new HashMap<String, String>(),
				userSelection, ontologyVariableDataManager, contextUtil.getCurrentProgramUUID());

		MatcherAssert.assertThat("Expected a non-null data map.", dataMap.size(),
				Is.is(CoreMatchers.not(CoreMatchers.equalTo(0))));
		MatcherAssert.assertThat(String.valueOf(observationDto.getMeasurementId()),
				Is.is(CoreMatchers.equalTo(dataMap.get(DataMapUtilTest.EXPERIMENT_ID))));
		MatcherAssert.assertThat(String.valueOf(observationDto.getMeasurementId()),
				Is.is(CoreMatchers.equalTo(dataMap.get(DataMapUtilTest.ACTION))));

		// Verify the factor and trait names and values were included properly
		// in data map
		final boolean isGidDesigFactorsIncluded = true;
		this.verifyCorrectValuesForFactors(dataMap, observationDto, isGidDesigFactorsIncluded, doAddNewGermplasmDescriptors,
				useDifferentLocalNames);
		this.verifyCorrectValuesForTraits(category1, dataMap);
	}

	@Test
	public void testGenerateDatatableDataMapWithDeletedTrait() {

		final boolean useDifferentLocalNames = false;
		final UserSelection userSelection = this.setupMeasurementVariablesInMockWorkbook(useDifferentLocalNames);
		// Remove from measurement variables the character trait, but it is
		// still in test observations
		this.measurementVariables.remove(0);

		final int recordsCount = 1;
		final TermSummary category1 = new TermSummary(111, this.measurementCategorical.getVariableValue(),
				"CategoryValue1Definition");
		final boolean doAddNewGermplasmDescriptors = false;
		final List<ObservationDto> observations = this.setupTestObservations(recordsCount, category1, doAddNewGermplasmDescriptors);
		final ObservationDto observationDto = observations.get(0);

		// Method to test
		final Map<String, Object> dataMap = (new DataMapUtil())
				.generateDatatableDataMap(observationDto, new HashMap<String, String>(), userSelection, ontologyVariableDataManager,
						this.contextUtil.getCurrentProgramUUID());

		// Verify that values exist for retained traits but deleted trait is not
		// included in data map
		MatcherAssert.assertThat(dataMap.get(this.measurementNumeric.getMeasurementVariable().getName()),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(dataMap.get(this.measurementCategorical.getMeasurementVariable().getName()),
				Is.is(CoreMatchers.not(CoreMatchers.nullValue())));
		MatcherAssert.assertThat(dataMap.get(this.measurementText.getMeasurementVariable().getName()), Is.is(CoreMatchers.nullValue()));
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

	private void verifyCorrectValuesForTraits(final TermSummary category1, final Map<String, Object> dataMap) {
		// Character Trait
		MatcherAssert
				.assertThat(Arrays.equals(new Object[] {this.measurementText.getVariableValue(), this.measurementText.getPhenotypeId(),
								this.measurementText.getValueStatus()},
						(Object[]) dataMap.get(this.measurementText.getMeasurementVariable().getName())), Is.is(true));

		// Numeric Trait
		MatcherAssert.assertThat(Arrays.equals(
				new Object[] {this.measurementNumeric.getVariableValue(), true, this.measurementNumeric.getPhenotypeId(),
						this.measurementNumeric.getValueStatus()},
				(Object[]) dataMap.get(this.measurementNumeric.getMeasurementVariable().getName())), Is.is(true));

		// Categorical Trait
		MatcherAssert.assertThat(Arrays.equals(
				new Object[] {category1.getName(), category1.getDefinition(), true, this.measurementCategorical.getPhenotypeId(),
						this.measurementCategorical.getValueStatus()},
				(Object[]) dataMap.get(this.measurementCategorical.getMeasurementVariable().getName())), Is.is(true));
	}


}
