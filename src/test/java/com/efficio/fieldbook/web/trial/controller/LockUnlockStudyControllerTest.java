package com.efficio.fieldbook.web.trial.controller;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class LockUnlockStudyControllerTest {
	
	@Mock
	private StudyDataManager studyDataManager;
	
	@Mock
	private HttpServletRequest request;
	
	@InjectMocks
	private LockUnlockStudyController controller;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testLockStudy() {
		int studyId = 100;
		Mockito.doReturn("1").when(this.request).getParameter("doLock");
		this.controller.changeLockedStatus(studyId, this.request);
		Mockito.verify(this.studyDataManager).updateStudyLockedStatus(studyId, true);
	}
	
	@Test
	public void testUnlockStudy() {
		int studyId = 100;
		Mockito.doReturn("0").when(this.request).getParameter("doLock");
		this.controller.changeLockedStatus(studyId, this.request);
		Mockito.verify(this.studyDataManager).updateStudyLockedStatus(studyId, false);
	}

}
