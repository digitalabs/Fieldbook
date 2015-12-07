
package com.efficio.fieldbook.web.naming.impl;

import java.text.SimpleDateFormat;

import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.spring.util.ContextUtil;
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

import com.efficio.fieldbook.util.FieldbookException;
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
	public void testBuildWhenAllRequiredInputIsAvailable() throws FieldbookException {

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
		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, plotNumber);
		Assert.assertNotNull(parameters);
		Assert.assertEquals(testProject.getCropType().getCropName(), parameters.getCrop());
		Assert.assertEquals(studyNameMV.getValue(), parameters.getStudyName());
		Assert.assertEquals(studyDetails.getStudyType(), parameters.getStudyType());
		Assert.assertEquals(locationMV.getValue(), parameters.getLocation());
		Assert.assertEquals(seasonCategory.getDefinition(), parameters.getSeason());
		Assert.assertEquals(plotNumber, parameters.getPlotNumber());
	}

	@Test
	public void testBuildWhenLocationVariableIsNotPresent() throws FieldbookException {

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

		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, "1");
		Assert.assertNull("Expected null location value being set when LOCATION_ABBR variable is missing.", parameters.getLocation());
	}

	@Test
	public void testBuildWhenLocationVariableIsPresentButWithNoValue() throws FieldbookException {

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

		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, "1");
		Assert.assertNull("Expected null location value being set when LOCATION_ABBR variable is present but there is no value set.",
				parameters.getLocation());
	}

	@Test
	public void testBuildWhenSeasonVariableIsNotPresent() throws FieldbookException {
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

		final String plotNumber = "1";
		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, plotNumber);

		SimpleDateFormat formatter = new SimpleDateFormat("YYYYMM");
		String currentYearAndMonth = formatter.format(new java.util.Date());

		Assert.assertEquals("Expected current year and month being set as Season when Crop_season_Code variable is missing.",
				currentYearAndMonth, parameters.getSeason());
	}

	@Test
	public void testBuildWhenSeasonVariableIsPresentButWithNoValue() throws FieldbookException {

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

		final GermplasmOriginGenerationParameters parameters = this.builder.build(workbook, "1");
		SimpleDateFormat formatter = new SimpleDateFormat("YYYYMM");
		String currentYearAndMonth = formatter.format(new java.util.Date());

		Assert.assertEquals(
				"Expected current year and month being set as Season when Crop_season_Code variable is present but value is missing.",
				currentYearAndMonth, parameters.getSeason());

	}
}
