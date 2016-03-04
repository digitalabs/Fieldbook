
package com.efficio.fieldbook.web.naming.impl;

import java.text.SimpleDateFormat;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.google.common.collect.Lists;

public class GermplasmOriginParameterBuilderImplTest {

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@InjectMocks
	private final GermplasmOriginParameterBuilderImpl builder = new GermplasmOriginParameterBuilderImpl();

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testBuildWhenAllRequiredInputIsAvailable() {

		// Setup workbook
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		final MeasurementVariable studyNameMV = new MeasurementVariable();
		studyNameMV.setTermId(TermId.STUDY_NAME.getId());
		studyNameMV.setValue("Study Name");
		studyNameMV.setLabel(Workbook.STUDY_LABEL);

		final MeasurementVariable locationMV = new MeasurementVariable();
		locationMV.setTermId(TermId.LOCATION_ABBR.getId());
		locationMV.setValue("MEX");

		final MeasurementVariable seasonMV = new MeasurementVariable();
		seasonMV.setTermId(TermId.SEASON_VAR.getId());
		seasonMV.setValue("10290");

		workbook.setConditions(Lists.newArrayList(studyNameMV, locationMV, seasonMV));

		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(testProject.getUniqueID());

		final Variable seasonVariable = new Variable();
		final Scale seasonScale = new Scale();
		final TermSummary seasonCategory = new TermSummary(10290, "Dry Season", "Dry Season");
		seasonScale.addCategory(seasonCategory);
		seasonVariable.setScale(seasonScale);
		Mockito.when(
				this.ontologyVariableDataManager.getVariable(Matchers.eq(testProject.getUniqueID()),
						Matchers.eq(TermId.SEASON_VAR.getId()), Matchers.eq(true), Matchers.eq(false))).thenReturn(seasonVariable);

		final String plotNumber = "1";
		final AdvancingSource advancingSource = new AdvancingSource();
		advancingSource.setPlotNumber(plotNumber);
		final String selectionNumber = "2"; 
		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, advancingSource, selectionNumber);
		Assert.assertNotNull(parameters);
		Assert.assertEquals(testProject.getCropType().getCropName(), parameters.getCrop());
		Assert.assertEquals(studyNameMV.getValue(), parameters.getStudyName());
		Assert.assertEquals(studyDetails.getStudyType(), parameters.getStudyType());
		Assert.assertEquals(locationMV.getValue(), parameters.getLocation());
		Assert.assertEquals(seasonCategory.getDefinition(), parameters.getSeason());
		Assert.assertEquals(plotNumber, parameters.getPlotNumber());
		Assert.assertEquals(selectionNumber, parameters.getSelectionNumber());
	}
	
	@Test
	public void testBuildWhenAllRequiredInputIsAvailableCrossing() {

		// Setup workbook
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		final MeasurementVariable studyNameMV = new MeasurementVariable();
		studyNameMV.setTermId(TermId.STUDY_NAME.getId());
		studyNameMV.setValue("Study Name");
		studyNameMV.setLabel(Workbook.STUDY_LABEL);

		final MeasurementVariable locationMV = new MeasurementVariable();
		locationMV.setTermId(TermId.LOCATION_ABBR.getId());
		locationMV.setValue("MEX");

		final MeasurementVariable seasonMV = new MeasurementVariable();
		seasonMV.setTermId(TermId.SEASON_VAR.getId());
		seasonMV.setValue("10290");

		workbook.setConditions(Lists.newArrayList(studyNameMV, locationMV, seasonMV));

		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(testProject.getUniqueID());

		final Variable seasonVariable = new Variable();
		final Scale seasonScale = new Scale();
		final TermSummary seasonCategory = new TermSummary(10290, "Dry Season", "Dry Season");
		seasonScale.addCategory(seasonCategory);
		seasonVariable.setScale(seasonScale);
		Mockito.when(
				this.ontologyVariableDataManager.getVariable(Matchers.eq(testProject.getUniqueID()),
						Matchers.eq(TermId.SEASON_VAR.getId()), Matchers.eq(true), Matchers.eq(false))).thenReturn(seasonVariable);

		ImportedCrosses crosses = new ImportedCrosses();
		crosses.setMaleStudyName("Male Study Name");
		crosses.setMalePlotNo("1");
		crosses.setFemaleStudyName("Female Study Name");
		crosses.setFemaleStudyName("2");
		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, crosses);
		
		Assert.assertNotNull(parameters);
		Assert.assertEquals(testProject.getCropType().getCropName(), parameters.getCrop());
		Assert.assertEquals(studyNameMV.getValue(), parameters.getStudyName());
		Assert.assertEquals(studyDetails.getStudyType(), parameters.getStudyType());
		Assert.assertEquals(locationMV.getValue(), parameters.getLocation());
		Assert.assertEquals(seasonCategory.getDefinition(), parameters.getSeason());
		Assert.assertEquals(crosses.getMaleStudyName(), parameters.getMaleStudyName());
		Assert.assertEquals(crosses.getMalePlotNo(), parameters.getMalePlotNumber());
		Assert.assertEquals(crosses.getFemaleStudyName(), parameters.getFemaleStudyName());
		Assert.assertEquals(crosses.getFemalePlotNo(), parameters.getFemalePlotNumber());
		Assert.assertTrue(parameters.isCross());
	}

	@Test
	public void testBuildWhenLocationVariableIsNotPresent() {

		// Setup workbook
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		final MeasurementVariable studyNameMV = new MeasurementVariable();
		studyNameMV.setTermId(TermId.STUDY_NAME.getId());
		studyNameMV.setValue("Study Name");
		studyNameMV.setLabel(Workbook.STUDY_LABEL);

		// No location variable
		workbook.setConditions(Lists.newArrayList(studyNameMV));

		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);

		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, new AdvancingSource(), "2");
		Assert.assertNull("Expected null location value being set when LOCATION_ABBR variable is missing.", parameters.getLocation());
	}

	@Test
	public void testBuildWhenLocationVariableIsPresentButWithNoValue() {

		// Setup workbook
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		final MeasurementVariable studyNameMV = new MeasurementVariable();
		studyNameMV.setTermId(TermId.STUDY_NAME.getId());
		studyNameMV.setValue("Study Name");
		studyNameMV.setLabel(Workbook.STUDY_LABEL);

		final MeasurementVariable locationMV = new MeasurementVariable();
		locationMV.setTermId(TermId.LOCATION_ABBR.getId());
		// Location variable present but no value
		locationMV.setValue(null);

		workbook.setConditions(Lists.newArrayList(studyNameMV, locationMV));

		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(testProject.getUniqueID());

		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, new AdvancingSource(), "2");
		Assert.assertNull("Expected null location value being set when LOCATION_ABBR variable is present but there is no value set.",
				parameters.getLocation());
	}

	@Test
	public void testBuildWhenSeasonVariableIsNotPresent() {
		// Setup workbook
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		final MeasurementVariable studyNameMV = new MeasurementVariable();
		studyNameMV.setTermId(TermId.STUDY_NAME.getId());
		studyNameMV.setValue("Study Name");
		studyNameMV.setLabel(Workbook.STUDY_LABEL);

		final MeasurementVariable locationMV = new MeasurementVariable();
		locationMV.setTermId(TermId.LOCATION_ABBR.getId());
		locationMV.setValue("MEX");

		// No season variable.
		workbook.setConditions(Lists.newArrayList(studyNameMV, locationMV));

		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(testProject.getUniqueID());

		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, new AdvancingSource(), "2");

		SimpleDateFormat formatter = new SimpleDateFormat("YYYYMM");
		String currentYearAndMonth = formatter.format(new java.util.Date());

		Assert.assertEquals("Expected current year and month being set as Season when Crop_season_Code variable is missing.",
				currentYearAndMonth, parameters.getSeason());
	}

	@Test
	public void testBuildWhenSeasonVariableIsPresentButWithNoValue() {

		// Setup workbook
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		final MeasurementVariable studyNameMV = new MeasurementVariable();
		studyNameMV.setTermId(TermId.STUDY_NAME.getId());
		studyNameMV.setValue("Study Name");
		studyNameMV.setLabel(Workbook.STUDY_LABEL);

		final MeasurementVariable locationMV = new MeasurementVariable();
		locationMV.setTermId(TermId.LOCATION_ABBR.getId());
		locationMV.setValue("MEX");

		final MeasurementVariable seasonMV = new MeasurementVariable();
		seasonMV.setTermId(TermId.SEASON_VAR.getId());
		// season variable present but no value.
		seasonMV.setValue(null);

		workbook.setConditions(Lists.newArrayList(studyNameMV, locationMV, seasonMV));

		// Mocks
		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(testProject.getUniqueID());

		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, new AdvancingSource(), "2");
		SimpleDateFormat formatter = new SimpleDateFormat("YYYYMM");
		String currentYearAndMonth = formatter.format(new java.util.Date());

		Assert.assertEquals(
				"Expected current year and month being set as Season when Crop_season_Code variable is present but value is missing.",
				currentYearAndMonth, parameters.getSeason());

	}

	@Test
	public void testDeriveLocationForNurseries() {
		// Setup Nursery workbook
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		MeasurementVariable locationMV = new MeasurementVariable();
		locationMV.setTermId(TermId.LOCATION_ABBR.getId());
		locationMV.setValue("MEX");

		workbook.setConditions(Lists.newArrayList(locationMV));

		GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		this.builder.deriveLocation(workbook, parameters, "1");
		Assert.assertEquals(locationMV.getValue(), parameters.getLocation());
	}

	@Test
	public void testDeriveLocationForTrials() {
		// Setup Trial workbook
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.T);
		workbook.setStudyDetails(studyDetails);

		MeasurementVariable locationAbbrMV = new MeasurementVariable();
		locationAbbrMV.setTermId(TermId.LOCATION_ABBR.getId());
		MeasurementData locationAbbrMD = new MeasurementData();
		locationAbbrMD.setValue("MEX");
		locationAbbrMD.setMeasurementVariable(locationAbbrMV);

		MeasurementVariable instanceNumberMV = new MeasurementVariable();
		instanceNumberMV.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		MeasurementData instanceNumberMD = new MeasurementData();
		instanceNumberMD.setValue("1");
		instanceNumberMD.setMeasurementVariable(instanceNumberMV);

		MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(instanceNumberMD, locationAbbrMD));

		workbook.setTrialObservations(Lists.newArrayList(trialInstanceObservation));

		GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		this.builder.deriveLocation(workbook, parameters, instanceNumberMD.getValue());
		Assert.assertEquals(locationAbbrMD.getValue(), parameters.getLocation());
	}

	@Test
	public void testDeriveSeasonForNurseries() {
		// Setup Nursery workbook
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);
		workbook.setStudyDetails(studyDetails);

		MeasurementVariable seasonMV = new MeasurementVariable();
		seasonMV.setTermId(TermId.SEASON_VAR.getId());
		seasonMV.setValue("Dry Season");

		workbook.setConditions(Lists.newArrayList(seasonMV));

		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(testProject.getUniqueID());

		final Variable seasonVariable = new Variable();
		final Scale seasonScale = new Scale();
		final TermSummary seasonCategory = new TermSummary(10290, "Dry Season", "Dry Season");
		seasonScale.addCategory(seasonCategory);
		seasonVariable.setScale(seasonScale);
		Mockito.when(
				this.ontologyVariableDataManager.getVariable(Matchers.eq(testProject.getUniqueID()),
						Matchers.eq(TermId.SEASON_VAR.getId()), Matchers.eq(true), Matchers.eq(false))).thenReturn(seasonVariable);

		GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		this.builder.deriveSeason(workbook, parameters, "1");
		Assert.assertEquals(seasonCategory.getDefinition(), parameters.getSeason());
	}

	@Test
	public void testDeriveSeasonForTrials() {
		// Setup Trial workbook
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.T);
		workbook.setStudyDetails(studyDetails);

		MeasurementVariable seasonMV = new MeasurementVariable();
		seasonMV.setTermId(TermId.SEASON_VAR.getId());
		MeasurementData seasonMD = new MeasurementData();
		seasonMD.setValue("MEX");
		seasonMD.setMeasurementVariable(seasonMV);

		MeasurementVariable instanceNumberMV = new MeasurementVariable();
		instanceNumberMV.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		MeasurementData instanceNumberMD = new MeasurementData();
		instanceNumberMD.setValue("1");
		instanceNumberMD.setMeasurementVariable(instanceNumberMV);

		MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(instanceNumberMD, seasonMD));

		workbook.setTrialObservations(Lists.newArrayList(trialInstanceObservation));

		GermplasmOriginGenerationParameters parameters = new GermplasmOriginGenerationParameters();
		this.builder.deriveSeason(workbook, parameters, instanceNumberMD.getValue());
		Assert.assertEquals(seasonMD.getValue(), parameters.getSeason());
	}
}
