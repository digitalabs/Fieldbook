
package com.efficio.fieldbook.web.common.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.google.common.base.Optional;
import org.fest.util.Collections;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.bean.UserSelection;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ManageSettingsControllerTest {

	private static final int STUDY_ID = 2020;
	public static final int TEST_VARIABLE_ID_0 = 1234;
	public static final int TEST_VARIABLE_ID_1 = 3456;
	public static final int TEST_VARIABLE_ID_2 = 4567;
	public static final String PROGRAM_UUID = "749782348-823asdasd-782364casd";

	@Mock
	private UserSelection userSelection;

	@Mock
	private StudyService studyService;

	@Mock
	private FormulaService formulaService;

	@Mock
	private SettingsService settingsService;

	@Mock
	protected FieldbookService fieldbookService;

	@Mock
	protected ContextUtil contextUtil;

	@InjectMocks
	private ManageSettingsController controller;

	@Before
	public void init() {


		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
	}

	@Test
	@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
	public void testDeleteVariableMultiple() throws Exception {
		// we just have to make sure that the original deleteVariable(@PathVariable int mode, @PathVariable int variableId) is called
		// unit test for that method should be done separately
		final ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn("").when(spyController).deleteVariable(Matchers.anyInt(), Matchers.anyInt());

		Assert.assertTrue("this should always return true regardless of input",
			spyController.deleteVariable(VariableType.TRAIT.getId(), Arrays.asList(ManageSettingsControllerTest.TEST_VARIABLE_ID_0)));

		Mockito.verify(spyController).deleteVariable(VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
	}

	@Test
	public void testHasMeasurementData() throws Exception {
		final ManageSettingsController spyController = this.initializeMockMeasurementRows();
		Mockito.doReturn(true).when(this.studyService).hasMeasurementDataEntered(Matchers.anyList(), Matchers.anyInt());
		assertThat(true, is(equalTo(spyController.hasMeasurementData(Arrays
			.asList(ManageSettingsControllerTest.TEST_VARIABLE_ID_0, ManageSettingsControllerTest.TEST_VARIABLE_ID_1,
				ManageSettingsControllerTest.TEST_VARIABLE_ID_2), VariableType.TRAIT.getId()))));
	}
	
	@Test
	public void testHasMeasurementDataWithNullWorkbook() throws Exception {
		final ManageSettingsController spyController = this.initializeMockMeasurementRows();
		// Returning null workbook means Study is unsaved yet
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(null);
		
		final boolean hasMeasurementData = spyController.hasMeasurementData(Arrays
			.asList(ManageSettingsControllerTest.TEST_VARIABLE_ID_0, ManageSettingsControllerTest.TEST_VARIABLE_ID_1,
				ManageSettingsControllerTest.TEST_VARIABLE_ID_2), VariableType.TRAIT.getId());
		
		// Unsaved Study will have no measurement data
		assertThat(false, is(equalTo(hasMeasurementData)));
		Mockito.verify(this.studyService, Mockito.never()).hasMeasurementDataEntered(Matchers.anyListOf(Integer.class), Matchers.anyInt());
	}
	
	@Test
	public void testHasMeasurementDataOnEnvironment() throws Exception {
		final int environmentNo = 1;
		final ManageSettingsController spyController = this.initializeMockMeasurementRows();
		Mockito.doReturn(true).when(this.studyService).hasMeasurementDataOnEnvironment(STUDY_ID, environmentNo);
		
		assertThat(true, is(equalTo(spyController.hasMeasurementDataOnEnvironment(new ArrayList<Integer>(), environmentNo))));
	}
	
	@Test
	public void testHasMeasurementDataOnEnvironmentWithNullWorkbook() throws Exception {
		final ManageSettingsController spyController = this.initializeMockMeasurementRows();
		// Returning null workbook means Study is unsaved yet
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(null);
		
		final boolean hasMeasurementData = spyController.hasMeasurementDataOnEnvironment(new ArrayList<Integer>(), 1);
		
		// Unsaved Study will have no measurement data
		assertThat(false, is(equalTo(hasMeasurementData)));
		Mockito.verify(this.studyService, Mockito.never()).hasMeasurementDataOnEnvironment(Matchers.anyInt(), Matchers.anyInt());
	}

	@Test
	public void testHasMeasurementFailScenario() throws Exception {
		final ManageSettingsController spyController = this.initializeMockMeasurementRows();
		Mockito.doReturn(false).when(spyController).hasMeasurementDataEntered(Matchers.anyInt());
		final List<MeasurementRow> rows = Arrays.asList(Mockito.mock(MeasurementRow.class), Mockito.mock(MeasurementRow.class));
		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(rows);
		Assert.assertFalse("we're sure this returns false",
			spyController.checkModeAndHasMeasurementData(VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0));

	}

	@Test
	public void testCheckModeAndHasMeasurementData() throws Exception {
		final ManageSettingsController spyController = this.initializeMockMeasurementRows();
		assertThat(true, is(equalTo(spyController.checkModeAndHasMeasurementData(VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0))));
		Mockito.verify(spyController).hasMeasurementDataEntered(ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
	}

	protected ManageSettingsController initializeMockMeasurementRows() {
		final ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn(true).when(spyController).hasMeasurementDataEntered(Matchers.anyInt());
		final List<MeasurementRow> rows = Arrays.asList(Mockito.mock(MeasurementRow.class), Mockito.mock(MeasurementRow.class));
		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(rows);

		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		final StudyDetails st = new StudyDetails();
		st.setId(STUDY_ID);
		Mockito.when(this.userSelection.getWorkbook().getStudyDetails()).thenReturn(st);
		return spyController;
	}

	@Test
	public void testCheckModeAndHasMeasurementDataFailScenario() throws Exception {

		final ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn(false).when(spyController).hasMeasurementDataEntered(Matchers.anyInt());

		final List<MeasurementRow> rows = Arrays.asList(Mockito.mock(MeasurementRow.class), Mockito.mock(MeasurementRow.class));
		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(rows);

		assertThat(false, is(equalTo(
			spyController.checkModeAndHasMeasurementData(VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0))));
	}

	@Test
	public void testCheckhasMeasurementDataEntered() throws Exception {

		final ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn(true).when(this.studyService).hasMeasurementDataEntered(Matchers.anyList(), Matchers.anyInt());

		final List<Integer> ids = new ArrayList<>();
		ids.add(ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
		assertThat(true, is(equalTo(spyController.checkModeAndHasMeasurementDataEntered(VariableType.TRAIT.getId(), ids, 2020))));
	}

	@Test
	public void testCheckhasMeasurementDataEnteredFailScenario() throws Exception {

		final ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn(false).when(this.studyService).hasMeasurementDataEntered(Matchers.anyList(), Matchers.anyInt());

		final List<Integer> ids = new ArrayList<>();
		ids.add(ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
		assertThat(false, is(equalTo(spyController.checkModeAndHasMeasurementDataEntered(VariableType.TRAIT.getId(), ids, 2020))));
	}

	@Test
	public void testAddSettings() throws Exception {

		final int cvTermId = 123;
		final String myVariable1 = "MyVariable1";

		final CreateTrialForm createTrialForm = new CreateTrialForm();
		final SettingVariable settingVariable = new SettingVariable();

		settingVariable.setCvTermId(cvTermId);
		createTrialForm.setSelectedVariables(Collections.list(settingVariable));

		final StandardVariable standardVariable = StandardVariableTestDataInitializer.createStandardVariable(cvTermId,
				myVariable1);

		final FormulaDto formulaDto = new FormulaDto();

		final ValueReference location1 = new ValueReference(1, "Location1");
		final ValueReference location2 = new ValueReference(2, "Location2");

		final List<ValueReference> possibleValues = Collections.list(location1, location2);
		final List<ValueReference> possibleValuesFavorite = Collections.list(location2);
		final List<ValueReference> allValues = Collections.list(location1, location2);
		final List<ValueReference> allFavoriteValues =  Collections.list(location2);

		Mockito.when(this.formulaService.getByTargetId(cvTermId)).thenReturn(Optional.of(formulaDto));
		Mockito.when(this.fieldbookService.getAllPossibleValues(cvTermId)).thenReturn(possibleValues);
		Mockito.when(this.fieldbookService.getAllPossibleValuesFavorite(cvTermId, PROGRAM_UUID, true)).thenReturn(possibleValuesFavorite);
		Mockito.when(this.fieldbookService.getAllPossibleValuesWithFilter(cvTermId, false)).thenReturn(allValues);
		Mockito.when(this.fieldbookService.getAllPossibleValuesFavorite(cvTermId, PROGRAM_UUID, null)).thenReturn(allFavoriteValues);

		// Add the variable in TRAIT settings.
		this.controller.addSettings(createTrialForm, VariableType.TRAIT.getId());

		Mockito.verify(settingsService).populateSettingVariable(settingVariable);

		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		Mockito.verify(settingsService).addNewSettingDetails(Mockito.eq(VariableType.TRAIT.getId()), captor.capture());

		final List<SettingDetail> addedSettingDetails = captor.getValue();
		final SettingDetail addedSettingDetail = addedSettingDetails.get(0);

		Assert.assertSame(formulaDto, addedSettingDetail.getVariable().getFormula());
		Assert.assertSame(Operation.ADD, addedSettingDetail.getVariable().getOperation());
		Assert.assertSame(allValues, addedSettingDetail.getAllValues());
		Assert.assertSame(possibleValuesFavorite, addedSettingDetail.getPossibleValuesFavorite());
		Assert.assertEquals(SettingsUtil.intersection(allValues, allFavoriteValues), addedSettingDetail.getAllFavoriteValues());
		Assert.assertNotNull(addedSettingDetail.getAllFavoriteValuesJson());
		Assert.assertNotNull(addedSettingDetail.getPossibleValuesJson());
		Assert.assertNotNull(addedSettingDetail.getPossibleValuesFavoriteJson());
		Assert.assertNotNull(addedSettingDetail.getAllValuesJson());

	}
}
