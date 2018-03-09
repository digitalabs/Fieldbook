
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.junit.Test;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;

public class WorkbookUtilTest {

	@Test
	public void testUpdateTrialObservations() {
		final Workbook currentWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 3);

		final Workbook temporaryWorkbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 2);

		WorkbookUtil.updateTrialObservations(currentWorkbook, temporaryWorkbook);

		Assert.assertEquals("Expecting that the trial observations of temporary workbook is copied to current workbook. ",
				currentWorkbook.getTrialObservations(), temporaryWorkbook.getTrialObservations());
	}

	@Test
	public void testResetObservationToDefaultDesign() {
		final Workbook nursery = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final List<MeasurementRow> observations = nursery.getObservations();

		DesignImportTestDataInitializer.updatePlotNoValue(observations);

		WorkbookUtil.resetObservationToDefaultDesign(observations);

		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			final MeasurementData entryNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.ENTRY_NO.getId(), dataList);
			final MeasurementData plotNoData = WorkbookUtil.retrieveMeasurementDataFromMeasurementRow(TermId.PLOT_NO.getId(), dataList);
			Assert.assertEquals("Expecting that the PLOT_NO value is equal to ENTRY_NO.", entryNoData.getValue(), plotNoData.getValue());
		}

	}

	@Test
	public void testFindMeasurementVariableByName() {

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		final String variable1 = "VARIABLE1";
		measurementVariable1.setName(variable1);
		measurementVariables.add(measurementVariable1);

		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		final String variable2 = "VARIABLE2";
		measurementVariable2.setName(variable2);
		measurementVariables.add(measurementVariable2);

		final MeasurementVariable measurementVariable3 = new MeasurementVariable();
		final String variable3 = "VARIABLE_3";
		measurementVariable3.setName(variable3);
		measurementVariables.add(measurementVariable3);

		final Optional<MeasurementVariable> result = WorkbookUtil.findMeasurementVariableByName(measurementVariables, variable1);

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(measurementVariable1, result.get());


	}

}
