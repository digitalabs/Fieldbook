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

package com.efficio.fieldbook.web.trial.controller;

import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.junit.Test;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.UserSelection;

public class ExpDesignControllerTest extends AbstractBaseControllerIntegrationTest {

	private static final int NO_OF_OBSERVATIONS = 10;
	private static final String ENVIRONMENTS = "3";

	@Resource
	ExpDesignController expDesignController;

	@Resource
	UserSelection userSelection;

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithNoExistingWorkbook() {
		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(null);
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTest.NO_OF_OBSERVATIONS);
		List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.",
				measurementRows.size(), combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithNullObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		workbook.setObservations(null);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTest.NO_OF_OBSERVATIONS);
		List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.",
				measurementRows.size(), combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithExisting() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTest.NO_OF_OBSERVATIONS);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + (measurementRows.size() + workbook.getObservations().size()) + " but got "
				+ combinedMeasurementRows.size() + " instead.", measurementRows.size() + workbook.getObservations().size(),
				combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithExistingTempWorkbook() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTest.NO_OF_OBSERVATIONS);

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(null);

		List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + (measurementRows.size() + workbook.getObservations().size()) + " but got "
				+ combinedMeasurementRows.size() + " instead.", measurementRows.size() + workbook.getObservations().size(),
				combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithoutMeasurementData() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);

		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTest.NO_OF_OBSERVATIONS);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, false);

		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.",
				measurementRows.size(), combinedMeasurementRows.size());
	}

	@Test
	public void testCountNewEnvironmentsWithNullWorkbook() {
		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(null);

		String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTest.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + ExpDesignControllerTest.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTest.ENVIRONMENTS, noOfNewEnvironments);
	}

	@Test
	public void testCountNewEnvironmentsWithNoObservationsInTempWorkbook() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		workbook.setObservations(null);

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(null);

		String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTest.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + ExpDesignControllerTest.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTest.ENVIRONMENTS, noOfNewEnvironments);
	}

	@Test
	public void testCountNewEnvironmentsWithNoObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);
		workbook.setObservations(null);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTest.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + ExpDesignControllerTest.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTest.ENVIRONMENTS, noOfNewEnvironments);
	}

	@Test
	public void testCountNewEnvironments() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTest.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + (Integer.parseInt(ExpDesignControllerTest.ENVIRONMENTS) - workbook.getTrialObservations().size())
				+ " but got " + noOfNewEnvironments + " instead.", Integer.parseInt(ExpDesignControllerTest.ENVIRONMENTS)
				- workbook.getTrialObservations().size(), Integer.parseInt(noOfNewEnvironments));
	}

	@Test
	public void testCountNewEnvironmentsWithExistingTempWorkbook() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(null);

		String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTest.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + (Integer.parseInt(ExpDesignControllerTest.ENVIRONMENTS) - workbook.getTrialObservations().size())
				+ " but got " + noOfNewEnvironments + " instead.", Integer.parseInt(ExpDesignControllerTest.ENVIRONMENTS)
				- workbook.getTrialObservations().size(), Integer.parseInt(noOfNewEnvironments));
	}

	@Test
	public void testCountNewEnvironmentsWithoutMeasurementData() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTest.NO_OF_OBSERVATIONS, StudyType.T);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTest.ENVIRONMENTS, this.userSelection, false);

		Assert.assertEquals("Expected " + ExpDesignControllerTest.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTest.ENVIRONMENTS, noOfNewEnvironments);
	}
}
