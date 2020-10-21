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

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.google.common.collect.Lists;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ImportGermplasmListControllerTest {

	private static final int EH_CM_TERMID = 20316;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private Workbook workbook;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private DataImportService dataImportService;

	private UserSelection userSelection;

	@InjectMocks
	private ImportGermplasmListController importGermplasmListController;

	private CropType cropType;

	@Before
	public void setUp() {
		this.userSelection = new UserSelection();
		this.userSelection.setPlotsLevelList(WorkbookDataUtil.getPlotLevelList());
		this.userSelection.setWorkbook(this.workbook);
		this.importGermplasmListController.setUserSelection(this.userSelection);

		this.cropType = new CropType("maize");
	}

	@Test
	public void testAddVariablesFromTemporaryWorkbookToWorkbook() {

		final Workbook workbook = this.createWorkbook();
		final Workbook temporaryWorkbook = this.createWorkbookWithVariate();

		this.userSelection.setExperimentalDesignVariables(this.createDesignVariables());
		this.userSelection.setWorkbook(workbook);
		this.userSelection.setTemporaryWorkbook(temporaryWorkbook);

		this.importGermplasmListController.addVariablesFromTemporaryWorkbookToWorkbook(this.userSelection);

		Assert.assertEquals("The number of factors should be 7 (5 germplasm factors and 2 design factors)", 7,
			workbook.getFactors().size());
		Assert.assertEquals("The number of variates should be 1", 1, workbook.getVariates().size());
	}

	/**
	 * Test to verify nextScreen() works and performs steps as expected.
	 */
	@Test
	public void testNextScreen() {
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyTypeDto.getNurseryDto());

		workbook.setStudyDetails(studyDetails);

		workbook.setFactors(Lists.<MeasurementVariable>newArrayList());

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(workbook);

		Mockito.doNothing().when(this.fieldbookService).createIdCodeNameVariablePairs(ArgumentMatchers.isA(Workbook.class),
			ArgumentMatchers.isA(String.class));
		Mockito.doNothing().when(this.fieldbookService).createIdNameVariablePairs(ArgumentMatchers.isA(Workbook.class),
			ArgumentMatchers.anyListOf(SettingDetail.class), ArgumentMatchers.isA(String.class), ArgumentMatchers.anyBoolean());

		final Project project = new Project();
		project.setUniqueID("123");
		project.setUserId(1);
		project.setProjectId(Long.parseLong("123"));
		project.setCropType(this.cropType);
		Mockito.when(this.importGermplasmListController.getCurrentProject()).thenReturn(project);

		final Integer studyIdInSaveDataset = 3;

		Mockito.when(this.dataImportService.saveDataset(workbook, true, false, project.getUniqueID(), this.cropType))
			.thenReturn(studyIdInSaveDataset);

		Mockito.doNothing().when(this.fieldbookService).saveStudyColumnOrdering(studyIdInSaveDataset, null,
			workbook);

		final String studyIdInNextScreen = this.importGermplasmListController.nextScreen(form, null, null, null);

		Mockito.verify(this.fieldbookService).createIdCodeNameVariablePairs(ArgumentMatchers.isA(Workbook.class),
			ArgumentMatchers.isA(String.class));
		Mockito.verify(this.fieldbookService).createIdNameVariablePairs(ArgumentMatchers.isA(Workbook.class),
			ArgumentMatchers.anyListOf(SettingDetail.class), ArgumentMatchers.isA(String.class), ArgumentMatchers.anyBoolean());
		Mockito.verify(this.dataImportService).saveDataset(workbook, true, false, project.getUniqueID(),
			this.cropType);

		Mockito.verify(this.fieldbookService).saveStudyColumnOrdering(studyIdInSaveDataset, null, workbook);

		Assert.assertEquals("Expecting studyIdInSaveDataset returned from nextScreen", "3", studyIdInNextScreen);
	}

	private Workbook createWorkbook() {

		final Workbook workbook = new Workbook();

		workbook.setFactors(this.createFactors());
		workbook.setVariates(new ArrayList<>());
		workbook.setConditions(new ArrayList<>());

		return workbook;
	}

	private Workbook createWorkbookWithVariate() {
		final Workbook workbook = this.createWorkbook();

		workbook.getVariates().addAll(this.createVariates());

		return workbook;
	}

	private List<MeasurementVariable> createDesignVariables() {

		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(TermId.REP_NO.getId(), "REP_NO", "Replication factor", "Number",
			"Enumerated", "PLOT"));
		variables.add(this.createMeasurementVariable(TermId.PLOT_NO.getId(), "PLOT_NO", "Field plot", "Number",
			"Enumerated", "PLOT"));
		return variables;

	}

	private List<MeasurementVariable> createFactors() {
		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(TermId.GID.getId(), "GID", "Germplasm id", "Germplasm id",
			"Assigned", "ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.DESIG.getId(), "DESIGNATION", "Germplasm id",
			"Germplasm name", "Assigned", "ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO", "Germplasm entry", "Number",
			"Enumerated", "ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.CROSS.getId(), "CROSS", "Cross history", "Text", "Assigned",
			"ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.ENTRY_TYPE.getId(), "CHECK", "Entry type",
			"Type of ENTRY_TYPE", "Assigned", "ENTRY"));
		return variables;

	}

	private List<MeasurementVariable> createVariates() {
		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(ImportGermplasmListControllerTest.EH_CM_TERMID, "EH_cm",
			"Ear height", "cm", "EH measurement", "VARIATE"));
		return variables;

	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String property,
		final String scale, final String method, final String label) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		measurementVariable.setProperty(property);
		measurementVariable.setScale(scale);
		measurementVariable.setMethod(method);
		return measurementVariable;
	}

	protected StandardVariable createStandardVariable(final int termId, final String name, final Term property,
		final Term scale, final Term method, final Term dataType, final PhenotypicType phenotypicType) {

		final StandardVariable stdVar = new StandardVariable(property, scale, method, dataType, null, phenotypicType);
		stdVar.setId(termId);
		stdVar.setName(name);

		return stdVar;
	}

}
