
package com.efficio.fieldbook.web.nursery.form;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.junit.Assert;
import org.junit.Test;

public class ManageNurseriesFormTest {

	@Test
	public void testManageNurseriesFormPagination() {

		ManageNurseriesForm form = new ManageNurseriesForm();
		List<StudyDetails> nurseryDetailsList = new ArrayList<StudyDetails>();

		for (int i = 0; i < 25; i++) {
			StudyDetails studyDetail = new StudyDetails();
			studyDetail.setStudyName("Study " + i);
			studyDetail.setId(i);
			nurseryDetailsList.add(studyDetail);
		}

		form.setNurseryDetailsList(nurseryDetailsList);

		form.setCurrentPage(1);
		Assert.assertEquals(form.getPaginatedNurseryDetailsList().size(), form.getResultPerPage());

		form.setCurrentPage(2);
		Assert.assertEquals(form.getPaginatedNurseryDetailsList().size(), form.getResultPerPage());

		form.setCurrentPage(3);
		Assert.assertEquals(form.getPaginatedNurseryDetailsList().size(), 5);
	}
}
