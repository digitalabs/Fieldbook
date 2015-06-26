
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;

@RunWith(MockitoJUnitRunner.class)
public class ManageSettingsControllerTest {

	private static final int NO_OF_TRIAL_INSTANCES = 2;
	private static final int NO_OF_OBSERVATIONS = 10;
	public static final int TEST_VARIABLE_ID_0 = 1234;
	public static final int TEST_VARIABLE_ID_1 = 3456;
	public static final int TEST_VARIABLE_ID_2 = 4567;
	@Mock
	private OntologyService ontologyService;

	@Mock
	private UserSelection userSelection;

	@InjectMocks
	private ManageSettingsController controller;

	@Test
	public void testDeleteVariableMultiple() throws Exception {
		// we just have to make sure that the original deleteVariable(@PathVariable int mode, @PathVariable int variableId) is called
		// unit test for that method should be done separately
		ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn("").when(spyController).deleteVariable(Matchers.anyInt(), Matchers.anyInt());

		Assert.assertTrue(
				"this should always return true regardless of input",
				spyController.deleteVariable(VariableType.TRAIT.getId(),
						Arrays.asList(ManageSettingsControllerTest.TEST_VARIABLE_ID_0)));

		Mockito.verify(spyController).deleteVariable(VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
	}

	@Test
	public void testHasMeasurementData() throws Exception {
		ManageSettingsController spyController = this.initializeMockMeasurementRows();

		Assert.assertTrue("were sure this returns true", spyController.hasMeasurementData(Arrays.asList(
				ManageSettingsControllerTest.TEST_VARIABLE_ID_0, ManageSettingsControllerTest.TEST_VARIABLE_ID_1,
				ManageSettingsControllerTest.TEST_VARIABLE_ID_2), VariableType.TRAIT.getId()));

		Mockito.verify(spyController).hasMeasurementDataEntered(ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
	}

	@Test
	public void testHasMeasurementFailScenario() throws Exception {
		ManageSettingsController spyController = this.initializeMockMeasurementRows();

		Assert.assertFalse("we're sure this returns false",
				spyController.hasMeasurementData(new ArrayList<Integer>(), VariableType.TRAIT.getId()));

	}

	@Test
	public void testCheckModeAndHasMeasurementData() throws Exception {

		ManageSettingsController spyController = this.initializeMockMeasurementRows();

		Assert.assertTrue("we're sure this returns true", spyController.checkModeAndHasMeasurementData(
				VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0));

		Mockito.verify(spyController).hasMeasurementDataEntered(ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
	}

	protected ManageSettingsController initializeMockMeasurementRows() {
		ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn(true).when(spyController).hasMeasurementDataEntered(Matchers.anyInt());

		List<MeasurementRow> rows = Arrays.asList(Mockito.mock(MeasurementRow.class), Mockito.mock(MeasurementRow.class));
		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(rows);
		return spyController;
	}

	@Test
	public void testCheckModeAndHasMeasurementDataFailScenario() throws Exception {

		ManageSettingsController spyController = Mockito.spy(this.controller);
		Mockito.doReturn(false).when(spyController).hasMeasurementDataEntered(Matchers.anyInt());

		List<MeasurementRow> rows = Arrays.asList(Mockito.mock(MeasurementRow.class), Mockito.mock(MeasurementRow.class));
		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(rows);

		Assert.assertFalse("we're sure this returns false", spyController.checkModeAndHasMeasurementData(
				VariableType.TRAIT.getId(), ManageSettingsControllerTest.TEST_VARIABLE_ID_0));

		Mockito.verify(spyController).hasMeasurementDataEntered(ManageSettingsControllerTest.TEST_VARIABLE_ID_0);
	}

	@Test
	public void testGetObservationsOnEnvironment() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(ManageSettingsControllerTest.NO_OF_OBSERVATIONS,
						ManageSettingsControllerTest.NO_OF_TRIAL_INSTANCES);

		Assert.assertEquals("Expecting that the return size of observation is " + ManageSettingsControllerTest.NO_OF_OBSERVATIONS
				+ " but returned " + this.controller.getObservationsOnEnvironment(workbook, 1).size(), this.controller
				.getObservationsOnEnvironment(workbook, 1).size(), ManageSettingsControllerTest.NO_OF_OBSERVATIONS);
	}

	@Test
	public void testHasMeasurementDataOnEnvronmentReturnsTrueForExistingTraits() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(ManageSettingsControllerTest.NO_OF_OBSERVATIONS,
						ManageSettingsControllerTest.NO_OF_TRIAL_INSTANCES);

		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();

		List<Integer> ids = new ArrayList<Integer>();
		ids.add(TermId.PLOT_NO.getId());

		Assert.assertTrue("Expected that the set of observations on the given environment has measurement data.",
				this.controller.hasMeasurementDataOnEnvironment(ids, 1));
	}

	@Test
	public void testHasMeasurementDataOnEnvronmentReturnsFalseForNonExistingTraits() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook =
				WorkbookDataUtil.getTestWorkbookForTrial(ManageSettingsControllerTest.NO_OF_OBSERVATIONS,
						ManageSettingsControllerTest.NO_OF_TRIAL_INSTANCES);

		Mockito.doReturn(workbook).when(this.userSelection).getWorkbook();

		List<Integer> ids = new ArrayList<Integer>();
		ids.add(TermId.ENTRY_CODE.getId());

		Assert.assertFalse("Expected that the set of observations on the given environment has no measurement data.",
				this.controller.hasMeasurementDataOnEnvironment(ids, 1));
	}

}
