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
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;

public class ExpDesignControllerTestIT extends AbstractBaseIntegrationTest {

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
		final List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS);
		final List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.",
				measurementRows.size(), combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithNullObservations() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));
		workbook.setObservations(null);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		final List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS);
		final List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.",
				measurementRows.size(), combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithExisting() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));
		final List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		final List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + (measurementRows.size() + workbook.getObservations().size()) + " but got "
				+ combinedMeasurementRows.size() + " instead.", measurementRows.size() + workbook.getObservations().size(),
				combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithExistingTempWorkbook() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));
		final List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS);

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(null);

		final List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, true);

		Assert.assertEquals("Expected " + (measurementRows.size() + workbook.getObservations().size()) + " but got "
				+ combinedMeasurementRows.size() + " instead.", measurementRows.size() + workbook.getObservations().size(),
				combinedMeasurementRows.size());
	}

	@Test
	public void testCombineNewlyGeneratedMeasurementsWithoutMeasurementData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));

		final List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		final List<MeasurementRow> combinedMeasurementRows =
				this.expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection, false);

		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.",
				measurementRows.size(), combinedMeasurementRows.size());
	}

	@Test
	public void testCountNewEnvironmentsWithNullWorkbook() {
		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(null);

		final String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTestIT.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + ExpDesignControllerTestIT.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTestIT.ENVIRONMENTS, noOfNewEnvironments);
	}

	@Test
	public void testCountNewEnvironmentsWithNoObservationsInTempWorkbook() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));
		workbook.setObservations(null);

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(null);

		final String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTestIT.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + ExpDesignControllerTestIT.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTestIT.ENVIRONMENTS, noOfNewEnvironments);
	}

	@Test
	public void testCountNewEnvironmentsWithNoObservations() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));
		workbook.setObservations(null);

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		final String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTestIT.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected " + ExpDesignControllerTestIT.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTestIT.ENVIRONMENTS, noOfNewEnvironments);
	}

	@Test
	public void testCountNewEnvironments() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		final String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTestIT.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected "
				+ (Integer.parseInt(ExpDesignControllerTestIT.ENVIRONMENTS) - workbook.getTrialObservations().size()) + " but got "
				+ noOfNewEnvironments + " instead.", Integer.parseInt(ExpDesignControllerTestIT.ENVIRONMENTS)
				- workbook.getTrialObservations().size(), Integer.parseInt(noOfNewEnvironments));
	}

	@Test
	public void testCountNewEnvironmentsWithExistingTempWorkbook() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(null);

		final String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTestIT.ENVIRONMENTS, this.userSelection, true);

		Assert.assertEquals("Expected "
				+ (Integer.parseInt(ExpDesignControllerTestIT.ENVIRONMENTS) - workbook.getTrialObservations().size()) + " but got "
				+ noOfNewEnvironments + " instead.", Integer.parseInt(ExpDesignControllerTestIT.ENVIRONMENTS)
				- workbook.getTrialObservations().size(), Integer.parseInt(noOfNewEnvironments));
	}

	@Test
	public void testCountNewEnvironmentsWithoutMeasurementData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(ExpDesignControllerTestIT.NO_OF_OBSERVATIONS, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));

		this.userSelection.setTemporaryWorkbook(null);
		this.userSelection.setWorkbook(workbook);

		final String noOfNewEnvironments =
				this.expDesignController.countNewEnvironments(ExpDesignControllerTestIT.ENVIRONMENTS, this.userSelection, false);

		Assert.assertEquals("Expected " + ExpDesignControllerTestIT.ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.",
				ExpDesignControllerTestIT.ENVIRONMENTS, noOfNewEnvironments);
	}
}
