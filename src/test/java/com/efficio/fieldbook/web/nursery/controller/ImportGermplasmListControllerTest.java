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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;

public class ImportGermplasmListControllerTest extends AbstractBaseControllerIntegrationTest {

	private static final int CHECK_TYPE = 1;
	private static final Integer PROJECT_ID = 97;
	private static final Integer GERMPLASM_LIST_ID = 98;
	private static final Integer STUDY_ID = 99;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmListControllerTest.class);

	/** The import germplasm file service. */
	@Autowired
	private ImportGermplasmFileService importGermplasmFileService;

	/** The user selection. */
	@Spy
	private UserSelection userSelection;

	@Spy
	private ExtendedModelMap model;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private org.generationcp.middleware.domain.etl.Workbook workbook;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	/** The workbook basic. */
	private Workbook workbookBasic;

	/** The workbook advance. */
	private Workbook workbookAdvance;

	/** The workbook basic xlsx. */
	private Workbook workbookBasicXlsx;

	/** The workbook advance xlsx. */
	private Workbook workbookAdvanceXlsx;

	/** The workbook invalid. */
	private Workbook workbookInvalid;

	private final Integer LIST_ID = 1;

	@InjectMocks
	ImportGermplasmListController importGermplasmListController;

	/**
	 * Sets the up.
	 */
	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		try {
			// InputStream inp = new FileInputStream("");

			this.importGermplasmListController = Mockito.spy(this.importGermplasmListController);

			InputStream inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Basic-rev4b-with_data.xls");

			this.workbookBasic = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xls");
			this.workbookAdvance = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Basic-rev4b-with_data.xlsx");

			this.workbookBasicXlsx = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xlsx");
			this.workbookAdvanceXlsx = WorkbookFactory.create(inp);

			inp = this.getClass().getClassLoader().getResourceAsStream("Population114_Pheno_FB_1.xls");
			this.workbookInvalid = WorkbookFactory.create(inp);

		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Test valid basic parse import gerplasm.
	 */
	@Test
	public void testValidBasicParseImportGerplasm() {

		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {
			this.importGermplasmFileService.doProcessNow(this.workbookBasic, mainInfo);
		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertFalse(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// we check the parse data here
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(), Integer.valueOf(20));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(), "IR 67632-14-2-5-1-2-B");
	}

	/**
	 * Test valid advance parse import gerplasm.
	 */
	@Test
	public void testValidAdvanceParseImportGerplasm() {
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {

			this.importGermplasmFileService.doProcessNow(this.workbookAdvance, mainInfo);

		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here and details of germplasm to be imported on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(2).getFactor(), "GID");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(3).getFactor(), "CROSS");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(4).getFactor(), "SOURCE");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(5).getFactor(), "ENTRY CODE");
		Assert.assertTrue(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// test the parsing
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68201-21-2-B-4-B-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(), Integer.valueOf(2));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(), "IR 67632-14-2-5-1-2-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
	}

	/**
	 * Test valid basic parse import gerplasm xlsx.
	 */
	@Test
	public void testValidBasicParseImportGerplasmXlsx() {

		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {
			this.importGermplasmFileService.doProcessNow(this.workbookBasicXlsx, mainInfo);
		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertFalse(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// we check the parse data here
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(), Integer.valueOf(20));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(), "IR 67632-14-2-5-1-2-B");
	}

	/**
	 * Test valid advance parse import gerplasm xlsx.
	 */
	@Test
	public void testValidAdvanceParseImportGerplasmXlsx() {
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		try {
			this.importGermplasmFileService.doProcessNow(this.workbookAdvanceXlsx, mainInfo);
		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}

		Assert.assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
		Assert.assertEquals(mainInfo.getListTitle(),
				"<Enter description of germplasm list here and details of germplasm to be imported on the Observation sheet>");
		Assert.assertEquals(mainInfo.getListType(), "LST");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(2).getFactor(), "GID");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(3).getFactor(), "CROSS");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(4).getFactor(), "SOURCE");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(5).getFactor(), "ENTRY CODE");
		Assert.assertTrue(mainInfo.isAdvanceImportType());
		Assert.assertTrue(mainInfo.getFileIsValid());
		// test the parsing
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68201-21-2-B-4-B-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");

		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(), Integer.valueOf(2));
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(), "IR 67632-14-2-5-1-2-B");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
		Assert.assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
	}

	/**
	 * Test valid basic parse import gerplasm xls pagination.
	 */
	@Test
	public void testValidBasicParseImportGerplasmXlsPagination() {
		// testing when doing pagination, we simulate the pagination
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.importGermplasmFileService.doProcessNow(this.workbookBasic, mainInfo);
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}
		form.changePage(1);
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68835-58-1-1-B"); // we check the parse data here
	}

	/**
	 * Test valid advance parse import gerplasm xls pagination.
	 */
	@Test
	public void testValidAdvanceParseImportGerplasmXlsPagination() {
		// testing when doing pagination, we simulate the pagination
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.importGermplasmFileService.doProcessNow(this.workbookAdvance, mainInfo);
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}
		form.changePage(1);
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
		Assert.assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68201-21-2-B-4-B-B"); // we check the parse data
																												// here
	}

	@Test
	public void testValidAndAddCheckFactor() throws MiddlewareQueryException {
		// testing when doing pagination, we simulate the pagination
		ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		try {
			this.importGermplasmFileService.doProcessNow(this.workbookAdvance, mainInfo);
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
		} catch (Exception e) {
			ImportGermplasmListControllerTest.LOG.error(e.getMessage(), e);
		}
		UserSelection userSelection = new UserSelection();
		userSelection.setWorkbook(new org.generationcp.middleware.domain.etl.Workbook());
		List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();

		userSelection.getWorkbook().setFactors(factors);
		userSelection.getWorkbook().setVariates(new ArrayList<MeasurementVariable>());
		userSelection.setImportedGermplasmMainInfo(mainInfo);
		this.importGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(), userSelection.getImportedGermplasmMainInfo()
				.getImportedGermplasmList().getImportedGermplasms(), userSelection);
		// no check factor yet
		Assert.assertEquals(0, userSelection.getWorkbook().getMeasurementDatasetVariables().size());
		// we now need to add check
		MeasurementVariable checkVariable =
				new MeasurementVariable("CHECK", "TYPE OF ENTRY", "CODE", "ASSIGNED", "CHECK", "C", "", "ENTRY");
		factors.add(checkVariable);
		userSelection.getWorkbook().reset();
		userSelection.getWorkbook().setFactors(factors);
		// need to check if the CHECK was added
		Assert.assertEquals(1, userSelection.getWorkbook().getMeasurementDatasetVariables().size());
	}

	@Test
	public void testHasMeasurementTrialWithMeasurement() {

		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(Mockito.mock(List.class));
		Mockito.when(this.userSelection.getMeasurementRowList().isEmpty()).thenReturn(false);

		Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertTrue(result);
	}

	@Test
	public void testHasMeasurementWithNullMeasurementRowList() {

		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(null);

		Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertFalse(result);
	}

	@Test
	public void testHasMeasurementTrialWithoutMeasurement() {

		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(Mockito.mock(List.class));
		Mockito.when(this.userSelection.getMeasurementRowList().isEmpty()).thenReturn(true);

		Boolean result = this.importGermplasmListController.hasMeasurement();
		Assert.assertFalse(result);
	}

	@Test
	public void testDisplayGermplasmDetailsForNursery() throws MiddlewareQueryException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID, 0, list.size());
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn("1").when(this.importGermplasmListController).getCheckId(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckList();

		this.importGermplasmListController.displayGermplasmDetails(this.LIST_ID, "N", form, this.model);

		UserSelection userSelection = this.importGermplasmListController.getUserSelection();

		Assert.assertTrue("If import is successful, isImportValid should be TRUE", userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) this.model.get(ImportGermplasmListController.LIST_DATA_TABLE);

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
	public void testDisplayGermplasmDetailsForTrial() throws MiddlewareQueryException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID, 0, list.size());
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn("1").when(this.importGermplasmListController).getCheckId(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckList();

		this.importGermplasmListController.displayGermplasmDetails(this.LIST_ID, "T", form, this.model);

		UserSelection userSelection = this.importGermplasmListController.getUserSelection();

		Assert.assertTrue("If import is successful, isImportValid should be TRUE", userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) this.model.get(ImportGermplasmListController.LIST_DATA_TABLE);

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
	public void testDisplaySelectedGermplasmDetailsForNursery() throws MiddlewareQueryException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID, 0, list.size());
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn("1").when(this.importGermplasmListController).getCheckId(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckList();
		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListsByProjectId(Integer.valueOf(ImportGermplasmListControllerTest.STUDY_ID), GermplasmListType.NURSERY);
		Mockito.doReturn(this.createListDataProject()).when(this.fieldbookMiddlewareService)
				.getListDataProject(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);

		Mockito.doReturn(this.workbook).when(this.userSelection).getWorkbook();
		Mockito.doReturn(this.createStudyDetails()).when(this.workbook).getStudyDetails();

		this.importGermplasmListController.displaySelectedGermplasmDetails("N", form, this.model);

		UserSelection userSelection = this.importGermplasmListController.getUserSelection();

		Assert.assertTrue(userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) this.model.get(ImportGermplasmListController.LIST_DATA_TABLE);

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
	public void testDisplaySelectedGermplasmDetailsForTrial() throws MiddlewareQueryException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID, 0, list.size());
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn("1").when(this.importGermplasmListController).getCheckId(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckList();
		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListsByProjectId(Integer.valueOf(ImportGermplasmListControllerTest.STUDY_ID), GermplasmListType.TRIAL);
		Mockito.doReturn(this.createListDataProject()).when(this.fieldbookMiddlewareService)
				.getListDataProject(ImportGermplasmListControllerTest.GERMPLASM_LIST_ID);

		Mockito.doReturn(this.workbook).when(this.userSelection).getWorkbook();
		Mockito.doReturn(this.createStudyDetails()).when(this.workbook).getStudyDetails();

		this.importGermplasmListController.displaySelectedGermplasmDetails("T", form, this.model);

		UserSelection userSelection = this.importGermplasmListController.getUserSelection();

		Assert.assertTrue(userSelection.isImportValid());

		List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) this.model.get(ImportGermplasmListController.LIST_DATA_TABLE);

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
	public void testDisplayCheckGermplasmDetails() throws MiddlewareQueryException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(Matchers.anyInt());

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID, 0, list.size());
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn("1").when(this.importGermplasmListController).getCheckId(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckList();

		this.importGermplasmListController.displayCheckGermplasmDetails(this.LIST_ID, form, this.model);

		UserSelection userSelection = this.importGermplasmListController.getUserSelection();

		Assert.assertTrue(userSelection.isImportValid());

	}

	@Test
	public void testDisplaySelectedCheckGermplasmDetails() throws MiddlewareQueryException {
		ImportGermplasmListForm form = new ImportGermplasmListForm();

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(Matchers.anyInt());

		List<GermplasmListData> list = this.createGermplasmListData();
		List<Enumeration> checkList = this.createCheckList();
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListDataByListId(this.LIST_ID, 0, list.size());
		Mockito.doReturn(Long.valueOf(list.size())).when(this.germplasmListManager).countGermplasmListDataByListId(this.LIST_ID);
		Mockito.doReturn("1").when(this.importGermplasmListController).getCheckId(Matchers.anyString(), Matchers.anyList());
		Mockito.doReturn(checkList).when(this.fieldbookService).getCheckList();

		Mockito.doReturn(this.workbook).when(this.userSelection).getWorkbook();
		Mockito.doReturn(this.createStudyDetails()).when(this.workbook).getStudyDetails();

		this.importGermplasmListController.displaySelectedCheckGermplasmDetails(form, this.model);

		UserSelection userSelection = this.importGermplasmListController.getUserSelection();

		Assert.assertTrue(userSelection.isImportValid());

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
		enumeration.setName("Name");
		enumeration.setDescription("Description");
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

}
