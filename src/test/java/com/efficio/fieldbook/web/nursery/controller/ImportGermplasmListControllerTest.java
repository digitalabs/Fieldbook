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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.google.common.collect.Lists;
import com.mchange.util.AssertException;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;

@RunWith(MockitoJUnitRunner.class)
public class ImportGermplasmListControllerTest {

	private static final int EH_CM_TERMID = 20316;
	private static final int CHECK_TYPE = 1;
	private static final Integer PROJECT_ID = 97;
	private static final Integer GERMPLASM_LIST_ID = 98;
	private static final Integer STUDY_ID = 99;

	private String programUUID = UUID.randomUUID().toString();

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private MergeCheckService mergeCheckService;

	@Mock
	private MeasurementsGeneratorService measurementsGeneratorService;

	@Mock
	private Workbook workbook;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private ImportGermplasmFileService importGermplasmFileService;

	@Mock
	private DataImportService dataImportService;

	@Mock
	private WorkbenchService workbenchService;

	private UserSelection userSelection;

	private final Integer LIST_ID = 1;

	@InjectMocks
	private ImportGermplasmListController importGermplasmListController;

	@Before
	public void setUp() {

		StandardVariable experimentalDesign =
				this.createStandardVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), "EXPT_DESIGN", new Term(2140, "Experimental design",
						"Experimental design"), new Term(61216, "Type of EXPT_DESIGN", "Type of EXPT_DESIGN_generated"), new Term(4030,
						"Assigned", "Term name or id assigned"), new Term(TermId.NUMERIC_VARIABLE.getId(), "Numeric variable", ""),
						PhenotypicType.TRIAL_ENVIRONMENT

				);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.programUUID);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), this.programUUID))
				.thenReturn(experimentalDesign);

		this.userSelection = new UserSelection();
	}

	@Test
	public void testHasMeasurementTrialWithMeasurement() {

		this.userSelection.setMeasurementRowList(WorkbookDataUtil.createNewObservations(1));

		this.importGermplasmListController.setUserSelection(this.userSelection);

		Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertTrue(result);
	}

	@Test
	public void testHasMeasurementWithNullMeasurementRowList() {

		this.userSelection.setMeasurementRowList(null);

		this.importGermplasmListController.setUserSelection(this.userSelection);

		Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertFalse(result);
	}

	@Test
	public void testHasMeasurementTrialWithoutMeasurement() {

		this.userSelection.setMeasurementRowList(new ArrayList<MeasurementRow>());

		this.importGermplasmListController.setUserSelection(this.userSelection);

		Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertFalse(result);

	}

	@Test
	public void testDisplayGermplasmDetailsForNursery() throws MiddlewareException {

		ImportGermplasmListForm form = new ImportGermplasmListForm();
		ExtendedModelMap model = new ExtendedModelMap();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();

		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckTypeList();

		this.importGermplasmListController.setUserSelection(this.userSelection);

		this.importGermplasmListController.displayGermplasmDetails(this.LIST_ID, "N", form, model);

		Assert.assertTrue("If import is successful, isImportValid should be TRUE", this.userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (Map<String, Object> map : listDataTable) {
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
			Assert.assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListController.CROSS));
			Assert.assertEquals("SEEDSOURCE" + x, map.get(ImportGermplasmListController.SOURCE));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals("", map.get(ImportGermplasmListController.CHECK));
			x++;
		}

	}

	@Test
	public void testDisplayGermplasmDetailsForTrial() throws MiddlewareException {

		ImportGermplasmListForm form = new ImportGermplasmListForm();
		ExtendedModelMap model = new ExtendedModelMap();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckTypeList();

		this.importGermplasmListController.setUserSelection(this.userSelection);

		this.importGermplasmListController.displayGermplasmDetails(this.LIST_ID, "T", form, model);

		Assert.assertTrue("If import is successful, isImportValid should be TRUE", this.userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (Map<String, Object> map : listDataTable) {
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
			Assert.assertEquals(null, map.get(ImportGermplasmListController.CROSS));
			Assert.assertEquals(null, map.get(ImportGermplasmListController.SOURCE));
			Assert.assertEquals(null, map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals("1", map.get(ImportGermplasmListController.CHECK));
			x++;
		}

	}

	@Test
	public void testDisplaySelectedGermplasmDetailsForNursery() throws MiddlewareException {

		ImportGermplasmListForm form = new ImportGermplasmListForm();
		ExtendedModelMap model = new ExtendedModelMap();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckTypeList();

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListsByProjectId(Integer.valueOf(ImportGermplasmListControllerTest.STUDY_ID), GermplasmListType.NURSERY);
		Mockito.doReturn(this.createListDataProject()).when(this.fieldbookMiddlewareService)
				.getListDataProject(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);

		this.userSelection.setWorkbook(this.workbook);
		this.importGermplasmListController.setUserSelection(this.userSelection);

		Mockito.doReturn(this.createStudyDetails()).when(this.workbook).getStudyDetails();

		this.importGermplasmListController.displaySelectedGermplasmDetails("N", form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (Map<String, Object> map : listDataTable) {
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
			Assert.assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListController.CROSS));
			Assert.assertEquals("SEEDSOURCE" + x, map.get(ImportGermplasmListController.SOURCE));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals("", map.get(ImportGermplasmListController.CHECK));
			x++;
		}

	}

	@Test
	public void testDisplaySelectedGermplasmDetailsForTrial() throws MiddlewareException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		ExtendedModelMap model = new ExtendedModelMap();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckTypeList();

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListsByProjectId(Integer.valueOf(ImportGermplasmListControllerTest.STUDY_ID), GermplasmListType.TRIAL);
		Mockito.doReturn(this.createListDataProject()).when(this.fieldbookMiddlewareService)
				.getListDataProject(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);

		this.userSelection.setWorkbook(this.workbook);
		this.importGermplasmListController.setUserSelection(this.userSelection);

		Mockito.doReturn(this.createStudyDetails()).when(this.workbook).getStudyDetails();

		this.importGermplasmListController.displaySelectedGermplasmDetails("T", form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (Map<String, Object> map : listDataTable) {
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
			Assert.assertEquals(null, map.get(ImportGermplasmListController.CROSS));
			Assert.assertEquals(null, map.get(ImportGermplasmListController.SOURCE));
			Assert.assertEquals(null, map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals(1, map.get(ImportGermplasmListController.CHECK));
			x++;
		}

	}

	@Test
	public void testDisplayCheckGermplasmDetails() throws MiddlewareException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		ExtendedModelMap model = new ExtendedModelMap();

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(Matchers.anyInt());

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckTypeList();

		this.importGermplasmListController.setUserSelection(this.userSelection);

		this.importGermplasmListController.displayCheckGermplasmDetails(this.LIST_ID, form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

	}

	@Test
	public void testDisplaySelectedCheckGermplasmDetails() throws MiddlewareException {

		ImportGermplasmListForm form = new ImportGermplasmListForm();
		ExtendedModelMap model = new ExtendedModelMap();

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(Matchers.anyInt());

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckTypeList();

		this.userSelection.setWorkbook(this.workbook);
		this.importGermplasmListController.setUserSelection(this.userSelection);

		Mockito.doReturn(this.createStudyDetails()).when(this.workbook).getStudyDetails();

		this.importGermplasmListController.displaySelectedCheckGermplasmDetails(form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

	}

	@Test
	public void testMergePrimaryAndCheckGermplasmList() {

		ImportGermplasmListForm form = new ImportGermplasmListForm();

		form.setImportedGermplasm(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		form.setImportedCheckGermplasm(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		form.setCheckVariables(this.createCheckVariables(true));

		this.userSelection.setImportedGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());
		this.userSelection.setImportedCheckGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		List<ImportedGermplasm> mergedImportedGermplasm = this.createMergedImportedGermplasm();

		Mockito.when(
				this.mergeCheckService.mergeGermplasmList(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyInt(),
						Mockito.anyInt(), Mockito.anyString())).thenReturn(mergedImportedGermplasm);

		this.importGermplasmListController.mergePrimaryAndCheckGermplasmList(this.userSelection, form);

		Mockito.verify(this.mergeCheckService).updatePrimaryListAndChecksBeforeMerge(form);
		Mockito.verify(this.mergeCheckService).mergeGermplasmList(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyString());

		Assert.assertEquals(this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(),
				mergedImportedGermplasm);
		Assert.assertEquals(form.getImportedGermplasm(), mergedImportedGermplasm);

	}

	@Test
	public void testCopyImportedGermplasmFromUserSelectionToForm() {

		ImportGermplasmListForm form = new ImportGermplasmListForm();

		this.userSelection.setImportedGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());
		this.userSelection.setImportedCheckGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		this.importGermplasmListController.copyImportedGermplasmFromUserSelectionToForm(this.userSelection, form);

		Assert.assertTrue(this.userSelection.getImportedGermplasmMainInfo().equals(form.getImportedGermplasmMainInfo()));
		Assert.assertTrue(this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
				.equals(form.getImportedGermplasm()));
		Assert.assertNotNull(this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getOriginalImportedGermplasms());

		Assert.assertTrue(this.userSelection.getImportedCheckGermplasmMainInfo().equals(form.getImportedCheckGermplasmMainInfo()));
		Assert.assertTrue(this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
				.equals(form.getImportedCheckGermplasm()));
	}

	@Test
	public void testProcessChecksNoSelectedChecks() {

		ImportGermplasmListForm form = new ImportGermplasmListForm();

		this.userSelection.setImportedCheckGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		this.importGermplasmListController.processChecks(this.userSelection, form);

		List<ImportedGermplasm> importedGermplasmList =
				this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

		Assert.assertEquals("", importedGermplasmList.get(0).getCheck());
		Assert.assertEquals(0, importedGermplasmList.get(0).getCheckId().intValue());

		Assert.assertEquals("", importedGermplasmList.get(1).getCheck());
		Assert.assertEquals(0, importedGermplasmList.get(1).getCheckId().intValue());

	}

	@Test
	public void testProcessChecksWithSelectedChecks() {

		ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setSelectedCheck(new String[] {"10180", "10180"});

		this.userSelection.setImportedCheckGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		this.importGermplasmListController.processChecks(this.userSelection, form);

		List<ImportedGermplasm> importedGermplasmList =
				this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

		Assert.assertEquals("10180", importedGermplasmList.get(0).getCheck());
		Assert.assertEquals(10180, importedGermplasmList.get(0).getCheckId().intValue());

		Assert.assertEquals("10180", importedGermplasmList.get(1).getCheck());
		Assert.assertEquals(10180, importedGermplasmList.get(1).getCheckId().intValue());

	}

	@Test
	public void testProcessImportedGermplasmAndChecks() {

		ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setImportedGermplasm(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		form.setImportedCheckGermplasm(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		form.setCheckVariables(this.createCheckVariables(true));

		this.userSelection.setImportedGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());
		this.userSelection.setImportedCheckGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());
		this.userSelection.setWorkbook(this.createWorkbook());

		List<ImportedGermplasm> mergedImportedGermplasm = this.createMergedImportedGermplasm();

		Mockito.when(
				this.mergeCheckService.mergeGermplasmList(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyInt(),
						Mockito.anyInt(), Mockito.anyString())).thenReturn(mergedImportedGermplasm);

		this.importGermplasmListController.processImportedGermplasmAndChecks(this.userSelection, form);

		Mockito.verify(this.importGermplasmFileService).validataAndAddCheckFactor(form.getImportedGermplasm(),
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), this.userSelection);
		Mockito.verify(this.measurementsGeneratorService).generateRealMeasurementRows(this.userSelection);
		Mockito.verify(this.fieldbookService).manageCheckVariables(this.userSelection, form);

	}

	@Test
	public void testAddVariablesFromTemporaryWorkbookToWorkbook() {

		Workbook workbook = this.createWorkbook();
		Workbook temporaryWorkbook = this.createWorkbookWithVariate();

		this.userSelection.setExperimentalDesignVariables(this.createDesignVariables());
		this.userSelection.setWorkbook(workbook);
		this.userSelection.setTemporaryWorkbook(temporaryWorkbook);

		this.importGermplasmListController.addVariablesFromTemporaryWorkbookToWorkbook(this.userSelection);

		Assert.assertEquals("The number of factors should be 7 (5 germplasm factors and 2 design factors)", 7, workbook.getFactors().size());
		Assert.assertEquals("The number of variates should be 1", 1, workbook.getVariates().size());
	}

	@Test
	public void addExperimentFactorToBeDeleted() {

		List<MeasurementVariable> conditions = new ArrayList<>();
		this.importGermplasmListController.addExperimentFactorToBeDeleted(conditions);

		Assert.assertEquals("Experimental Design factor should be added to the conditions list", 1, conditions.size());

	}

	/**
	 * Test to verify nextScreen() works and performs steps as expected.
	 */
	@Test
	public void testNextScreen() throws BVDesignException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setStartingEntryNo("801");
		Workbook workbook = new Workbook();
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyType.N);

		workbook.setStudyDetails(studyDetails);

		workbook.setFactors(Lists.<MeasurementVariable>newArrayList());

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(workbook);
		ImportedGermplasmMainInfo importedGermplasmMainInfo = new ImportedGermplasmMainInfo();
		importedGermplasmMainInfo.setListId(4);

		ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmMainInfo.setImportedGermplasmList(importedGermplasmList);
		ArrayList<ImportedGermplasm> germplasmList = new ArrayList<ImportedGermplasm>();
		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setGid("1");
		importedGermplasm.setEntryId(1);
		importedGermplasm.setEntryCode("2");
		importedGermplasm.setDesig("(CML454 X CML451)-B-4-1-112");
		importedGermplasm.setCheckId(1);
		importedGermplasm.setSource("Source");
		importedGermplasm.setGroupName("Group Name");
		germplasmList.add(importedGermplasm);

		importedGermplasmList.setImportedGermplasms(germplasmList);

		this.userSelection.setImportedGermplasmMainInfo(importedGermplasmMainInfo);

		this.importGermplasmListController.setUserSelection(this.userSelection);

		Mockito.doNothing().when(this.fieldbookService).createIdCodeNameVariablePairs(Mockito.isA(Workbook.class), Mockito.isA(String.class));
		Mockito.doNothing().when(this.fieldbookService).createIdNameVariablePairs(Mockito.isA(Workbook.class), Mockito.anyList(), Mockito.isA(String.class), Mockito.anyBoolean());

		Project project = new Project();
		project.setUniqueID("123");
		project.setUserId(1);
		project.setProjectId(Long.parseLong("123"));
		Mockito.when(this.importGermplasmListController.getCurrentProject()).thenReturn(project);

		Mockito.when(this.workbenchService.getCurrentIbdbUserId(Mockito.isA(Long.class), Mockito.isA(Integer.class))).thenReturn(1);
		Integer studyIdInSaveDataset = 3;

		Mockito.when(this.dataImportService.saveDataset(workbook, true, true, project.getUniqueID())).thenReturn(studyIdInSaveDataset);
		Mockito.doNothing().when(this.fieldbookService).saveStudyImportedCrosses(Mockito.anyList(), Mockito.isA(Integer.class));

		List<ListDataProject> listDataProjects = new ArrayList<>();
		ListDataProject listDataProject = new ListDataProject();
		listDataProjects.add(listDataProject);

		Mockito.when(this.fieldbookMiddlewareService.saveOrUpdateListDataProject(3, GermplasmListType.NURSERY, 4, listDataProjects, 7)).thenReturn(3);

		Mockito.doNothing().when(this.fieldbookService).saveStudyColumnOrdering(studyIdInSaveDataset, null, null, workbook);

		String studyIdInNextScreen = this.importGermplasmListController.nextScreen(form, null, null, null);

		Mockito.verify(this.fieldbookService).createIdCodeNameVariablePairs(Mockito.isA(Workbook.class), Mockito.isA(String.class));
		Mockito.verify(this.fieldbookService).createIdNameVariablePairs(Mockito.isA(Workbook.class), Mockito.anyList(), Mockito.isA(String.class), Mockito.anyBoolean());
		Mockito.verify(this.workbenchService).getCurrentIbdbUserId(Mockito.isA(Long.class), Mockito.isA(Integer.class));
		Mockito.verify(this.dataImportService).saveDataset(workbook, true, true, project.getUniqueID());
		Mockito.verify(this.fieldbookService).saveStudyImportedCrosses(Mockito.anyList(), Mockito.isA(Integer.class));
		Mockito.verify(this.fieldbookService).saveStudyColumnOrdering(studyIdInSaveDataset, null, null, workbook);

		Assert.assertEquals("Expecting studyIdInSaveDataset returned from nextScreen", "3", studyIdInNextScreen);
	}

	@Test
	public void testValidateEntryAndPlotNoEmptyList() throws Exception {
		// create a stub of importGermplasmListController that we can test
		ImportGermplasmListController controllerToTest = Mockito.mock(ImportGermplasmListController.class);
		ImportGermplasmListForm form = new ImportGermplasmListForm();

		Mockito.when(controllerToTest.getUserSelection()).thenReturn(this.userSelection);
		Mockito.doCallRealMethod().when(controllerToTest).validateEntryAndPlotNo(form);

		controllerToTest.validateEntryAndPlotNo(form);

		// validateEntryAndPlotNo should not process if theres no imported germplasm in the study
		Mockito.verify(controllerToTest,Mockito.times(0)).computeTotalExpectedChecks(form);
	}


	private List<ListDataProject> createListDataProject() {
		List<ListDataProject> list = new ArrayList<>();
		for (int x = 1; x <= 5; x++) {
			ListDataProject data = new ListDataProject();
			data.setEntryId(x);
			data.setDesignation("DESIGNATION" + x);
			data.setEntryCode(String.valueOf(x));
			data.setGroupName("GROUPNAME" + x);
			data.setSeedSource("SEEDSOURCE" + x);
			data.setGermplasmId(x);
			data.setListDataProjectId(x);
			data.setCheckType(ImportGermplasmListControllerTest.CHECK_TYPE);
			list.add(data);
		}
		return list;
	}

	private StudyDetails createStudyDetails() {
		StudyDetails details = new StudyDetails();
		details.setId(ImportGermplasmListControllerTest.STUDY_ID);
		return details;
	}

	private List<GermplasmList> createGermplasmList() {
		List<GermplasmList> list = new ArrayList<>();
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);
		germplasmList.setProjectId(ImportGermplasmListControllerTest.PROJECT_ID);
		list.add(germplasmList);
		return list;
	}

	private List<Enumeration> createCheckList() {
		List<Enumeration> list = new ArrayList<>();
		Enumeration enumeration = new Enumeration();
		enumeration.setId(1);
		enumeration.setName("T");
		enumeration.setDescription("Test Entry");
		list.add(enumeration);
		return list;
	}

	private List<GermplasmListData> createGermplasmListData() {

		List<GermplasmListData> list = new ArrayList<>();
		for (int x = 1; x <= 5; x++) {
			GermplasmListData data = new GermplasmListData();
			data.setId(x);
			data.setEntryId(x);
			data.setDesignation("DESIGNATION" + x);
			data.setEntryCode(String.valueOf(x));
			data.setGid(x);
			data.setGroupName("GROUPNAME" + x);
			data.setSeedSource("SEEDSOURCE" + x);
			data.setStatus(1);
			list.add(data);
		}
		return list;
	}

	private List<SettingDetail> createCheckVariables(boolean hasValue) {
		List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();

		checkVariables.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_START.getId(), "CHECK_START", hasValue ? "1"
				: null, "TRIAL"));
		checkVariables.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_INTERVAL.getId(), "CHECK_INTERVAL",
				hasValue ? "4" : null, "TRIAL"));
		checkVariables.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_PLAN.getId(), "CHECK_PLAN", hasValue ? "8414"
				: null, "TRIAL"));

		return checkVariables;
	}

	protected List<ImportedGermplasm> createMergedImportedGermplasm() {
		List<ImportedGermplasm> mergedImportedGermplasm = new ArrayList<>();
		for (int x = 1; x <= 8; x++) {
			mergedImportedGermplasm.add(ImportedGermplasmMainInfoInitializer.createImportedGermplasm(x));
		}
		return mergedImportedGermplasm;
	}

	private Workbook createWorkbook() {

		Workbook workbook = new Workbook();

		workbook.setFactors(this.createFactors());
		workbook.setVariates(new ArrayList<MeasurementVariable>());
		workbook.setConditions(new ArrayList<MeasurementVariable>());

		return workbook;
	}

	private Workbook createWorkbookWithVariate() {
		Workbook workbook = this.createWorkbook();

		workbook.getVariates().addAll(this.createVariates());

		return workbook;
	}

	private List<MeasurementVariable> createDesignVariables() {

		List<MeasurementVariable> variables = new ArrayList<>();
		variables
				.add(this.createMeasurementVariable(TermId.REP_NO.getId(), "REP_NO", "Replication factor", "Number", "Enumerated", "PLOT"));
		variables.add(this.createMeasurementVariable(TermId.PLOT_NO.getId(), "PLOT_NO", "Field plot", "Number", "Enumerated", "PLOT"));
		return variables;

	}

	private List<MeasurementVariable> createFactors() {
		List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(TermId.GID.getId(), "GID", "Germplasm id", "Germplasm id", "Assigned", "ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.DESIG.getId(), "DESIGNATION", "Germplasm id", "Germplasm name", "Assigned",
				"ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO", "Germplasm entry", "Number", "Enumerated",
				"ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.CROSS.getId(), "CROSS", "Cross history", "Text", "Assigned", "ENTRY"));
		variables.add(this.createMeasurementVariable(TermId.ENTRY_TYPE.getId(), "CHECK", "Entry type", "Type of ENTRY_TYPE", "Assigned",
				"ENTRY"));
		return variables;

	}

	private List<MeasurementVariable> createVariates() {
		List<MeasurementVariable> variables = new ArrayList<>();
		variables.add(this.createMeasurementVariable(EH_CM_TERMID, "EH_cm", "Ear height", "cm", "EH measurement", "VARIATE"));
		return variables;

	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String property, final String scale,
			final String method, final String label) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setName(name);
		measurementVariable.setLabel(label);
		measurementVariable.setProperty(property);
		measurementVariable.setScale(scale);
		measurementVariable.setMethod(method);
		return measurementVariable;
	}

	protected StandardVariable createStandardVariable(int termId, String name, Term property, Term scale, Term method, Term dataType,
			PhenotypicType phenotypicType) {

		StandardVariable stdVar = new StandardVariable(property, scale, method, dataType, null, phenotypicType);
		stdVar.setId(termId);
		stdVar.setName(name);

		return stdVar;
	}

	@Test
	public void testCheckNumbersUpdatedAppropriately() throws Exception {
		//Unchanged check entry number if there are no new start entry number
		checkNumberTest(1, 5, 1, 1, null);
		checkNumberTest(0, 0, 3, 3, null);

		// Since the entry number starts at 100 we expect the check id to be bumped to 100 too
		checkNumberTest(1, 5, 1, 100, "100");

		// Since the entry number starts at 50 and our choosen check id is 52 we expect the check id to be bumped to 52 too
		checkNumberTest(1, 5, 3, 52, "50");


	}

	private void checkNumberTest(final int startEntryNumberForTestList, final int numberOfItemsInGermplasmList,
			final int checkNumberInCheckList,
			final int expectedGermplasmCheckEntryNumber, final String startEntryNumber) {
		final UserSelection userSelection = new UserSelection();
		userSelection.setImportedGermplasmMainInfo(getGermplasmMainInfo(startEntryNumberForTestList, 5));
		userSelection.setImportedCheckGermplasmMainInfo(getGermplasmMainInfo(checkNumberInCheckList, 1));

		final ImportGermplasmListController importGermplasmListController = new ImportGermplasmListController();
		importGermplasmListController.setUserSelection(userSelection);

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setStartingEntryNo(startEntryNumber);
		form.setStartingPlotNo("100");
		importGermplasmListController.assignAndIncrementEntryNumberAndPlotNumber(form);

		assertEquals("We exepect this to ", userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(),
				new Integer(expectedGermplasmCheckEntryNumber));
	}

	private ImportedGermplasmMainInfo getGermplasmMainInfo(final int startingEntryId, final int number) {
		final ImportedGermplasmMainInfo importedGermplasmMainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();
		for (int i = 0; i <  number; i++) {
			germplasmList.add(new ImportedGermplasm(i+startingEntryId, "desig", "check"));
		}
		importedGermplasmList.setImportedGermplasms(germplasmList);
		importedGermplasmMainInfo.setImportedGermplasmList(importedGermplasmList);
		return importedGermplasmMainInfo;
	}

}
