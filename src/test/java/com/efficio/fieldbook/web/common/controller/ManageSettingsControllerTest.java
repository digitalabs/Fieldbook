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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ManageSettingsControllerTest {

	public static final int TEST_VARIABLE_ID_0 = 1234;
	@Mock
	private OntologyService ontologyService;

	@Mock
	private UserSelection userSelection;

	@InjectMocks
	private ManageSettingsController controller;

	@Test
	public void testRemoveVariables() throws Exception {

	}

	@Test
	public void testHasMeasurementData() throws Exception {

	}

	@Test
	public void testCheckModeAndHasMeasurementData() throws Exception {

		ManageSettingsController spyController = spy(controller);
		doReturn(true).when(spyController).hasMeasurementDataEntered(anyInt());

		List<MeasurementRow> rows = Arrays.asList(mock(MeasurementRow.class),mock(MeasurementRow.class));
		when(userSelection.getMeasurementRowList()).thenReturn(rows);

		assertTrue("were sure this returns true", spyController
				.checkModeAndHasMeasurementData(AppConstants.SEGMENT_TRAITS.getInt(),
						TEST_VARIABLE_ID_0));

		verify(spyController).hasMeasurementDataEntered(TEST_VARIABLE_ID_0);


	}
}
