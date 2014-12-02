/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.utils.test.ExcelImportUtil;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public class ExcelExportStudyServiceTest extends AbstractBaseIntegrationTest {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelExportStudyServiceTest.class);
	
	private static final int STUDY_DETAILS_ROWS = 6;
	private static final int NO_OF_SECTION_SPACES = 4;
	private static final int NO_OF_SECTION_HEADERS = 4;

	@Resource
	private ExcelExportStudyService excelExportStudyService;

	@Resource
	private FieldbookProperties fieldbookProperties;

	private static com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
	
	private static org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Test
	public void testExportingMoreThan4000Observations() {
		List<Integer> visibleColumns = getVisibleColumnListWithSomeRequiredColumns();
		
		ExcelExportStudyServiceTest.fieldbookService = Mockito.mock(com.efficio.fieldbook.service.api.FieldbookService.class);

		((ExcelExportStudyServiceImpl) this.excelExportStudyService)
				.setFieldbookService(ExcelExportStudyServiceTest.fieldbookService);
		try {
			Mockito.when(ExcelExportStudyServiceTest.fieldbookService.getAllPossibleValues(1))
					.thenReturn(new ArrayList<ValueReference>());
		} catch (MiddlewareQueryException e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("Error in mocking the FieldbookService class");
		}

		// set filename path, create test data for workbook
		FileOutputStream fos = null;
		HSSFWorkbook xlsBook = new HSSFWorkbook();
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(
				WorkbookDataUtil.NUMBER_OF_OBSERVATIONS, StudyType.N);
		String filenamePath = this.fieldbookProperties.getUploadDirectory() + File.separator
				+ WorkbookDataUtil.FILE_NAME + AppConstants.EXPORT_XLS_SUFFIX.getString();
		
		// write description and observation sheets
		((ExcelExportStudyServiceImpl) this.excelExportStudyService).writeDescriptionSheet(xlsBook,
				workbook, workbook.getTrialObservations().get(0), visibleColumns);
		((ExcelExportStudyServiceImpl) this.excelExportStudyService).writeObservationSheet(xlsBook,
				workbook, workbook.getObservations(), visibleColumns);

		try {
			fos = new FileOutputStream(new File(filenamePath));
			xlsBook.write(fos);
		} catch (FileNotFoundException e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("File not found");
		} catch (IOException e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("Error writing to xls file");
		}

		try {
			// check if the file created is not corrupted
			org.apache.poi.ss.usermodel.Workbook xlsBookOutput = ExcelImportUtil
					.parseFile(filenamePath);
			Assert.assertNotNull("Expecting a non corrupted xls file but found none", xlsBookOutput);

			// check if observation sheet was created
			Sheet sheet = xlsBookOutput.getSheetAt(1);
			Assert.assertNotNull("Expecting 2 sheets in the xls file but did not found 2nd sheet",
					sheet);

			Assert.assertEquals("Expecting " + WorkbookDataUtil.NUMBER_OF_OBSERVATIONS
					+ " but got " + sheet.getLastRowNum() + " instead.",
					(WorkbookDataUtil.NUMBER_OF_OBSERVATIONS), sheet.getLastRowNum());
			
			
			// checks the no of visible colums in Observation Sheet
			int expectedNoOfVisibleColumns = visibleColumns.size() + 1; 
			Assert.assertEquals("Expected the number of columns in Observation Sheet is " + expectedNoOfVisibleColumns + " but returned " 
					+ sheet.getRow(0).getLastCellNum(), expectedNoOfVisibleColumns, sheet.getRow(0).getLastCellNum());
			
			// checks the no of rows in Description Sheet
			sheet = xlsBookOutput.getSheetAt(0);
			int expectedNoOfRowsInDescriptionSheet = getTotalNoOfRowsInDescriptionSheet(workbook, visibleColumns);
			int actualNoOfRowsInDescriptionSheet = sheet.getLastRowNum() + 1;
			Assert.assertEquals("Expected the number of rows in Description Sheet is " + expectedNoOfRowsInDescriptionSheet + " but returned " 
					+ actualNoOfRowsInDescriptionSheet, expectedNoOfRowsInDescriptionSheet, actualNoOfRowsInDescriptionSheet);
		
		} catch (Exception e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("Error encountered in parsing xls file created");
		}
	}
	
	private int getTotalNoOfRowsInDescriptionSheet(Workbook workbook, List<Integer> visibleColumns){
		int totalRows = STUDY_DETAILS_ROWS 
				+ NO_OF_SECTION_HEADERS 
				+ NO_OF_SECTION_SPACES 
				+ getNoOfConditions(workbook) 
				+ getNoOfFactors(workbook, visibleColumns) 
				+ workbook.getConstants().size() 
				+ getNoOfVariates(workbook, visibleColumns);
		return totalRows;
	}
	
	private int getNoOfVariates(Workbook workbook, List<Integer> visibleColumns){
		int noOfFactors = 0;
		List<MeasurementVariable> variables = workbook.getVariates();
		for (MeasurementVariable variable : variables) {
			if (ExportImportStudyUtil.isColumnVisible(variable.getTermId(),visibleColumns)) {
				noOfFactors++;
			}
		}
		
		return noOfFactors;
	}
	
	private int getNoOfFactors(Workbook workbook, List<Integer> visibleColumns){
		int noOfFactors = 0;
		List<MeasurementVariable> variables = workbook.getFactors();
		for (MeasurementVariable variable : variables) {
			if (variable.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId() 
					&& ExportImportStudyUtil.isColumnVisible(variable.getTermId(),visibleColumns)) {
				noOfFactors++;
			}
		}
		
		return noOfFactors;
	}
	
	private int getNoOfConditions(Workbook workbook){
		int noOfConditions = 0;
		List<MeasurementVariable> variables = workbook.getConditions();
		for (MeasurementVariable variable : variables) {
			if (!ExcelExportStudyServiceImpl.STUDY_DETAILS_IDS.contains(variable.getTermId())) {
				noOfConditions++;
			}
		}
		
		return noOfConditions;
	}
	
	@Test
	public void testGetFileNamePathWithSiteAndMoreThanOneInstance() {
		List<Location> locations = WorkbookDataUtil.createLocationData();
		
		String fileName = "trial_" + new Random().nextInt(1000) + ".xls";
		
		fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		try {
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(locations.get(0));
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_2)).thenReturn(locations.get(1));
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked class but encountered error in middleware");
		}
		
		ExcelExportStudyServiceImpl excelExportStudyServiceImpl = ((ExcelExportStudyServiceImpl) this.excelExportStudyService);
		excelExportStudyServiceImpl.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 2);
		List<Integer> instances = WorkbookDataUtil.getTrialInstances();
		
		try {
			int index = 1;
			for (MeasurementRow row : workbook.getTrialObservations()) {
				String outputFileName = excelExportStudyServiceImpl.getFileNamePath(index, row, 
						instances, fileName, false);
				Assert.assertTrue("Expected location in filename but did not found one.", 
						outputFileName.contains(WorkbookDataUtil.LNAME + "_" + index));
				index++;
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked locations but encountered error in middleware");
		}
	}
	
	@Test
	public void testGetFileNamePathWithSiteAndOneInstance() {
		List<Location> locations = WorkbookDataUtil.createLocationData();
			
		String fileName = "trial_" + new Random().nextInt(1000) + ".xls";
		
		fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		try {
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(locations.get(0));
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked class but encountered error in middleware");
		}
		
		ExcelExportStudyServiceImpl excelExportStudyServiceImpl = ((ExcelExportStudyServiceImpl) this.excelExportStudyService);
		excelExportStudyServiceImpl.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 1);
		List<Integer> instances = WorkbookDataUtil.getTrialInstances();
		
		try {
			String outputFileName = excelExportStudyServiceImpl.getFileNamePath(1, 
					workbook.getTrialObservations().get(0), instances, fileName, false);
			Assert.assertTrue("Expected location in filename but did not found one.", outputFileName.contains(WorkbookDataUtil.LNAME + "_1"));
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked locations but encountered error in middleware");
		}
	}
	
	@Test
	public void testGetFileNamePathWithoutSite() {
		List<Location> locations = WorkbookDataUtil.createLocationData();
		
		String fileName = "trial_" + new Random().nextInt(1000) + ".xls";
		
		fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
		try {
			Mockito.when(fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(locations.get(0));
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked class but encountered error in middleware");
		}
		
		ExcelExportStudyServiceImpl excelExportStudyServiceImpl = ((ExcelExportStudyServiceImpl) this.excelExportStudyService);
		excelExportStudyServiceImpl.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		MeasurementRow trialObservation = WorkbookDataUtil.createTrialObservationWithoutSite();
		List<Integer> instances = new ArrayList<Integer>();
		instances.add(1);
		
		try {
			String outputFileName = excelExportStudyServiceImpl.getFileNamePath(1, trialObservation, instances, fileName, false);
			Assert.assertTrue("Expected filename in output filename but found none.", 
					outputFileName.contains(fileName.substring(0, fileName.lastIndexOf("."))));
			String processedFileName = outputFileName.substring(0, fileName.lastIndexOf(".")); 
			Assert.assertFalse("Expected no underscore before the file extension but found one.", 
					processedFileName.charAt(processedFileName.length()-1) == '_' );
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			Assert.fail("Expected value from mocked locations but encountered error in middleware");
		}
	}
	
	private List<Integer> getVisibleColumnListWithSomeRequiredColumns() {
		List<Integer> visibleColumns = new ArrayList<Integer>();
		
		visibleColumns.add(TermId.ENTRY_NO.getId());
		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());
		
		return visibleColumns;
	}
	
}
