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

package com.efficio.fieldbook.web.nursery.controller;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Debug;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ImportWorkbookFileService;

public class NurseryDetailsTest extends AbstractBaseIntegrationTest {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(NurseryDetailsTest.class);
	public static final int CURRENT_IBDB_USER_ID = 1;

	/** The file service. */
	@Autowired
	ImportWorkbookFileService fileService;

	/** The fieldbook service. */
	@Autowired
	FieldbookService fieldbookService;

	/** The data import service. */
	@Autowired
	DataImportService dataImportService;

	/** The Constant FILE_NAME_VALID. */
	private static final String FILE_NAME_VALID = "Population114_Pheno_FB_1.xls";

	/** The Constant FILE_NAME_INVALID. */
	private static final String FILE_NAME_INVALID = "GermplasmImportTemplate-Basic-rev4b-with_data.xls";

	/** The controller valid. */
	private NurseryDetailsController controllerValid;

	/** The controller invalid. */
	private NurseryDetailsController controllerInvalid;

	/**
	 * Sets the up.
	 */
	@Override
	@Before
	public void setUp() {
	}

	/**
	 * Test valid nursery workbook.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Ignore // FIXME fail on fresh db because there is no program. Create program for test
	public void testValidNurseryWorkbook() throws Exception {

		// Get the file
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(NurseryDetailsTest.FILE_NAME_VALID);
		String tempFileName = this.fieldbookService.storeUserWorkbook(inputStream);
		UserSelection userSelection = new UserSelection();
		userSelection.setActualFileName(NurseryDetailsTest.FILE_NAME_VALID);
		userSelection.setServerFileName(tempFileName);

		// Parse the file to create Workbook
		File file = this.fileService.retrieveCurrentWorkbookAsFile(userSelection);
		Workbook datasetWorkbook = this.dataImportService.parseWorkbook(file, CURRENT_IBDB_USER_ID);
		userSelection.setWorkbook(datasetWorkbook);

		this.controllerValid = new NurseryDetailsController();
		this.controllerValid.setUserSelection(userSelection);

		// Test if the workbook in the controller is valid
		Workbook workbook = this.controllerValid.getUserSelection().getWorkbook();

		Assert.assertTrue(workbook.getConditions() != null && workbook.getConditions().size() > 0);
		Assert.assertTrue(workbook.getFactors() != null && workbook.getFactors().size() > 0);
		Assert.assertTrue(workbook.getConstants() != null && workbook.getConstants().size() > 0);
		Assert.assertTrue(workbook.getVariates() != null && workbook.getVariates().size() > 0);

		// Output the nursery details
		NurseryDetailsTest.LOG.debug("========== CONDITIONS ==========");
		this.printMeasurementVariables(workbook.getConditions(), 4);
		NurseryDetailsTest.LOG.debug("========== FACTORS ==========");
		this.printMeasurementVariables(workbook.getFactors(), 4);
		NurseryDetailsTest.LOG.debug("========== CONSTANTS ==========");
		this.printMeasurementVariables(workbook.getConstants(), 4);
		NurseryDetailsTest.LOG.debug("========== VARIATES ==========");
		this.printMeasurementVariables(workbook.getVariates(), 4);
	}

	/**
	 * Prints the measurement variables.
	 *
	 * @param mVariables the m variables
	 * @param indent the indent
	 */
	private void printMeasurementVariables(List<MeasurementVariable> mVariables, int indent) {
		for (MeasurementVariable mVar : mVariables) {
			mVar.print(indent);
		}
		Debug.println(1, "");
	}

}
