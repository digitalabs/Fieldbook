package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettingsServiceImplTest {

	@Mock
	private Workbook workbook;

	@Mock
	private FieldbookService fieldbookService;

	@InjectMocks
	private SettingsServiceImpl serviceDUT;

	@Test
	public void testRetrieveTraitsAsLabels() throws Exception {
		List<MeasurementVariable> traits = initializeListOfVariates();

		when(workbook.getVariates()).thenReturn(traits);

		List<LabelFields> result = serviceDUT.retrieveTraitsAsLabels(workbook);

		verify(workbook,times(1)).getVariates();

		assertEquals("equal results",initializeListOfVariates().size(),result.size());



	}

	private List<MeasurementVariable> initializeListOfVariates() {
		List<MeasurementVariable> traits = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			MeasurementVariable variate = new MeasurementVariable();
			variate.setName("variate_name" + i);
			variate.setTermId(i);

			traits.add(variate);
		}

		return traits;
	}
}