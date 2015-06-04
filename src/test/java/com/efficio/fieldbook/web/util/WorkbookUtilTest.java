
package com.efficio.fieldbook.web.util;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.Workbook;
import org.junit.Test;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;

public class WorkbookUtilTest {

	@Test
	public void testUpdateTrialObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook currentWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		WorkbookDataUtil.setTestWorkbook(null);
		Workbook temporaryWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 2);

		WorkbookUtil.updateTrialObservations(currentWorkbook, temporaryWorkbook);

		Assert.assertEquals("Expecting that the trial observations of temporary workbook is copied to current workbook. ",
				currentWorkbook.getTrialObservations(), temporaryWorkbook.getTrialObservations());
	}
}
