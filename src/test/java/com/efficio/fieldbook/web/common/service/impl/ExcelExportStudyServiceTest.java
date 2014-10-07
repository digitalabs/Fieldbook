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

import java.io.*;
import java.util.ArrayList;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.LabelPrintingServiceTest;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.ExcelImportUtil;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public class ExcelExportStudyServiceTest extends AbstractBaseIntegrationTest {

	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceTest.class);

	@Resource
	private ExcelExportStudyService excelExportStudyService;

	@Resource
	private FieldbookProperties fieldbookProperties;

	private static FieldbookService fieldbookService;

	@Test
	public void testExportingMoreThan4000Observations() {
		ExcelExportStudyServiceTest.fieldbookService = Mockito.mock(FieldbookService.class);

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
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(
				WorkbookDataUtil.NUMBER_OF_OBSERVATIONS, StudyType.N);
		String filenamePath = this.fieldbookProperties.getUploadDirectory() + File.separator
				+ WorkbookDataUtil.FILE_NAME + AppConstants.EXPORT_XLS_SUFFIX.getString();

		// write description and observation sheets
		((ExcelExportStudyServiceImpl) this.excelExportStudyService).writeDescriptionSheet(xlsBook,
				workbook, workbook.getTrialObservations().get(0));
		((ExcelExportStudyServiceImpl) this.excelExportStudyService).writeObservationSheet(xlsBook,
				workbook, workbook.getObservations());

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
					WorkbookDataUtil.NUMBER_OF_OBSERVATIONS, sheet.getLastRowNum());
		} catch (Exception e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("Error encountered in parsing xls file created");
		}
	}
}
