package com.efficio.etl.web.controller.angular;

import junit.framework.Assert;
import org.generationcp.middleware.domain.oms.StudyType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AngularSelectSheetControllerTest {

	@InjectMocks
	private AngularSelectSheetController angularSelectSheetController;

	@Test
	public void testGetStudyTypes() {

		final Map<String, String> expectedStudyTypesMap = new HashMap<>();

		for (final StudyType type : StudyType.values()) {
			expectedStudyTypesMap.put(type.getName(), type.getLabel());
		}

		final Map<String, String> result = angularSelectSheetController.getStudyTypes();

		Assert.assertEquals(expectedStudyTypesMap, result);

	}

}
