package com.efficio.fieldbook.web.trial.form;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.junit.Test;

public class ManageTrialFormTest {

	@Test
	public void testManageTrialFormPagination() {
		
		ManageTrialForm form = new ManageTrialForm();
		List<StudyDetails> nurseryDetailsList = new ArrayList<StudyDetails>(); 
		
		for(int i=0; i<25; i++) {
			StudyDetails studyDetail = new StudyDetails();
			studyDetail.setStudyName("Study " + i);
			studyDetail.setId(i);
			nurseryDetailsList.add(studyDetail);
		}
		
		form.setTrialDetailsList(nurseryDetailsList);
		
		form.setCurrentPage(1);
		assertEquals(form.getPaginatedTrialDetailsList().size(), form.getResultPerPage());

		form.setCurrentPage(2);
		assertEquals(form.getPaginatedTrialDetailsList().size(), form.getResultPerPage());
		
		form.setCurrentPage(3);
		assertEquals(form.getPaginatedTrialDetailsList().size(), 5);
	}
}
