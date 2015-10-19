
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@RunWith(MockitoJUnitRunner.class)
public class ExcelExportStudyServiceImplTest {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelExportStudyServiceImplTest.class);

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private static org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private ExcelExportStudyServiceImpl excelExportStudyServiceImpl;

	@Before
	public void setUp() {

	}

	@Test
	public void testGetFileNamePathWithSiteAndMoreThanOneInstance() {
		final List<Location> locations = WorkbookDataUtil.createLocationData();

		final String fileName = "trial_" + new Random().nextInt(1000) + ".xls";

		fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		try {
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(locations.get(0));
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_2)).thenReturn(locations.get(1));
		} catch (final MiddlewareQueryException e) {
			ExcelExportStudyServiceImplTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked class but encountered error in middleware");
		}

		WorkbookDataUtil.setTestWorkbook(null);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 2);
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances();

		try {
			int index = 1;
			for (final MeasurementRow row : workbook.getTrialObservations()) {
				final String outputFileName = this.excelExportStudyServiceImpl.getFileNamePath(index, row, instances, fileName, false);
				Assert.assertTrue("Expected location in filename but did not found one.",
						outputFileName.contains(WorkbookDataUtil.LNAME + "_" + index));
				index++;
			}
		} catch (final MiddlewareQueryException e) {
			ExcelExportStudyServiceImplTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked locations but encountered error in middleware");
		}
	}

	@Test
	public void testGetFileNamePathWithSiteAndOneInstance() {
		final List<Location> locations = WorkbookDataUtil.createLocationData();

		final String fileName = "trial_" + new Random().nextInt(1000) + ".xls";

		fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		try {
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(locations.get(0));
		} catch (final MiddlewareQueryException e) {
			ExcelExportStudyServiceImplTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked class but encountered error in middleware");
		}

		WorkbookDataUtil.setTestWorkbook(null);
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 1);
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances();

		try {
			final String outputFileName =
					this.excelExportStudyServiceImpl.getFileNamePath(1, workbook.getTrialObservations().get(0), instances, fileName, false);
			Assert.assertTrue("Expected location in filename but did not found one.",
					outputFileName.contains(WorkbookDataUtil.LNAME + "_1"));
		} catch (final MiddlewareQueryException e) {
			ExcelExportStudyServiceImplTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked locations but encountered error in middleware");
		}
	}

	@Test
	public void testGetFileNamePathWithoutSite() {
		final List<Location> locations = WorkbookDataUtil.createLocationData();

		final String fileName = "trial_" + new Random().nextInt(1000) + ".xls";

		fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		try {
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(locations.get(0));
		} catch (final MiddlewareQueryException e) {
			ExcelExportStudyServiceImplTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked class but encountered error in middleware");
		}

		final MeasurementRow trialObservation = WorkbookDataUtil.createTrialObservationWithoutSite();
		final List<Integer> instances = new ArrayList<Integer>();
		instances.add(1);

		try {
			final String outputFileName = this.excelExportStudyServiceImpl.getFileNamePath(1, trialObservation, instances, fileName, false);
			Assert.assertTrue("Expected filename in output filename but found none.",
					outputFileName.contains(fileName.substring(0, fileName.lastIndexOf("."))));
			final String processedFileName = outputFileName.substring(0, fileName.lastIndexOf("."));
			Assert.assertFalse("Expected no underscore before the file extension but found one.",
					processedFileName.charAt(processedFileName.length() - 1) == '_');
		} catch (final MiddlewareQueryException e) {
			ExcelExportStudyServiceImplTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked locations but encountered error in middleware");
		}
	}
}
