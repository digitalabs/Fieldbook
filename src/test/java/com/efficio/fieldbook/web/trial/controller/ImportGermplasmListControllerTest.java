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
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.google.common.collect.Lists;
import org.generationcp.commons.data.initializer.ImportedGermplasmTestDataInitializer;
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
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
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
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ImportGermplasmListControllerTest {

	private static final String STARTING_ENTRY_NUMBER = "1";
	private static final int EH_CM_TERMID = 20316;
	private static final int CHECK_TYPE = 1;
	private static final Integer PROJECT_ID = 97;
	private static final Integer GERMPLASM_LIST_ID = 98;
	private static final Integer STUDY_ID = 99;
	private static final Integer STARTING_ENTRY_NO = 10;
	private static final int TOTAL_NUMBER_OF_ENTRIES = 20;

	private static final String DESIGNATION_FACTOR = "8250-key";
	private static final String GID_FACTOR = "8240-key";
	private static final String CROSS_FACTOR = "8377-key";
	private static final String ENTRY_NO_FACTOR = "8230-key";
	private static final int CURRENT_PAGE = 0;

	private final String programUUID = UUID.randomUUID().toString();

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

	@Mock
	private InventoryDataManager inventoryDataManager;

	private UserSelection userSelection;

	private final Integer LIST_ID = 1;

	@InjectMocks
	private ImportGermplasmListController importGermplasmListController;
	private final String cropPrefix = "ABCD";

	private List<Enumeration> checkList;

	@Before
	public void setUp() {

		final StandardVariable experimentalDesign = this.createStandardVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				"EXPT_DESIGN", new Term(2140, "Experimental design", "Experimental design"),
				new Term(61216, "Type of EXPT_DESIGN", "Type of EXPT_DESIGN_generated"),
				new Term(4030, "Assigned", "Term name or id assigned"),
				new Term(TermId.NUMERIC_VARIABLE.getId(), "Numeric variable", ""), PhenotypicType.TRIAL_ENVIRONMENT);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.programUUID);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
				this.programUUID)).thenReturn(experimentalDesign);

		this.userSelection = new UserSelection();
		this.userSelection.setPlotsLevelList(WorkbookDataUtil.getPlotLevelList());
		this.userSelection.setWorkbook(this.workbook);
		Mockito.doReturn(this.createStudyDetails()).when(this.workbook).getStudyDetails();

		this.importGermplasmListController.setInventoryDataManager(this.inventoryDataManager);
		this.importGermplasmListController.setUserSelection(this.userSelection);

		this.checkList = this.createCheckList();
		Mockito.doReturn(this.checkList).when(this.fieldbookService).getCheckTypeList();
	}

	@Test
	public void testHasMeasurementTrialWithMeasurement() {

		this.userSelection.setMeasurementRowList(WorkbookDataUtil.createNewObservations(1));
		final Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertTrue(result);
	}

	@Test
	public void testHasMeasurementWithNullMeasurementRowList() {

		this.userSelection.setMeasurementRowList(null);

		final Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertFalse(result);
	}

	@Test
	public void testHasMeasurementTrialWithoutMeasurement() {

		this.userSelection.setMeasurementRowList(new ArrayList<MeasurementRow>());

		final Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertFalse(result);

	}

	@Test
	public void testDisplayGermplasmDetailsOfSelectedListForNursery() throws MiddlewareException {

		final List<GermplasmListData> list = this.createGermplasmListData();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn((long) list.size()).when(this.germplasmListManager)
				.countGermplasmListDataByListId(this.LIST_ID);

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();
		this.importGermplasmListController.displayGermplasmDetailsOfSelectedList(this.LIST_ID, form, model);

		Assert.assertTrue("If import is successful, isImportValid should be TRUE", this.userSelection.isImportValid());

		final List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model
				.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the
		// GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (final Map<String, Object> map : listDataTable) {
			// Test the values set in the list data table
			Assert.assertEquals(this.checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListControllerTest.DESIGNATION_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.GID_FACTOR));
			Assert.assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListControllerTest.CROSS_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.ENTRY_NO_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals("", map.get(ImportGermplasmListController.CHECK));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			x++;
		}

		// Test values set in form
		Assert.assertEquals("The starting entry number should be 1.", form.getStartingEntryNo(),
				ImportGermplasmListControllerTest.STARTING_ENTRY_NUMBER);
		Assert.assertEquals("The starting plot number should be 1.", form.getStartingPlotNo(),
				ImportGermplasmListController.STARTING_PLOT_NO);
	}

	@Test
	public void testDisplayGermplasmDetailsOfSelectedListForTrial() throws MiddlewareException {
		final List<GermplasmListData> list = this.createGermplasmListData();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn((long) list.size()).when(this.germplasmListManager)
				.countGermplasmListDataByListId(this.LIST_ID);

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();
		this.importGermplasmListController.displayGermplasmDetailsOfSelectedList(this.LIST_ID,
				form, model);

		Assert.assertTrue("If import is successful, isImportValid should be TRUE", this.userSelection.isImportValid());

		final List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model
				.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the
		// GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (final Map<String, Object> map : listDataTable) {
			// Test the values set in the list data table
			Assert.assertEquals(this.checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListControllerTest.DESIGNATION_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.GID_FACTOR));
			Assert.assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListControllerTest.CROSS_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.ENTRY_NO_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals("1", map.get(ImportGermplasmListController.CHECK));
			x++;
		}

		// Test values set in form
		Assert.assertEquals("The starting entry number should be 1.", form.getStartingEntryNo(),
				ImportGermplasmListControllerTest.STARTING_ENTRY_NUMBER);
		Assert.assertEquals("The starting plot number should be 1.", form.getStartingPlotNo(),
				ImportGermplasmListController.STARTING_PLOT_NO);
	}

	@Test
	public void testGenerateGermplasmListDataTableForTrial() {
		final List<ImportedGermplasm> list = ImportedGermplasmTestDataInitializer.createImportedGermplasmList();
		final List<Map<String, Object>> dataTableDataList =
			this.importGermplasmListController.generateGermplasmListDataTable(list, "1", false);

		int x = 1;
		for (final Map<String, Object> map : dataTableDataList) {
			Assert.assertEquals("The position's value should be " + x, String.valueOf(x),
					map.get(ImportGermplasmListController.POSITION));
			Assert.assertNull("The entry code's value should be null",
					map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals("The check's value should be 1", "1", map.get(ImportGermplasmListController.CHECK));
			Assert.assertEquals("The entry's value should be " + x, String.valueOf(x),
					map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals("The check option's value should be " + this.checkList, this.checkList,
					map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals("The desig's value should be Desig", "Desig",
					map.get(ImportGermplasmListController.DESIG));
			Assert.assertEquals("The GID's value should be " + x, String.valueOf(x),
					map.get(ImportGermplasmListController.GID));
			x++;
		}
	}

	@Test
	public void testGenerateGermplasmListDataTableForNursery() {
		final List<ImportedGermplasm> list = ImportedGermplasmTestDataInitializer.createImportedGermplasmList();
		final List<Map<String, Object>> dataTableDataList =
			this.importGermplasmListController.generateGermplasmListDataTable(list, "1", false);

		int x = 1;
		for (final Map<String, Object> map : dataTableDataList) {
			Assert.assertEquals("The position's value should be " + x, String.valueOf(x),
					map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals("The entry code's value should be " + x, String.valueOf(x),
					map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals("The check's value should be empty string", "",
					map.get(ImportGermplasmListController.CHECK));
			Assert.assertEquals("The entry's value should be " + x, String.valueOf(x),
					map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals("The check option's value should be " + this.checkList, this.checkList,
					map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals("The desig's value should be Desig", "Desig",
					map.get(ImportGermplasmListController.DESIG));
			Assert.assertEquals("The GID's value should be " + x, String.valueOf(x),
					map.get(ImportGermplasmListController.GID));
			x++;
		}
	}

	@Test
	public void testDisplayGermplasmDetailsOfCurrentStudyForNursery() throws MiddlewareException {
		final List<GermplasmListData> list = this.createGermplasmListData();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn((long) list.size()).when(this.germplasmListManager)
				.countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListsByProjectId(ImportGermplasmListControllerTest.STUDY_ID, GermplasmListType.STUDY);
		Mockito.doReturn(this.createListDataProject()).when(this.fieldbookMiddlewareService)
				.getListDataProject(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();
		this.importGermplasmListController.displayGermplasmDetailsOfCurrentStudy(form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

		final List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model
				.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the
		// GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (final Map<String, Object> map : listDataTable) {
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY_CODE));
			Assert.assertEquals("", map.get(ImportGermplasmListController.CHECK));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals(this.checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListControllerTest.DESIGNATION_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.GID_FACTOR));
			Assert.assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListControllerTest.CROSS_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.ENTRY_NO_FACTOR));
			x++;
		}

	}

	@Test
	public void testInitializeObjectsForGermplasmDetailsView() {
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();
		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final List<ImportedGermplasm> list = ImportedGermplasmMainInfoInitializer.createImportedGermplasmList();
		final List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		this.importGermplasmListController.initializeObjectsForGermplasmDetailsView(form, model, mainInfo, list,
				dataTableDataList);

		Assert.assertEquals("The current page should be " + ImportGermplasmListControllerTest.CURRENT_PAGE,
				ImportGermplasmListControllerTest.CURRENT_PAGE, form.getCurrentPage());
		Assert.assertEquals("The current page should be " + ImportGermplasmListControllerTest.CURRENT_PAGE,
				ImportGermplasmListControllerTest.CURRENT_PAGE, this.userSelection.getCurrentPageGermplasmList());
		Assert.assertEquals("The main info should be " + mainInfo, mainInfo,
				this.userSelection.getImportedGermplasmMainInfo());
		Assert.assertTrue("The isImportValid's value should be true", this.userSelection.isImportValid());
		Assert.assertEquals("The checklist should be " + this.checkList, this.checkList,
				model.get(ImportGermplasmListController.CHECK_LISTS));
		Assert.assertEquals("The data table list should be " + dataTableDataList, dataTableDataList,
				model.get(ImportGermplasmListController.LIST_DATA_TABLE));
		Assert.assertNotNull("The table header list should not be null",
				ImportGermplasmListController.TABLE_HEADER_LIST);
		Assert.assertEquals("The starting plot no should be " + ImportGermplasmListController.STARTING_PLOT_NO,
				ImportGermplasmListController.STARTING_PLOT_NO, form.getStartingPlotNo());
		Assert.assertEquals("The starting entry no should be 1", "1", form.getStartingEntryNo());
		Assert.assertEquals("The main info should be" + mainInfo, mainInfo, this.userSelection.getImportedGermplasmMainInfo());
		Assert.assertNotNull("The imported germplasm list should not be null", this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList());
		Assert.assertEquals("The imported germplasm should be " + list, list, this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
	}
	
	@Test
	public void testInitializeObjectsForGermplasmDetailsViewWhereListIsEmpty() {
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();
		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final List<ImportedGermplasm> list = new ArrayList<>();
		final List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		this.importGermplasmListController.initializeObjectsForGermplasmDetailsView(form, model, mainInfo, list,
				dataTableDataList);

		Assert.assertEquals("The current page should be " + ImportGermplasmListControllerTest.CURRENT_PAGE,
				ImportGermplasmListControllerTest.CURRENT_PAGE, form.getCurrentPage());
		Assert.assertEquals("The current page should be " + ImportGermplasmListControllerTest.CURRENT_PAGE,
				ImportGermplasmListControllerTest.CURRENT_PAGE, this.userSelection.getCurrentPageGermplasmList());
		Assert.assertEquals("The main info should be " + mainInfo, mainInfo,
				this.userSelection.getImportedGermplasmMainInfo());
		Assert.assertTrue("The isImportValid's value should be true", this.userSelection.isImportValid());
		Assert.assertEquals("The checklist should be " + this.checkList, this.checkList,
				model.get(ImportGermplasmListController.CHECK_LISTS));
		Assert.assertEquals("The data table list should be " + dataTableDataList, dataTableDataList,
				model.get(ImportGermplasmListController.LIST_DATA_TABLE));
		Assert.assertNotNull("The table header list should not be null",
				ImportGermplasmListController.TABLE_HEADER_LIST);
		Assert.assertEquals("The starting plot no should be " + ImportGermplasmListController.STARTING_PLOT_NO,
				ImportGermplasmListController.STARTING_PLOT_NO, form.getStartingPlotNo());
		Assert.assertNull("The starting entry no should be null", form.getStartingEntryNo());
		Assert.assertEquals("The main info should be" + mainInfo, mainInfo, this.userSelection.getImportedGermplasmMainInfo());
		Assert.assertNull("The imported germplasm list should be null", this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList());
	}

	@Test
	public void testDisplayGermplasmDetailsOfCurrentStudyForTrial() throws MiddlewareException {
		final List<GermplasmListData> list = this.createGermplasmListData();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn((long) list.size()).when(this.germplasmListManager)
				.countGermplasmListDataByListId(this.LIST_ID);

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListsByProjectId(ImportGermplasmListControllerTest.STUDY_ID, GermplasmListType.STUDY);
		Mockito.doReturn(this.createListDataProject()).when(this.fieldbookMiddlewareService)
				.getListDataProject(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();
		this.importGermplasmListController.displayGermplasmDetailsOfCurrentStudy(form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

		final List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model
				.get(ImportGermplasmListController.LIST_DATA_TABLE);

		// Check if the content of list data table is equal to the
		// GermplasmListData
		Assert.assertEquals(5, listDataTable.size());

		int x = 1;
		for (final Map<String, Object> map : listDataTable) {
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
			Assert.assertEquals(1, map.get(ImportGermplasmListController.CHECK));
			Assert.assertEquals(this.checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
			Assert.assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListControllerTest.DESIGNATION_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.GID_FACTOR));
			Assert.assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListControllerTest.CROSS_FACTOR));
			Assert.assertEquals(String.valueOf(x), map.get(ImportGermplasmListControllerTest.ENTRY_NO_FACTOR));
			x++;
		}

	}

	@Test
	public void testDisplayCheckGermplasmDetails() throws MiddlewareException {
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(Matchers.anyInt());

		final List<GermplasmListData> list = this.createGermplasmListData();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn((long) list.size()).when(this.germplasmListManager)
				.countGermplasmListDataByListId(this.LIST_ID);

		this.importGermplasmListController.displayCheckGermplasmDetails(this.LIST_ID, form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

	}

	@Test
	public void testDisplaySelectedCheckGermplasmDetails() throws MiddlewareException {
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(Matchers.anyInt());

		final List<GermplasmListData> list = this.createGermplasmListData();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn((long) list.size()).when(this.germplasmListManager)
				.countGermplasmListDataByListId(this.LIST_ID);

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ExtendedModelMap model = new ExtendedModelMap();
		this.importGermplasmListController.displaySelectedCheckGermplasmDetails(form, model);

		Assert.assertTrue(this.userSelection.isImportValid());

	}

	@Test
	public void testMergePrimaryAndCheckGermplasmList() {

		final ImportGermplasmListForm form = new ImportGermplasmListForm();

		form.setImportedGermplasm(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		form.setImportedCheckGermplasm(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList());
		form.setCheckVariables(this.createCheckVariables(true));

		this.userSelection
				.setImportedGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());
		this.userSelection.setImportedCheckGermplasmMainInfo(
				ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		final List<ImportedGermplasm> mergedImportedGermplasm = this.createMergedImportedGermplasm();

		Mockito.when(this.mergeCheckService.mergeGermplasmList(Matchers.anyList(), Matchers.anyList(),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn(mergedImportedGermplasm);

		this.importGermplasmListController.mergePrimaryAndCheckGermplasmList(this.userSelection, form);

		Mockito.verify(this.mergeCheckService).updatePrimaryListAndChecksBeforeMerge(form);
		Mockito.verify(this.mergeCheckService).mergeGermplasmList(Matchers.anyList(), Matchers.anyList(),
				Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt());

		Assert.assertEquals(
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(),
				mergedImportedGermplasm);
		Assert.assertEquals(form.getImportedGermplasm(), mergedImportedGermplasm);

	}

	@Test
	public void testCopyImportedGermplasmFromUserSelectionToForm() {

		final ImportGermplasmListForm form = new ImportGermplasmListForm();

		this.userSelection
				.setImportedGermplasmMainInfo(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());
		this.userSelection.setImportedCheckGermplasmMainInfo(
				ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		this.importGermplasmListController.copyImportedGermplasmFromUserSelectionToForm(this.userSelection, form);

		Assert.assertTrue(
				this.userSelection.getImportedGermplasmMainInfo().equals(form.getImportedGermplasmMainInfo()));
		Assert.assertTrue(this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()
				.getImportedGermplasms().equals(form.getImportedGermplasm()));
		Assert.assertNotNull(this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()
				.getOriginalImportedGermplasms());

		Assert.assertTrue(this.userSelection.getImportedCheckGermplasmMainInfo()
				.equals(form.getImportedCheckGermplasmMainInfo()));
		Assert.assertTrue(this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList()
				.getImportedGermplasms().equals(form.getImportedCheckGermplasm()));
	}

	@Test
	public void testProcessChecksNoSelectedChecks() {

		final ImportGermplasmListForm form = new ImportGermplasmListForm();

		this.userSelection.setImportedCheckGermplasmMainInfo(
				ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		this.importGermplasmListController.processChecks(this.userSelection, form);

		final List<ImportedGermplasm> importedGermplasmList = this.userSelection.getImportedCheckGermplasmMainInfo()
				.getImportedGermplasmList().getImportedGermplasms();

		Assert.assertEquals("", importedGermplasmList.get(0).getEntryTypeValue());
		Assert.assertEquals(0, importedGermplasmList.get(0).getEntryTypeCategoricalID().intValue());

		Assert.assertEquals("", importedGermplasmList.get(1).getEntryTypeValue());
		Assert.assertEquals(0, importedGermplasmList.get(1).getEntryTypeCategoricalID().intValue());

	}

	@Test
	public void testProcessChecksWithSelectedChecks() {

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setSelectedCheck(new String[] { "10180", "10180" });

		this.userSelection.setImportedCheckGermplasmMainInfo(
				ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo());

		this.importGermplasmListController.processChecks(this.userSelection, form);

		final List<ImportedGermplasm> importedGermplasmList = this.userSelection.getImportedCheckGermplasmMainInfo()
				.getImportedGermplasmList().getImportedGermplasms();

		Assert.assertEquals("10180", importedGermplasmList.get(0).getEntryTypeValue());
		Assert.assertEquals(10180, importedGermplasmList.get(0).getEntryTypeCategoricalID().intValue());

		Assert.assertEquals("10180", importedGermplasmList.get(1).getEntryTypeValue());
		Assert.assertEquals(10180, importedGermplasmList.get(1).getEntryTypeCategoricalID().intValue());

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

	@Test
	public void addExperimentFactorToBeDeleted() {

		final List<MeasurementVariable> conditions = new ArrayList<>();
		this.importGermplasmListController.addExperimentFactorToBeDeleted(conditions);

		Assert.assertEquals("Experimental Design factor should be added to the conditions list", 1, conditions.size());

	}

	/**
	 * Test to verify nextScreen() works and performs steps as expected.
	 */
	@Test
	public void testNextScreen() throws BVDesignException {
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setStartingEntryNo("801");
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(new StudyTypeDto("N"));

		workbook.setStudyDetails(studyDetails);

		workbook.setFactors(Lists.<MeasurementVariable> newArrayList());

		this.userSelection.setTemporaryWorkbook(workbook);
		this.userSelection.setWorkbook(workbook);
		final ImportedGermplasmMainInfo importedGermplasmMainInfo = new ImportedGermplasmMainInfo();
		importedGermplasmMainInfo.setListId(4);

		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmMainInfo.setImportedGermplasmList(importedGermplasmList);
		final ArrayList<ImportedGermplasm> germplasmList = new ArrayList<>();
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setGid("1");
		importedGermplasm.setEntryId(1);
		importedGermplasm.setEntryCode("2");
		importedGermplasm.setDesig("(CML454 X CML451)-B-4-1-112");
		importedGermplasm.setEntryTypeCategoricalID(1);
		importedGermplasm.setSource("Source");
		importedGermplasm.setGroupName("Group Name");
		germplasmList.add(importedGermplasm);

		importedGermplasmList.setImportedGermplasms(germplasmList);

		this.userSelection.setImportedGermplasmMainInfo(importedGermplasmMainInfo);

		Mockito.doNothing().when(this.fieldbookService).createIdCodeNameVariablePairs(Matchers.isA(Workbook.class),
				Matchers.isA(String.class));
		Mockito.doNothing().when(this.fieldbookService).createIdNameVariablePairs(Matchers.isA(Workbook.class),
				Matchers.anyList(), Matchers.isA(String.class), Matchers.anyBoolean());

		final Project project = new Project();
		project.setUniqueID("123");
		project.setUserId(1);
		project.setProjectId(Long.parseLong("123"));
		final CropType cropType = new CropType();
		cropType.setPlotCodePrefix(this.cropPrefix);
		project.setCropType(cropType);
		Mockito.when(this.importGermplasmListController.getCurrentProject()).thenReturn(project);

		Mockito.when(this.workbenchService.getCurrentIbdbUserId(Matchers.isA(Long.class), Matchers.isA(Integer.class)))
				.thenReturn(1);
		final Integer studyIdInSaveDataset = 3;

		Mockito.when(this.dataImportService.saveDataset(workbook, true, true, project.getUniqueID(), this.cropPrefix))
				.thenReturn(studyIdInSaveDataset);
		Mockito.doNothing().when(this.fieldbookService).saveStudyImportedCrosses(Matchers.anyList(),
				Matchers.isA(Integer.class));

		final List<ListDataProject> listDataProjects = new ArrayList<>();
		final ListDataProject listDataProject = new ListDataProject();
		listDataProjects.add(listDataProject);

		Mockito.when(this.fieldbookMiddlewareService.saveOrUpdateListDataProject(3, GermplasmListType.STUDY, 4,
				listDataProjects, 7)).thenReturn(3);

		Mockito.doNothing().when(this.fieldbookService).saveStudyColumnOrdering(studyIdInSaveDataset, null, null,
				workbook);

		final String studyIdInNextScreen = this.importGermplasmListController.nextScreen(form, null, null, null);

		Mockito.verify(this.fieldbookService).createIdCodeNameVariablePairs(Matchers.isA(Workbook.class),
				Matchers.isA(String.class));
		Mockito.verify(this.fieldbookService).createIdNameVariablePairs(Matchers.isA(Workbook.class),
				Matchers.anyList(), Matchers.isA(String.class), Matchers.anyBoolean());
		Mockito.verify(this.workbenchService).getCurrentIbdbUserId(Matchers.isA(Long.class),
				Matchers.isA(Integer.class));
		Mockito.verify(this.dataImportService).saveDataset(workbook, true, true, project.getUniqueID(),
				this.cropPrefix);
		Mockito.verify(this.fieldbookService).saveStudyImportedCrosses(Matchers.anyList(), Matchers.isA(Integer.class));
		Mockito.verify(this.fieldbookService).saveStudyColumnOrdering(studyIdInSaveDataset, null, null, workbook);

		Assert.assertEquals("Expecting studyIdInSaveDataset returned from nextScreen", "3", studyIdInNextScreen);
	}

	@Test
	public void testValidateEntryAndPlotNoEmptyList() throws Exception {
		// create a stub of importGermplasmListController that we can test
		final ImportGermplasmListController controllerToTest = Mockito.mock(ImportGermplasmListController.class);
		final ImportGermplasmListForm form = new ImportGermplasmListForm();

		Mockito.when(controllerToTest.getUserSelection()).thenReturn(this.userSelection);
		Mockito.doCallRealMethod().when(controllerToTest).validateEntryAndPlotNo(form);

		controllerToTest.validateEntryAndPlotNo(form);

		// validateEntryAndPlotNo should not process if theres no imported
		// germplasm in the study
		// Matthew : changed 0 to 1 - please improve this test
		Mockito.verify(controllerToTest, Mockito.times(1)).computeTotalExpectedWithChecks(form);

	}

	@Test
	public void testComputeTotalExpectedWithChecksEmptyChecks() throws Exception {
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final ImportedGermplasmMainInfo mainInfo = Mockito.mock(ImportedGermplasmMainInfo.class);

		this.userSelection.setImportedGermplasmMainInfo(mainInfo);
		final ImportedGermplasmList importedList = Mockito.mock(ImportedGermplasmList.class);
		Mockito.doReturn(importedList).when(mainInfo).getImportedGermplasmList();
		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmList()).when(importedList)
				.getImportedGermplasms();

		final int count = this.importGermplasmListController.computeTotalExpectedWithChecks(form);
		Assert.assertEquals(
				"Unable to provide an accurate count of total expected germplasms if there is no check information",
				DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES, count);

	}

	private List<ListDataProject> createListDataProject() {
		final List<ListDataProject> list = new ArrayList<>();
		for (int x = 1; x <= 5; x++) {
			final ListDataProject data = new ListDataProject();
			data.setEntryId(x);
			data.setDesignation("DESIGNATION" + x);
			data.setEntryCode(String.valueOf(x));
			data.setGroupName("GROUPNAME" + x);
			data.setSeedSource("SEEDSOURCE" + x);
			data.setGermplasmId(x);
			data.setListDataProjectId(x);
			data.setCheckType(ImportGermplasmListControllerTest.CHECK_TYPE);
			data.setGroupId(0);
			list.add(data);
		}
		return list;
	}

	private StudyDetails createStudyDetails() {
		final StudyDetails details = new StudyDetails();
		details.setId(ImportGermplasmListControllerTest.STUDY_ID);
		return details;
	}

	private List<GermplasmList> createGermplasmList() {
		final List<GermplasmList> list = new ArrayList<>();
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);
		germplasmList.setProjectId(ImportGermplasmListControllerTest.PROJECT_ID);
		list.add(germplasmList);
		return list;
	}

	private List<Enumeration> createCheckList() {
		final List<Enumeration> list = new ArrayList<>();
		final Enumeration enumeration = new Enumeration();
		enumeration.setId(1);
		enumeration.setName("T");
		enumeration.setDescription("Test Entry");
		list.add(enumeration);
		return list;
	}

	private List<GermplasmListData> createGermplasmListData() {

		final List<GermplasmListData> list = new ArrayList<>();
		for (int x = 1; x <= 5; x++) {
			final GermplasmListData data = new GermplasmListData();
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

	private List<SettingDetail> createCheckVariables(final boolean hasValue) {
		final List<SettingDetail> checkVariables = new ArrayList<>();

		checkVariables.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_START.getId(),
				"CHECK_START", hasValue ? "1" : null, "TRIAL"));
		checkVariables.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_INTERVAL.getId(),
				"CHECK_INTERVAL", hasValue ? "4" : null, "TRIAL"));
		checkVariables.add(SettingDetailTestDataInitializer.createSettingDetail(TermId.CHECK_PLAN.getId(), "CHECK_PLAN",
				hasValue ? "8414" : null, "TRIAL"));

		return checkVariables;
	}

	protected List<ImportedGermplasm> createMergedImportedGermplasm() {
		final List<ImportedGermplasm> mergedImportedGermplasm = new ArrayList<>();
		for (int x = 1; x <= 8; x++) {
			mergedImportedGermplasm.add(ImportedGermplasmMainInfoInitializer.createImportedGermplasm(x));
		}
		return mergedImportedGermplasm;
	}

	private Workbook createWorkbook() {

		final Workbook workbook = new Workbook();

		workbook.setFactors(this.createFactors());
		workbook.setVariates(new ArrayList<MeasurementVariable>());
		workbook.setConditions(new ArrayList<MeasurementVariable>());

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

	@Test
	public void testCheckNumbersUpdatedAppropriately() throws Exception {
		// Unchanged check entry number if there are no new start entry number
		this.checkNumberTest(1, 5, 1, 1, null);
		this.checkNumberTest(0, 0, 3, 3, null);

		// Since the entry number starts at 100 we expect the check id to be
		// bumped to 100 too
		this.checkNumberTest(1, 5, 1, 100, "100");

		// Since the entry number starts at 50 and our choosen check id is 52 we
		// expect the check id to be bumped to 52 too
		this.checkNumberTest(1, 5, 3, 52, "50");

	}

	private void checkNumberTest(final int startEntryNumberForTestList, final int numberOfItemsInGermplasmList,
			final int checkNumberInCheckList, final int expectedGermplasmCheckEntryNumber,
			final String startEntryNumber) {
		final UserSelection userSelection = new UserSelection();
		userSelection.setImportedGermplasmMainInfo(this.getGermplasmMainInfo(startEntryNumberForTestList, 5));
		userSelection.setImportedCheckGermplasmMainInfo(this.getGermplasmMainInfo(checkNumberInCheckList, 1));

		final ImportGermplasmListController importGermplasmListController = new ImportGermplasmListController();
		importGermplasmListController.setUserSelection(userSelection);

		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setStartingEntryNo(startEntryNumber);
		form.setStartingPlotNo("100");
		importGermplasmListController.assignAndIncrementEntryNumberAndPlotNumber(form);

		Assert.assertEquals("We exepect this to ", userSelection.getImportedCheckGermplasmMainInfo()
				.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(),
				new Integer(expectedGermplasmCheckEntryNumber));
	}

	private ImportedGermplasmMainInfo getGermplasmMainInfo(final int startingEntryId, final int number) {
		final ImportedGermplasmMainInfo importedGermplasmMainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();
		for (int i = 0; i < number; i++) {
			germplasmList.add(new ImportedGermplasm(i + startingEntryId, "desig", "check"));
		}
		importedGermplasmList.setImportedGermplasms(germplasmList);
		importedGermplasmMainInfo.setImportedGermplasmList(importedGermplasmList);
		return importedGermplasmMainInfo;
	}

	/**
	 * Test to verify nextScreen() works and performs steps as expected.
	 */
	@Test
	public void testRefreshListDetails() {
		final ImportGermplasmListForm form = new ImportGermplasmListForm();
		final Model model = Mockito.mock(Model.class);
		final List<Enumeration> checkTypes = this.createCheckTypesTestData();
		Mockito.doReturn(checkTypes).when(this.fieldbookService).getCheckTypeList();
		this.updateUserSelection();
		this.importGermplasmListController.setUserSelection(this.userSelection);
		this.importGermplasmListController.refreshListDetails(model, form);
		Assert.assertEquals(this.userSelection.getImportedGermplasmMainInfo(), form.getImportedGermplasmMainInfo());
		Assert.assertEquals(
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(),
				form.getImportedGermplasm());
		Assert.assertEquals(this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()
				.getImportedGermplasms().get(0).getEntryId().toString(), form.getStartingEntryNo());
		Assert.assertEquals(Integer.toString(this.userSelection.getStartingEntryNo()), form.getStartingEntryNo());
		Mockito.verify(model).addAttribute(ImportGermplasmListController.CHECK_LISTS, checkTypes);
		Mockito.verify(model).addAttribute(ImportGermplasmListController.TYPE2, "T");
		Mockito.verify(model).addAttribute("hasMeasurement", false);

	}

	private void updateUserSelection() {
		this.userSelection.setStartingEntryNo(ImportGermplasmListControllerTest.STARTING_ENTRY_NO);
		this.userSelection.setPlotsLevelList(this.createPlotsLevelListTestData());
		this.userSelection.setImportedGermplasmMainInfo(this.createImportedGermplasmMainInfoTestData());
	}

	private ImportedGermplasmMainInfo createImportedGermplasmMainInfoTestData() {
		final ImportedGermplasmMainInfo importedGermplasmMainInfo = new ImportedGermplasmMainInfo();
		importedGermplasmMainInfo.setImportedGermplasmList(this.createImportedGermplasmsTestData());
		return importedGermplasmMainInfo;
	}

	private ImportedGermplasmList createImportedGermplasmsTestData() {
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		final List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		for (int entryNo = ImportGermplasmListControllerTest.STARTING_ENTRY_NO, count = 0; count < ImportGermplasmListControllerTest.TOTAL_NUMBER_OF_ENTRIES; entryNo++, count++) {
			importedGermplasms.add(this.createImportedGermplasmTestData(count, entryNo));
		}
		importedGermplasmList.setImportedGermplasms(importedGermplasms);
		return importedGermplasmList;
	}

	private ImportedGermplasm createImportedGermplasmTestData(final int index, final int entryNo) {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setIndex(index);
		importedGermplasm.setEntryId(entryNo);
		importedGermplasm.setMgid(entryNo * 10);
		return importedGermplasm;
	}

	private List<SettingDetail> createPlotsLevelListTestData() {
		final List<SettingDetail> plotsLevelList = new ArrayList<>();
		plotsLevelList.add(this.createSettingDetailTestData(TermId.GID.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.ENTRY_CODE.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.ENTRY_NO.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.SOURCE.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.GERMPLASM_SOURCE.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.CROSS.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.DESIG.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.CHECK.getId()));
		plotsLevelList.add(this.createSettingDetailTestData(TermId.GROUP_ID.getId()));
		return plotsLevelList;
	}

	private SettingDetail createSettingDetailTestData(final int termId) {
		final SettingDetail settingDetail = new SettingDetail();
		settingDetail.setVariable(this.createSettingVariable(termId));
		return settingDetail;
	}

	private SettingVariable createSettingVariable(final int termId) {
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(termId);
		return variable;
	}

	private List<Enumeration> createCheckTypesTestData() {
		final List<Enumeration> checkTypes = new ArrayList<>();
		checkTypes.add(new Enumeration(10170, "T", "Test entry", 1));
		checkTypes.add(new Enumeration(10170, "C", "Check entry", 2));
		checkTypes.add(new Enumeration(10170, "D", "Disease entry", 3));
		checkTypes.add(new Enumeration(10170, "S", "Stress entry", 4));
		return checkTypes;
	}

	@Test
	public void testUpdateEntryNumbersOfGermplasmList() {
		this.updateUserSelection();
		this.importGermplasmListController.setUserSelection(this.userSelection);
		final Integer newStartingEntryNo = 50;
		this.importGermplasmListController.updateEntryNumbersOfGermplasmList(newStartingEntryNo);
		final List<ImportedGermplasm> list = this.userSelection.getImportedGermplasmMainInfo()
				.getImportedGermplasmList().getImportedGermplasms();
		int expectedEntryNo = newStartingEntryNo;
		for (final ImportedGermplasm germplasm : list) {
			final Integer currentEntryNo = germplasm.getEntryId();
			Assert.assertEquals(new Integer(expectedEntryNo++), currentEntryNo);
		}
		Assert.assertEquals(newStartingEntryNo, this.userSelection.getStartingEntryNo());
	}

}
