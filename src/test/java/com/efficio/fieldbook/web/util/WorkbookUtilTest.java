
package com.efficio.fieldbook.web.util;

import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Test;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.data.initializer.DesignImportDataInitializer;

public class WorkbookUtilTest {

	@Test
	public void testUpdateTrialObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		final Workbook currentWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		WorkbookDataUtil.setTestWorkbook(null);
		final Workbook temporaryWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 2);

		WorkbookUtil.updateTrialObservations(currentWorkbook, temporaryWorkbook);

		Assert.assertEquals("Expecting that the trial observations of temporary workbook is copied to current workbook. ",
				currentWorkbook.getTrialObservations(), temporaryWorkbook.getTrialObservations());
	}

	@Test
	public void testResetObservationToDefaultDesign() {
		final Workbook nursery = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);
		final List<MeasurementRow> observations = nursery.getObservations();

		DesignImportDataInitializer.updatePlotNoValue(observations);

		WorkbookUtil.resetObservationToDefaultDesign(observations);

		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			final MeasurementData entryNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.ENTRY_NO.getId(), dataList);
			final MeasurementData plotNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.PLOT_NO.getId(), dataList);
			Assert.assertEquals("Expecting that the PLOT_NO value is equal to ENTRY_NO.", entryNoData.getValue(), plotNoData.getValue());
		}

	}

}
