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
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(null);
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(NO_OF_OBSERVATIONS);
		List<MeasurementRow> combinedMeasurementRows = expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, userSelection, true);
		
		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.", measurementRows.size(), combinedMeasurementRows.size());
	}
	
	@Test
	public void testCombineNewlyGeneratedMeasurementsWithNullObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		workbook.setObservations(null);
		
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(workbook);
		
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(NO_OF_OBSERVATIONS);
		List<MeasurementRow> combinedMeasurementRows = expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, userSelection, true);
		
		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.", measurementRows.size(), combinedMeasurementRows.size());
	}
	
	@Test
	public void testCombineNewlyGeneratedMeasurementsWithExisting() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(NO_OF_OBSERVATIONS);
		
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(workbook);
		
		List<MeasurementRow> combinedMeasurementRows = expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, userSelection, true);
		
		Assert.assertEquals("Expected " + (measurementRows.size() + workbook.getObservations().size()) + " but got " + combinedMeasurementRows.size() + " instead.", 
				measurementRows.size() + workbook.getObservations().size(), combinedMeasurementRows.size());
	}
	
	@Test
	public void testCombineNewlyGeneratedMeasurementsWithExistingTempWorkbook() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(NO_OF_OBSERVATIONS);
		
		userSelection.setTemporaryWorkbook(workbook);
		userSelection.setWorkbook(null);
		
		List<MeasurementRow> combinedMeasurementRows = expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, userSelection, true);
		
		Assert.assertEquals("Expected " + (measurementRows.size() + workbook.getObservations().size()) + " but got " + combinedMeasurementRows.size() + " instead.", 
				measurementRows.size() + workbook.getObservations().size(), combinedMeasurementRows.size());
	}
	
	@Test
	public void testCombineNewlyGeneratedMeasurementsWithoutMeasurementData() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		
		List<MeasurementRow> measurementRows = WorkbookDataUtil.createNewObservations(NO_OF_OBSERVATIONS);
		
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(workbook);
		
		List<MeasurementRow> combinedMeasurementRows = expDesignController.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, userSelection, false);
		
		Assert.assertEquals("Expected " + measurementRows.size() + " but got " + combinedMeasurementRows.size() + " instead.", measurementRows.size(), combinedMeasurementRows.size());
	}
	
	@Test
	public void testCountNewEnvironmentsWithNullWorkbook() {
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(null);
		
		String noOfNewEnvironments = expDesignController.countNewEnvironments(ENVIRONMENTS, userSelection, true);
		
		Assert.assertEquals("Expected " + ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.", ENVIRONMENTS, noOfNewEnvironments);
	}
	
	@Test
	public void testCountNewEnvironmentsWithNoObservationsInTempWorkbook() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		workbook.setObservations(null);
		
		userSelection.setTemporaryWorkbook(workbook);
		userSelection.setWorkbook(null);
 
		String noOfNewEnvironments = expDesignController.countNewEnvironments(ENVIRONMENTS, userSelection, true);
		
		Assert.assertEquals("Expected " + ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.", ENVIRONMENTS, noOfNewEnvironments);
	}
	
	@Test
	public void testCountNewEnvironmentsWithNoObservations() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		workbook.setObservations(null);
		
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(workbook);
 
		String noOfNewEnvironments = expDesignController.countNewEnvironments(ENVIRONMENTS, userSelection, true);
		
		Assert.assertEquals("Expected " + ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.", ENVIRONMENTS, noOfNewEnvironments);
	}
	
	@Test
	public void testCountNewEnvironments() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(workbook);
 
		String noOfNewEnvironments = expDesignController.countNewEnvironments(ENVIRONMENTS, userSelection, true);
		
		Assert.assertEquals("Expected " + (Integer.parseInt(ENVIRONMENTS) - workbook.getTrialObservations().size()) 
				+ " but got " + noOfNewEnvironments + " instead.", Integer.parseInt(ENVIRONMENTS) - workbook.getTrialObservations().size(), Integer.parseInt(noOfNewEnvironments));
	}

	@Test
	public void testCountNewEnvironmentsWithExistingTempWorkbook() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		
		userSelection.setTemporaryWorkbook(workbook);
		userSelection.setWorkbook(null);
 
		String noOfNewEnvironments = expDesignController.countNewEnvironments(ENVIRONMENTS, userSelection, true);
		
		Assert.assertEquals("Expected " + (Integer.parseInt(ENVIRONMENTS) - workbook.getTrialObservations().size()) 
				+ " but got " + noOfNewEnvironments + " instead.", Integer.parseInt(ENVIRONMENTS) - workbook.getTrialObservations().size(), Integer.parseInt(noOfNewEnvironments));
	}
	
	@Test
	public void testCountNewEnvironmentsWithoutMeasurementData() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATIONS, StudyType.T);
		
		userSelection.setTemporaryWorkbook(null);
		userSelection.setWorkbook(workbook);
 
		String noOfNewEnvironments = expDesignController.countNewEnvironments(ENVIRONMENTS, userSelection, false);
		
		Assert.assertEquals("Expected " + ENVIRONMENTS + " but got " + noOfNewEnvironments + " instead.", ENVIRONMENTS, noOfNewEnvironments);
	}	
}