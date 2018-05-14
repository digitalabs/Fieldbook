package com.efficio.etl.web.controller.angular;

import junit.framework.Assert;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AngularSelectSheetControllerTest {

	@InjectMocks
	private AngularSelectSheetController angularSelectSheetController;

	@Mock
	StudyDataManager studyDataManager;

	@Before
	public void init() {
		List<StudyTypeDto> studyTypeDtoList = new ArrayList<>();
		studyTypeDtoList.add(new StudyTypeDto(10000, "Nursery","N"));
		studyTypeDtoList.add(new StudyTypeDto(10010, "Trial","T"));

		Mockito.when(this.studyDataManager.getAllVisibleStudyTypes()).thenReturn(studyTypeDtoList);
	}

	@Test
	public void testGetStudyTypes() {

		final Map<String, String> expectedStudyTypesMap = new HashMap<>();
			expectedStudyTypesMap.put("N", "Nursery");
			expectedStudyTypesMap.put("T", "Trial");

		final Map<String, String> result = angularSelectSheetController.getStudyTypes();

		Assert.assertEquals(expectedStudyTypesMap, result);

	}

}
