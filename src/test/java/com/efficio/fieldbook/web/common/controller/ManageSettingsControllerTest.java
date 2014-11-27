package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.AppConstants;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ManageSettingsControllerTest {

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
}
