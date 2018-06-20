/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.junit.Assert;
import org.junit.Ignore;
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

@Ignore(value = "BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
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
	
	private final String breedingMethodPropertyName = "Breeding Method";

	@Test
	public void testExportingMoreThan4000Observations() {
		final List<Integer> visibleColumns = this.getVisibleColumnListWithSomeRequiredColumns();

		ExcelExportStudyServiceTest.fieldbookService = Mockito.mock(com.efficio.fieldbook.service.api.FieldbookService.class);

		((ExcelExportStudyServiceImpl) this.excelExportStudyService).setFieldbookService(ExcelExportStudyServiceTest.fieldbookService);
		try {
			Mockito.when(ExcelExportStudyServiceTest.fieldbookService.getAllPossibleValues(1)).thenReturn(new ArrayList<ValueReference>());
		} catch (final MiddlewareException e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("Error in mocking the FieldbookService class");
		}

		// set filename path, create test data for workbook
		final FileOutputStream fos;
		final HSSFWorkbook xlsBook = new HSSFWorkbook();
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(WorkbookDataUtil.NUMBER_OF_OBSERVATIONS, new StudyTypeDto("N"));
		final String filenamePath =
				this.fieldbookProperties.getUploadDirectory() + File.separator + WorkbookDataUtil.FILE_NAME
						+ AppConstants.EXPORT_XLS_SUFFIX.getString();

		// write description and observation sheets
		((ExcelExportStudyServiceImpl) this.excelExportStudyService).writeDescriptionSheet(xlsBook, workbook, workbook
				.getTrialObservations().get(0), visibleColumns);
		((ExcelExportStudyServiceImpl) this.excelExportStudyService).writeObservationSheet(xlsBook, workbook, workbook.getObservations(),
				visibleColumns, breedingMethodPropertyName);

		try {
			fos = new FileOutputStream(new File(filenamePath));
			xlsBook.write(fos);
		} catch (final FileNotFoundException e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("File not found");
		} catch (final IOException e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("Error writing to xls file");
		}

		try {
			// check if the file created is not corrupted
			final org.apache.poi.ss.usermodel.Workbook xlsBookOutput = ExcelImportUtil.parseFile(filenamePath);
			Assert.assertNotNull("Expecting a non corrupted xls file but found none", xlsBookOutput);

			// check if observation sheet was created
			Sheet sheet = xlsBookOutput.getSheetAt(1);
			Assert.assertNotNull("Expecting 2 sheets in the xls file but did not found 2nd sheet", sheet);

			Assert.assertEquals("Expecting " + WorkbookDataUtil.NUMBER_OF_OBSERVATIONS + " but got " + sheet.getLastRowNum() + " instead.",
					WorkbookDataUtil.NUMBER_OF_OBSERVATIONS, sheet.getLastRowNum());

			// checks the no of visible colums in Observation Sheet
			final int expectedNoOfVisibleColumns = visibleColumns.size() + 1;
			Assert.assertEquals("Expected the number of columns in Observation Sheet is " + expectedNoOfVisibleColumns + " but returned "
					+ sheet.getRow(0).getLastCellNum(), expectedNoOfVisibleColumns, sheet.getRow(0).getLastCellNum());

			// checks the no of rows in Description Sheet
			sheet = xlsBookOutput.getSheetAt(0);
			final int expectedNoOfRowsInDescriptionSheet = this.getTotalNoOfRowsInDescriptionSheet(workbook, visibleColumns);
			final int actualNoOfRowsInDescriptionSheet = sheet.getLastRowNum() + 1;
			Assert.assertEquals("Expected the number of rows in Description Sheet is " + expectedNoOfRowsInDescriptionSheet
					+ " but returned " + actualNoOfRowsInDescriptionSheet, expectedNoOfRowsInDescriptionSheet,
					actualNoOfRowsInDescriptionSheet);

		} catch (final Exception e) {
			ExcelExportStudyServiceTest.LOG.error(e.getMessage(), e);
			Assert.fail("Error encountered in parsing xls file created");
		}
	}

	private int getTotalNoOfRowsInDescriptionSheet(final Workbook workbook, final List<Integer> visibleColumns) {
		final int totalRows =
				ExcelExportStudyServiceTest.STUDY_DETAILS_ROWS + ExcelExportStudyServiceTest.NO_OF_SECTION_HEADERS
						+ ExcelExportStudyServiceTest.NO_OF_SECTION_SPACES + this.getNoOfConditions(workbook)
						+ this.getNoOfFactors(workbook, visibleColumns) + workbook.getConstants().size()
						+ this.getNoOfVariates(workbook, visibleColumns);
		return totalRows;
	}

	private int getNoOfVariates(final Workbook workbook, final List<Integer> visibleColumns) {
		int noOfFactors = 0;
		final List<MeasurementVariable> variables = workbook.getVariates();
		for (final MeasurementVariable variable : variables) {
			if (ExportImportStudyUtil.isColumnVisible(variable.getTermId(), visibleColumns)) {
				noOfFactors++;
			}
		}

		return noOfFactors;
	}

	private int getNoOfFactors(final Workbook workbook, final List<Integer> visibleColumns) {
		int noOfFactors = 0;
		final List<MeasurementVariable> variables = workbook.getFactors();
		for (final MeasurementVariable variable : variables) {
			if (variable.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()
					&& ExportImportStudyUtil.isColumnVisible(variable.getTermId(), visibleColumns)) {
				noOfFactors++;
			}
		}

		return noOfFactors;
	}

	private int getNoOfConditions(final Workbook workbook) {
		int noOfConditions = 0;
		final List<MeasurementVariable> variables = workbook.getConditions();
		for (final MeasurementVariable variable : variables) {
			if (!ExcelExportStudyServiceImpl.STUDY_DETAILS_IDS.contains(variable.getTermId())) {
				noOfConditions++;
			}
		}

		return noOfConditions;
	}

	private List<Integer> getVisibleColumnListWithSomeRequiredColumns() {
		final List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.ENTRY_NO.getId());
		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

}
