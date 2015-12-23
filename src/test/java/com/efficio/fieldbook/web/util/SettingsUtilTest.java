
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.generationcp.middleware.util.Debug;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;

public class SettingsUtilTest {

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private UserSelection userSelection;

	private static final String PROGRAM_UUID = "123456789";

	private static final Term property = new Term(2002, "User", "Database user");
	private static final Term scale = new Term(6000, "DBCV", "Controlled vocabulary from a database");
	private static final Term method = new Term(4030, "Assigned", "Term, name or id assigned");
	private static final Term dataType = new Term(DataType.NUMERIC_VARIABLE.getId(), DataType.NUMERIC_VARIABLE.getName(),
			"Numeric Variable Description");

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Ignore(value = "BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
	@Test
	public void testConvertXmlDatasetToWorkbookAndBack() {
		final Dataset dataset = new Dataset();

		dataset.setConditions(new ArrayList<Condition>());
		dataset.setFactors(new ArrayList<Factor>());
		dataset.setVariates(new ArrayList<Variate>());

		dataset.getConditions().add(
				new Condition("CONDITION1", "CONDITION1", "PERSON", "DBCV", "ASSIGNED", PhenotypicType.STUDY.toString(), "C", "Meeh", null,
						null, null));
		dataset.getFactors().add(
				new Factor("FACTOR1", "FACTOR1", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", PhenotypicType.GERMPLASM.toString(), "N", 0));
		dataset.getVariates().add(
				new Variate("VARIATE1", "VARIATE1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice", PhenotypicType.VARIATE.toString(), "N",
						TermId.NUMERIC_VARIABLE.getId(), new ArrayList<ValueReference>(), 0.0, 0.0));

		final Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true, PROGRAM_UUID);
		Debug.println(0, workbook);

		final Dataset newDataset = (Dataset) SettingsUtil.convertWorkbookToXmlDataset(workbook);
		Assert.assertEquals(dataset.getConditions().get(0).getName(), newDataset.getConditions().get(0).getName());
		Assert.assertEquals(dataset.getConditions().get(0).getDescription(), newDataset.getConditions().get(0).getDescription());
		Assert.assertEquals(dataset.getConditions().get(0).getProperty(), newDataset.getConditions().get(0).getProperty());
		Assert.assertEquals(dataset.getConditions().get(0).getScale(), newDataset.getConditions().get(0).getScale());
		Assert.assertEquals(dataset.getConditions().get(0).getMethod(), newDataset.getConditions().get(0).getMethod());
		Assert.assertEquals(dataset.getConditions().get(0).getRole(), newDataset.getConditions().get(0).getRole());
		Assert.assertEquals(dataset.getConditions().get(0).getDatatype(), newDataset.getConditions().get(0).getDatatype());

		Assert.assertEquals(dataset.getFactors().get(0).getName(), newDataset.getFactors().get(0).getName());
		Assert.assertEquals(dataset.getFactors().get(0).getDescription(), newDataset.getFactors().get(0).getDescription());
		Assert.assertEquals(dataset.getFactors().get(0).getProperty(), newDataset.getFactors().get(0).getProperty());
		Assert.assertEquals(dataset.getFactors().get(0).getScale(), newDataset.getFactors().get(0).getScale());
		Assert.assertEquals(dataset.getFactors().get(0).getMethod(), newDataset.getFactors().get(0).getMethod());
		Assert.assertEquals(dataset.getFactors().get(0).getRole(), newDataset.getFactors().get(0).getRole());
		Assert.assertEquals(dataset.getFactors().get(0).getDatatype(), newDataset.getFactors().get(0).getDatatype());

		Assert.assertEquals(dataset.getVariates().get(0).getName(), newDataset.getVariates().get(0).getName());
		Assert.assertEquals(dataset.getVariates().get(0).getDescription(), newDataset.getVariates().get(0).getDescription());
		Assert.assertEquals(dataset.getVariates().get(0).getProperty(), newDataset.getVariates().get(0).getProperty());
		Assert.assertEquals(dataset.getVariates().get(0).getScale(), newDataset.getVariates().get(0).getScale());
		Assert.assertEquals(dataset.getVariates().get(0).getMethod(), newDataset.getVariates().get(0).getMethod());
		Assert.assertEquals(dataset.getVariates().get(0).getRole(), newDataset.getVariates().get(0).getRole());
		Assert.assertEquals(dataset.getVariates().get(0).getDatatype(), newDataset.getVariates().get(0).getDatatype());

	}

	@Test
	public void testIfCheckVariablesAreInFixedNurseryList() {
		Assert.assertTrue(SettingsUtil.inFixedNurseryList(TermId.CHECK_START.getId()));
		Assert.assertTrue(SettingsUtil.inFixedNurseryList(TermId.CHECK_INTERVAL.getId()));
		Assert.assertTrue(SettingsUtil.inFixedNurseryList(TermId.CHECK_PLAN.getId()));
	}

	@Test
	public void testGetCodeValueValid() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8414", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertEquals("Expected 1 but got " + code + " instead.", 1, code);
	}

	@Test
	public void testGetCodeValueWhenConditionsIsNull() {
		final List<SettingDetail> removedConditions = null;
		final int code = SettingsUtil.getCodeValue("8414", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetCodeValueWhenPossibleValuesIsNull() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8411", removedConditions, TermId.CHECK_START.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetCodeValueWhenPossibleValuesIsNotNullButEmpty() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8412", removedConditions, TermId.CHECK_INTERVAL.getId());
		Assert.assertEquals("Expected 0 but got " + code + " instead.", 0, code);
	}

	@Test
	public void testGetVariableAppConstantLabels() throws Exception {
		final List<String> labels = new ArrayList<>(Arrays.asList(new String[] {"abc", "def"}));

		final Properties appConfigProp = Mockito.mock(Properties.class);
		Mockito.when(appConfigProp.getProperty(Mockito.any(String.class))).thenReturn("any value");
		final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

		SettingsUtil.getVariableAppConstantLabels(labels, appConfigProp);

		Mockito.verify(appConfigProp, Mockito.times(labels.size())).getProperty(argument.capture());
		Assert.assertTrue(argument.getValue().contains("LABEL"));
	}

	@Test
	public void testGetCodeValueInvalid() {
		final List<SettingDetail> removedConditions = this.createCheckVariables(true);
		final int code = SettingsUtil.getCodeValue("8413", removedConditions, TermId.CHECK_PLAN.getId());
		Assert.assertNotSame("Expected 1 but got " + code + " instead.", 1, code);
	}

	@Test
	public void testIfCheckVariablesHaveValues() {
		final List<SettingDetail> checkVariables = this.createCheckVariables(true);
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertTrue(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesHaveNoValues() {
		final List<SettingDetail> checkVariables = this.createCheckVariables(false);
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesIsNull() {
		final List<SettingDetail> checkVariables = null;
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testIfCheckVariablesIsEmpty() {
		final List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();
		final boolean checksHaveValues = SettingsUtil.checkVariablesHaveValues(checkVariables);
		Assert.assertFalse(checksHaveValues);
	}

	@Test
	public void testParseVariableIds() {
		final List<Integer> variableIds = this.settingsUtilParseVariableIds("1|2|3");
		Assert.assertEquals("Should have 3 variable ids", 3, variableIds.size());
		Assert.assertEquals("1st Id should be 1", new Integer(1), variableIds.get(0));
		Assert.assertEquals("2nd Id should be 2", new Integer(2), variableIds.get(1));
		Assert.assertEquals("3rd Id should be 3", new Integer(3), variableIds.get(2));
	}

	@Test
	public void testConvertToExpDesignParamsUi() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();

		expDesigns.add(this.createMeasurementVariable(TermId.BLOCK_SIZE.getId(), "1"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_COLS_IN_REPS.getId(), "2"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_ROWS_IN_REPS.getId(), "3"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CBLKS_LATINIZE.getId(), "4"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CCOLS_LATINIZE.getId(), "5"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_CROWS_LATINIZE.getId(), "6"));
		expDesigns.add(this.createMeasurementVariable(TermId.NO_OF_REPS_IN_COLS.getId(), "7"));
		expDesigns.add(this.createMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "8"));
		expDesigns.add(this.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "9"));

		ExpDesignParameterUi result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);

		Assert.assertEquals("1", result.getBlockSize());
		Assert.assertEquals("2", result.getColsPerReplications());
		Assert.assertEquals("3", result.getRowsPerReplications());
		Assert.assertEquals("4", result.getNblatin());
		Assert.assertEquals("5", result.getNclatin());
		Assert.assertEquals("6", result.getNrlatin());
		Assert.assertEquals("7", result.getReplatinGroups());
		Assert.assertEquals("8", result.getReplicationsCount());
		Assert.assertEquals("9", result.getFileName());

	}

	@Test
	public void testConvertToExpDesignParamsUiRandomizedBlockDesign() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId())));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(0, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiResolvableIncompleteBlock() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId())));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(1, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiResolvableIncompleteRowColumn() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId())));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(2, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiOtherDesign() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns
				.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), String.valueOf(TermId.OTHER_DESIGN.getId())));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(3, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiAlphaLatticeEntry30Rep2() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId())));
		expDesigns.add(this.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "E30-Rep2-Block6-5Ind.csv"));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(4, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiAlphaLatticeEntry30Rep3() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId())));
		expDesigns.add(this.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "E30-Rep3-Block6-5Ind.csv"));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(5, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiAlphaLatticeEntry50Rep2() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId())));
		expDesigns.add(this.createMeasurementVariable(TermId.EXPT_DESIGN_SOURCE.getId(), "E50-Rep2-Block5-10Ind.csv"));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(6, result.getDesignType().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiRepsInSingleColumn() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.REPLICATIONS_MAP.getId(), String.valueOf(TermId.REPS_IN_SINGLE_COL.getId())));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(1, result.getReplicationsArrangement().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiRepsInSingleRow() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns.add(this.createMeasurementVariable(TermId.REPLICATIONS_MAP.getId(), String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId())));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(2, result.getReplicationsArrangement().intValue());

	}

	@Test
	public void testConvertToExpDesignParamsUiRepsInAdjacentCol() {

		List<MeasurementVariable> expDesigns = new ArrayList<>();
		expDesigns
				.add(this.createMeasurementVariable(TermId.REPLICATIONS_MAP.getId(), String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId())));

		ExpDesignParameterUi result;
		result = SettingsUtil.convertToExpDesignParamsUi(expDesigns);
		Assert.assertEquals(3, result.getReplicationsArrangement().intValue());

	}

	@Test
	public void testGetExperimentalDesignValue() {

		ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();

		expDesignParameterUi.setBlockSize("1");
		expDesignParameterUi.setColsPerReplications("2");
		expDesignParameterUi.setRowsPerReplications("3");
		expDesignParameterUi.setNblatin("4");
		expDesignParameterUi.setNclatin("5");
		expDesignParameterUi.setNrlatin("6");
		expDesignParameterUi.setReplatinGroups("7");
		expDesignParameterUi.setReplicationsCount("8");

		Assert.assertEquals("1", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.BLOCK_SIZE));
		Assert.assertEquals("2", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_COLS_IN_REPS));
		Assert.assertEquals("3", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_ROWS_IN_REPS));
		Assert.assertEquals("4", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_CBLKS_LATINIZE));
		Assert.assertEquals("5", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_CCOLS_LATINIZE));
		Assert.assertEquals("6", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_CROWS_LATINIZE));
		Assert.assertEquals("7", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NO_OF_REPS_IN_COLS));
		Assert.assertEquals("8", SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.NUMBER_OF_REPLICATES));

		expDesignParameterUi.setDesignType(0);
		Assert.assertEquals(String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(1);
		expDesignParameterUi.setUseLatenized(false);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(1);
		expDesignParameterUi.setUseLatenized(true);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(2);
		expDesignParameterUi.setUseLatenized(false);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(2);
		expDesignParameterUi.setUseLatenized(true);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(3);
		Assert.assertEquals(String.valueOf(TermId.OTHER_DESIGN.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setUseLatenized(false);
		expDesignParameterUi.setDesignType(4);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(5);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setDesignType(6);
		Assert.assertEquals(String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.EXPERIMENT_DESIGN_FACTOR));

		expDesignParameterUi.setReplicationsArrangement(1);
		Assert.assertEquals(String.valueOf(TermId.REPS_IN_SINGLE_COL.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.REPLICATIONS_MAP));

		expDesignParameterUi.setReplicationsArrangement(2);
		Assert.assertEquals(String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.REPLICATIONS_MAP));

		expDesignParameterUi.setReplicationsArrangement(3);
		Assert.assertEquals(String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId()),
				SettingsUtil.getExperimentalDesignValue(expDesignParameterUi, TermId.REPLICATIONS_MAP));
	}

	private List<Integer> settingsUtilParseVariableIds(final String variableIds) {
		return SettingsUtil.parseVariableIds(variableIds);
	}

	private List<SettingDetail> createCheckVariables(final boolean hasValue) {
		final List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();

		checkVariables.add(this.createSettingDetail(TermId.CHECK_START.getId(), hasValue ? "1" : null));
		checkVariables.add(this.createSettingDetail(TermId.CHECK_INTERVAL.getId(), hasValue ? "4" : null));
		checkVariables.add(this.createSettingDetail(TermId.CHECK_PLAN.getId(), hasValue ? "8414" : null));

		return checkVariables;
	}

	private SettingDetail createSettingDetail(final int cvTermId, final String value) {
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		final SettingDetail settingDetail = new SettingDetail(variable, null, value, false);

		if (cvTermId == TermId.CHECK_PLAN.getId()) {
			final List<ValueReference> possibleValues = new ArrayList<ValueReference>();
			possibleValues.add(new ValueReference(8414, "1", "Insert each check in turn"));
			possibleValues.add(new ValueReference(8415, "2", "Insert all checks at each position"));
			settingDetail.setPossibleValues(possibleValues);
		} else if (cvTermId == TermId.CHECK_INTERVAL.getId()) {
			settingDetail.setPossibleValues(new ArrayList<ValueReference>());
		}
		return settingDetail;
	}

	@Test
	public void testcleanSheetAndFileNameWithInvalid() {
		final String cleanedName = SettingsUtil.cleanSheetAndFileName("Test[:\\\\/*?|<>]");
		Assert.assertEquals("String should be cleaned", "Test[_________]", cleanedName);
	}

	@Test
	public void testGetCodeInPossibleValues() {
		final List<ValueReference> valueRefs = new ArrayList<ValueReference>();
		valueRefs.add(new ValueReference(8414, "1"));
		valueRefs.add(new ValueReference(8415, "2"));
		Assert.assertEquals("Should return 1 since the matching name for 8414 is 1", 1,
				SettingsUtil.getCodeInPossibleValues(valueRefs, "8414"));
		Assert.assertEquals("Should return 2 since the matching name for 8415 is 2", 2,
				SettingsUtil.getCodeInPossibleValues(valueRefs, "8415"));
	}

	@Test
	public void testSetSettingDetailRoleForDefaultVartypes() {
		// Create a standardVaraible as pre-req
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setVariableTypes(null);

		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.CATEGORICAL_VARIATE.getId(), PROGRAM_UUID)).thenReturn(
				standardVariable);

		for (final VariableType varType : VariableType.values()) {
			final SettingDetail settingDetail = new SettingDetail();
			final SettingVariable settingVariable = new SettingVariable();
			settingVariable.setCvTermId(TermId.CATEGORICAL_VARIATE.getId());
			settingDetail.setVariable(settingVariable);
			settingDetail.setRole(null);

			final List<SettingDetail> detailList = new ArrayList<>();
			// use any setting variable that is not a trial instance factor
			detailList.add(settingDetail);

			SettingsUtil.setSettingDetailRole(varType.getId(), detailList, this.userSelection, this.fieldbookMiddlewareService,
					PROGRAM_UUID);
			Assert.assertEquals("Should have the correct phenotypic type role as per the variable type", varType.getRole(),
					settingDetail.getRole());

		}
	}

	@Test
	public void testSetSettingDetailRoleWithTrialInstanceFactorAsRole() {
		// SettingDetail contains Trial instance factor
		final List<SettingDetail> newDetails = new ArrayList<SettingDetail>();
		final SettingDetail detail = new SettingDetail();
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		detail.setVariable(variable);
		newDetails.add(detail);

		// for mode, we use any that is not a germplasm descriptor
		final VariableType studyDetailMode = VariableType.STUDY_DETAIL;
		SettingsUtil.setSettingDetailRole(studyDetailMode.getId().intValue(), newDetails, this.userSelection,
				this.fieldbookMiddlewareService, PROGRAM_UUID);
		Assert.assertEquals(
				"Since we had a settingDetail that is a trial instance factor, the detail's role should be converted to Trial Environment",
				PhenotypicType.TRIAL_ENVIRONMENT, detail.getRole());

	}

	/**
	 * Test for check if given baseline traits empty or null then empty variate list should be returned.
	 */
	@Test
	public void testConvertBaselineTraitsToVariatesWithEmptyBaselineTraits() {
		final List<SettingDetail> baselineTraits = new ArrayList<>();

		final UserSelection userSelection = new UserSelection();

		final List<Variate> baselineVariates =
				SettingsUtil.convertBaselineTraitsToVariates(baselineTraits, userSelection, this.fieldbookMiddlewareService, PROGRAM_UUID);

		Assert.assertEquals(baselineTraits.size(), baselineVariates.size());
	}

	protected MeasurementVariable createMeasurementVariable(int termId, String value) {
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setValue(value);
		return measurementVariable;
	}
}
