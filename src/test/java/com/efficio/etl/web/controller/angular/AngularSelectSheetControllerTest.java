
package com.efficio.etl.web.controller.angular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.StudyPermissionValidator;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.util.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.ConsolidatedStepForm;
import com.efficio.etl.web.bean.StudyDetailsForm;

import junit.framework.Assert;

public class AngularSelectSheetControllerTest {

	private static final int NUM_STUDIES = 5;

	@InjectMocks
	private AngularSelectSheetController angularSelectSheetController;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private ETLService etlService;
	
	@Mock
	private ContextUtil contextUtil;
	
	@Mock
	private StudyPermissionValidator studyPermissionValidator;
	
	@Mock
	private Model model;
	
	@Mock
	private ConsolidatedStepForm form;
	
	@Mock
	private StudyDetailsForm studyDetails;
	
	private List<StudyDetails> allStudies;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		final List<StudyTypeDto> studyTypeDtoList = new ArrayList<>();
		studyTypeDtoList.add(new StudyTypeDto(10000, StudyTypeDto.NURSERY_LABEL, StudyTypeDto.NURSERY_NAME));
		studyTypeDtoList.add(new StudyTypeDto(10010, StudyTypeDto.TRIAL_LABEL, StudyTypeDto.TRIAL_NAME));
		Mockito.when(this.studyDataManager.getAllVisibleStudyTypes()).thenReturn(studyTypeDtoList);

		this.allStudies = new ArrayList<>();
		for (int i = 1; i <= NUM_STUDIES; i++) {
			this.allStudies.add(new StudyDetails(i, "STUDY" + i, "", "", "", "", StudyTypeDto.getTrialDto(), "", "", "", "1", true));
		}
		Mockito.doReturn(this.allStudies).when(this.etlService).retrieveExistingStudyDetails(ArgumentMatchers.<String>isNull());
		Mockito.doReturn(this.studyDetails).when(this.form).getStudyDetails();
	}

	@Test
	public void testGetStudyTypes() {

		final Map<String, String> expectedStudyTypesMap = new HashMap<>();
		expectedStudyTypesMap.put(StudyTypeDto.getNurseryDto().getName(), StudyTypeDto.getNurseryDto().getLabel());
		expectedStudyTypesMap.put(StudyTypeDto.getTrialDto().getName(), StudyTypeDto.getTrialDto().getLabel());

		final Map<String, String> result = angularSelectSheetController.getStudyTypes();

		Assert.assertEquals(expectedStudyTypesMap, result);

	}

	@Test
	public void testGetPreviousStudies() {
		Mockito.doReturn(false).when(this.studyPermissionValidator).userLacksPermissionForStudy(Matchers.any(StudyReference.class));
		final List<StudyDetails> studies = this.angularSelectSheetController.getPreviousStudies(this.model);
		Assert.assertEquals(this.allStudies, studies);
		Mockito.verify(this.model).addAttribute("restrictedStudies", new ArrayList<String>());
	}
	
	@Test
	public void testGetPreviousStudiesWithRestrictedStudies() {
		final StudyDetails restrictedStudy1 = this.allStudies.get(0);
		final StudyDetails restrictedStudy2 = this.allStudies.get(1);
		Mockito.doReturn(true).when(this.studyPermissionValidator).userLacksPermissionForStudy(new StudyReference(restrictedStudy1.getId(), restrictedStudy1.getStudyName()));
		Mockito.doReturn(true).when(this.studyPermissionValidator).userLacksPermissionForStudy(new StudyReference(restrictedStudy2.getId(), restrictedStudy2.getStudyName()));
		
		final List<StudyDetails> studies = this.angularSelectSheetController.getPreviousStudies(this.model);
		Assert.assertEquals(this.allStudies.size() - 2, studies.size());
		Assert.assertFalse(studies.contains(restrictedStudy1));
		Assert.assertFalse(studies.contains(restrictedStudy2));
		Mockito.verify(this.model).addAttribute("restrictedStudies", Arrays.asList(restrictedStudy1.getStudyName(), restrictedStudy2.getStudyName()));
	}
	
	@Test
	public void testValidateFormInputWhenStartDateIsEmpty() {
		Mockito.doReturn("").when(this.studyDetails).getStartDate();
		Mockito.doReturn("01/01/2017").when(this.studyDetails).getEndDate();
		List<Message> messages = this.angularSelectSheetController.validateFormInput(this.form);
		Assert.assertFalse(messages.isEmpty());
		Assert.assertEquals("error.date.startdate.required", messages.get(0).getMessageKey());
	}
	
	@Test
	public void testValidateFormInputWhenStartDateAfterCurrentDate() {
		Mockito.doReturn("01/01/2500").when(this.studyDetails).getStartDate();
		List<Message> messages = this.angularSelectSheetController.validateFormInput(this.form);
		Assert.assertFalse(messages.isEmpty());
		Assert.assertEquals("error.start.is.after.current.date", messages.get(0).getMessageKey());
	}
	
	@Test
	public void testValidateFormInputWhenEndDateBeforeStartDate() {
		Mockito.doReturn("01/01/2018").when(this.studyDetails).getStartDate();
		Mockito.doReturn("01/01/2017").when(this.studyDetails).getEndDate();
		List<Message> messages = this.angularSelectSheetController.validateFormInput(this.form);
		Assert.assertFalse(messages.isEmpty());
		Assert.assertEquals("error.date.enddate.invalid", messages.get(0).getMessageKey());
	}
	
	@Test
	public void testValidateFormInputSuccessful() {
		Mockito.doReturn("01/01/2017").when(this.studyDetails).getStartDate();
		Mockito.doReturn("01/01/2018").when(this.studyDetails).getEndDate();
		List<Message> messages = this.angularSelectSheetController.validateFormInput(this.form);
		Assert.assertTrue(messages.isEmpty());
	}

}
