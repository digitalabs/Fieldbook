package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.AppConstants;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

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
		ManageSettingsController spyController = spy(controller);
		doReturn("").when(spyController).deleteVariable(anyInt(),anyInt());

		assertTrue("this should always return true regardless of input",spyController.deleteVariable(AppConstants.SEGMENT_TRAITS.getInt(),Arrays.asList(TEST_VARIABLE_ID_0)));

		verify(spyController).deleteVariable(AppConstants.SEGMENT_TRAITS.getInt(),TEST_VARIABLE_ID_0);
	}

	@Test
	public void testHasMeasurementData() throws Exception {
		ManageSettingsController spyController = initializeMockMeasurementRows();

		assertTrue("were sure this returns true", spyController
				.hasMeasurementData(Arrays.asList(TEST_VARIABLE_ID_0, TEST_VARIABLE_ID_1,
						TEST_VARIABLE_ID_2),AppConstants.SEGMENT_TRAITS.getInt()));

		verify(spyController).hasMeasurementDataEntered(TEST_VARIABLE_ID_0);
	}

	@Test
	public void testHasMeasurementFailScenario() throws Exception {
		ManageSettingsController spyController = initializeMockMeasurementRows();

		assertFalse("we're sure this returns false", spyController
				.hasMeasurementData(new ArrayList<Integer>(), AppConstants.SEGMENT_TRAITS.getInt()));

	}

	@Test
	public void testCheckModeAndHasMeasurementData() throws Exception {

		ManageSettingsController spyController = initializeMockMeasurementRows();

		assertTrue("we're sure this returns true", spyController
				.checkModeAndHasMeasurementData(AppConstants.SEGMENT_TRAITS.getInt(),
						TEST_VARIABLE_ID_0));

		verify(spyController).hasMeasurementDataEntered(TEST_VARIABLE_ID_0);
	}

	protected ManageSettingsController initializeMockMeasurementRows() {
		ManageSettingsController spyController = spy(controller);
		doReturn(true).when(spyController).hasMeasurementDataEntered(anyInt());

		List<MeasurementRow> rows = Arrays
				.asList(mock(MeasurementRow.class), mock(MeasurementRow.class));
		when(userSelection.getMeasurementRowList()).thenReturn(rows);
		return spyController;
	}

	@Test
	public void testCheckModeAndHasMeasurementDataFailScenario() throws Exception {

		ManageSettingsController spyController = spy(controller);
		doReturn(false).when(spyController).hasMeasurementDataEntered(anyInt());

		List<MeasurementRow> rows = Arrays.asList(mock(MeasurementRow.class),mock(MeasurementRow.class));
		when(userSelection.getMeasurementRowList()).thenReturn(rows);

		assertFalse("we're sure this returns false", spyController
				.checkModeAndHasMeasurementData(AppConstants.SEGMENT_TRAITS.getInt(),
						TEST_VARIABLE_ID_0));

		verify(spyController).hasMeasurementDataEntered(TEST_VARIABLE_ID_0);
	}
	
	@Test
	public void testGetObservationsOnEnvironment(){
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		
		Assert.assertEquals("Expecting that the return size of observation is " + NO_OF_OBSERVATIONS + 
				" but returned " + controller.getObservationsOnEnvironment(workbook, 1).size(),controller.getObservationsOnEnvironment(workbook, 1).size(), NO_OF_OBSERVATIONS);	
	}
	
	@Test
	public void testHasMeasurementDataOnEnvronmentReturnsTrueForExistingTraits(){
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		
		doReturn(workbook).when(userSelection).getWorkbook();
		
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(TermId.PLOT_NO.getId());
		
		Assert.assertTrue("Expected that the set of observations on the given environment has measurement data.",controller.hasMeasurementDataOnEnvironment(ids, 1));
	}
	
	@Test
	public void testHasMeasurementDataOnEnvronmentReturnsFalseForNonExistingTraits(){
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(NO_OF_OBSERVATIONS, NO_OF_TRIAL_INSTANCES);
		
		doReturn(workbook).when(userSelection).getWorkbook();
		
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(TermId.ENTRY_CODE.getId());
		
		Assert.assertFalse("Expected that the set of observations on the given environment has no measurement data.",controller.hasMeasurementDataOnEnvironment(ids, 1));
	}

}
