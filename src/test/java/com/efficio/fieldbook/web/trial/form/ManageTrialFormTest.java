
package com.efficio.fieldbook.web.trial.form;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.junit.Assert;
import org.junit.Test;

public class ManageTrialFormTest {

	@Test
	public void testManageStudyFormPagination() {

		final ManageTrialForm form = new ManageTrialForm();
		final List<StudyDetails> nurseryDetailsList = new ArrayList<StudyDetails>();

		for (int i = 0; i < 25; i++) {
			final StudyDetails studyDetail = new StudyDetails();
			studyDetail.setStudyName("Study " + i);
			studyDetail.setId(i);
			nurseryDetailsList.add(studyDetail);
		}

		form.setTrialDetailsList(nurseryDetailsList);

		form.setCurrentPage(1);
		Assert.assertEquals(form.getPaginatedTrialDetailsList().size(), form.getResultPerPage());

		form.setCurrentPage(2);
		Assert.assertEquals(form.getPaginatedTrialDetailsList().size(), form.getResultPerPage());

		form.setCurrentPage(3);
		Assert.assertEquals(form.getPaginatedTrialDetailsList().size(), 5);
	}
}
