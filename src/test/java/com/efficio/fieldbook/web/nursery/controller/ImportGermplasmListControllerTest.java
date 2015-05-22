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
package com.efficio.fieldbook.web.nursery.controller;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        	
        	importGermplasmListController = spy(importGermplasmListController);

            InputStream inp = getClass().getClassLoader().getResourceAsStream(
                    "GermplasmImportTemplate-Basic-rev4b-with_data.xls");

            workbookBasic = WorkbookFactory.create(inp);

            inp = getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xls");
            workbookAdvance = WorkbookFactory.create(inp);

            inp = getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Basic-rev4b-with_data.xlsx");

            workbookBasicXlsx = WorkbookFactory.create(inp);

            inp = getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xlsx");
            workbookAdvanceXlsx = WorkbookFactory.create(inp);

            inp = getClass().getClassLoader().getResourceAsStream("Population114_Pheno_FB_1.xls");
            workbookInvalid = WorkbookFactory.create(inp);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Test valid basic parse import gerplasm.
     */
    @Test
    public void testValidBasicParseImportGerplasm() {

        ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
        try {
            importGermplasmFileService.doProcessNow(workbookBasic, mainInfo);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
        assertEquals(mainInfo.getListTitle(),
                "<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
        assertEquals(mainInfo.getListType(), "LST");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
        assertFalse(mainInfo.isAdvanceImportType());
        assertTrue(mainInfo.getFileIsValid());
        // we check the parse data here
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(),
                Integer.valueOf(1));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");

        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(),
                Integer.valueOf(20));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(),
                "IR 67632-14-2-5-1-2-B");
    }

    /**
     * Test valid advance parse import gerplasm.
     */
    @Test
    public void testValidAdvanceParseImportGerplasm() {
        ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
        try {

            importGermplasmFileService.doProcessNow(workbookAdvance, mainInfo);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
        assertEquals(mainInfo.getListTitle(),
                "<Enter description of germplasm list here and details of germplasm to be imported on the Observation sheet>");
        assertEquals(mainInfo.getListType(), "LST");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(2).getFactor(), "GID");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(3).getFactor(), "CROSS");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(4).getFactor(), "SOURCE");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(5).getFactor(), "ENTRY CODE");
        assertTrue(mainInfo.isAdvanceImportType());
        assertTrue(mainInfo.getFileIsValid());
        // test the parsing
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(),
                Integer.valueOf(1));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(),
                "IR 68201-21-2-B-4-B-B");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");

        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(),
                Integer.valueOf(2));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(),
                "IR 67632-14-2-5-1-2-B");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
    }

    /**
     * Test valid basic parse import gerplasm xlsx.
     */
    @Test
    public void testValidBasicParseImportGerplasmXlsx() {

        ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
        try {
            importGermplasmFileService.doProcessNow(workbookBasicXlsx, mainInfo);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
        assertEquals(mainInfo.getListTitle(),
                "<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
        assertEquals(mainInfo.getListType(), "LST");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
        assertFalse(mainInfo.isAdvanceImportType());
        assertTrue(mainInfo.getFileIsValid());
        // we check the parse data here
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(),
                Integer.valueOf(1));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");

        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(),
                Integer.valueOf(20));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(),
                "IR 67632-14-2-5-1-2-B");
    }

    /**
     * Test valid advance parse import gerplasm xlsx.
     */
    @Test
    public void testValidAdvanceParseImportGerplasmXlsx() {
        ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
        try {
            importGermplasmFileService.doProcessNow(workbookAdvanceXlsx, mainInfo);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        assertEquals(mainInfo.getListName(), "<Enter name for germplasm list>");
        assertEquals(mainInfo.getListTitle(),
                "<Enter description of germplasm list here and details of germplasm to be imported on the Observation sheet>");
        assertEquals(mainInfo.getListType(), "LST");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(2).getFactor(), "GID");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(3).getFactor(), "CROSS");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(4).getFactor(), "SOURCE");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(5).getFactor(), "ENTRY CODE");
        assertTrue(mainInfo.isAdvanceImportType());
        assertTrue(mainInfo.getFileIsValid());
        // test the parsing
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(),
                Integer.valueOf(1));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(),
                "IR 68201-21-2-B-4-B-B");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");

        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(),
                Integer.valueOf(2));
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(),
                "IR 67632-14-2-5-1-2-B");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
        assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
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
            importGermplasmFileService.doProcessNow(workbookBasic, mainInfo);
            form.setImportedGermplasmMainInfo(mainInfo);
            form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        form.changePage(1);
        assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
        assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68835-58-1-1-B"); // we check the parse data here
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
            importGermplasmFileService.doProcessNow(workbookAdvance, mainInfo);
            form.setImportedGermplasmMainInfo(mainInfo);
            form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        form.changePage(1);
        assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
        assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68201-21-2-B-4-B-B"); // we check the parse data here
    }
    
    @Test
    public void testValidAndAddCheckFactor() throws MiddlewareQueryException {
        // testing when doing pagination, we simulate the pagination
        ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
        ImportGermplasmListForm form = new ImportGermplasmListForm();
        try {
            importGermplasmFileService.doProcessNow(workbookAdvance, mainInfo);
            form.setImportedGermplasmMainInfo(mainInfo);
            form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        UserSelection userSelection = new UserSelection();
        userSelection.setWorkbook(new org.generationcp.middleware.domain.etl.Workbook());
        List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
        
        userSelection.getWorkbook().setFactors(factors);
        userSelection.getWorkbook().setVariates(new ArrayList<MeasurementVariable> ());
        userSelection.setImportedGermplasmMainInfo(mainInfo);
        importGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(), userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), userSelection);
        //no check factor yet
        assertEquals(0, userSelection.getWorkbook().getMeasurementDatasetVariables().size());
        //we now need to add check
        MeasurementVariable checkVariable = new MeasurementVariable("CHECK", "TYPE OF ENTRY", "CODE", "ASSIGNED", "CHECK", "C", "", "ENTRY");
        factors.add(checkVariable);
        userSelection.getWorkbook().reset();
        userSelection.getWorkbook().setFactors(factors);
        // need to check if the CHECK was added
        assertEquals(1, userSelection.getWorkbook().getMeasurementDatasetVariables().size());
    }
    
    @Test
    public void testHasMeasurementTrialWithMeasurement() {
    	
    	when(userSelection.getMeasurementRowList()).thenReturn(mock(List.class));
    	when(userSelection.getMeasurementRowList().isEmpty()).thenReturn(false);
    	
    	Boolean result = importGermplasmListController.hasMeasurement();
    	assertTrue(result);
    }
    
    @Test
    public void testHasMeasurementWithNullMeasurementRowList() {
    	
    	when(userSelection.getMeasurementRowList()).thenReturn(null);
    	
    	Boolean result = importGermplasmListController.hasMeasurement();
    	assertFalse(result);
    }
    
    @Test
    public void testHasMeasurementTrialWithoutMeasurement() {
    	
    	when(userSelection.getMeasurementRowList()).thenReturn(mock(List.class));
    	when(userSelection.getMeasurementRowList().isEmpty()).thenReturn(true);
    	
    	Boolean result = importGermplasmListController.hasMeasurement();
    	assertFalse(result);
    }
    
    @Test
    public void testDisplayGermplasmDetailsForNursery() throws MiddlewareQueryException{
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	
    	List<GermplasmListData> list = createGermplasmListData();
    	List<Enumeration> checkList = createCheckList();
    	doReturn(list).when(germplasmListManager).getGermplasmListDataByListId(LIST_ID, 0, list.size());
    	doReturn(Long.valueOf(list.size())).when(germplasmListManager).countGermplasmListDataByListId(LIST_ID);
    	doReturn("1").when(importGermplasmListController).getCheckId(anyString(), anyList());
    	doReturn(checkList).when(fieldbookService).getCheckList();
    	
    	importGermplasmListController.displayGermplasmDetails(LIST_ID, "N", form, model);
    	
    	UserSelection userSelection = importGermplasmListController.getUserSelection();
    	
    	assertTrue("If import is successful, isImportValid should be TRUE", userSelection.isImportValid());
    	
    	List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);
    	
    	//Check if the content of list data table is equal to the GermplasmListData
        assertEquals(5, listDataTable.size());
        
        int x = 1;
        for(Map<String, Object> map : listDataTable){
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
        	assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
        	assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
        	assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListController.CROSS));
        	assertEquals("SEEDSOURCE" + x, map.get(ImportGermplasmListController.SOURCE));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY_CODE));
        	assertEquals("", map.get(ImportGermplasmListController.CHECK));
        	x++;
        }
        
    	
    }
    
    @Test
    public void testDisplayGermplasmDetailsForTrial() throws MiddlewareQueryException{
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	
    	List<GermplasmListData> list = createGermplasmListData();
    	List<Enumeration> checkList = createCheckList();
    	doReturn(list).when(germplasmListManager).getGermplasmListDataByListId(LIST_ID, 0, list.size());
    	doReturn(Long.valueOf(list.size())).when(germplasmListManager).countGermplasmListDataByListId(LIST_ID);
    	doReturn("1").when(importGermplasmListController).getCheckId(anyString(), anyList());
    	doReturn(checkList).when(fieldbookService).getCheckList();
    	
    	importGermplasmListController.displayGermplasmDetails(LIST_ID, "T", form, model);
    	
    	UserSelection userSelection = importGermplasmListController.getUserSelection();
    	
    	assertTrue("If import is successful, isImportValid should be TRUE", userSelection.isImportValid());
    	
    	List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);
    	
    	//Check if the content of list data table is equal to the GermplasmListData
        assertEquals(5, listDataTable.size());
        
        int x = 1;
        for(Map<String, Object> map : listDataTable){
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
        	assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
        	assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
        	assertEquals(null, map.get(ImportGermplasmListController.CROSS));
        	assertEquals(null, map.get(ImportGermplasmListController.SOURCE));
        	assertEquals(null, map.get(ImportGermplasmListController.ENTRY_CODE));
        	assertEquals("1", map.get(ImportGermplasmListController.CHECK));
        	x++;
        }
        
    }
    
    @Test
    public void testDisplaySelectedGermplasmDetailsForNursery() throws MiddlewareQueryException{
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	
    	List<GermplasmListData> list = createGermplasmListData();
    	List<Enumeration> checkList = createCheckList();
    	doReturn(list).when(germplasmListManager).getGermplasmListDataByListId(LIST_ID, 0, list.size());
    	doReturn(Long.valueOf(list.size())).when(germplasmListManager).countGermplasmListDataByListId(LIST_ID);
    	doReturn("1").when(importGermplasmListController).getCheckId(anyString(), anyList());
    	doReturn(checkList).when(fieldbookService).getCheckList();
    	doReturn(createGermplasmList()).when(fieldbookMiddlewareService).getGermplasmListsByProjectId(Integer.valueOf(STUDY_ID), GermplasmListType.NURSERY);
    	doReturn(createListDataProject()).when(fieldbookMiddlewareService).getListDataProject(GERMPLASM_LIST_ID);
    	
    	doReturn(workbook).when(userSelection).getWorkbook();
    	doReturn(createStudyDetails()).when(workbook).getStudyDetails();
    	
    	importGermplasmListController.displaySelectedGermplasmDetails("N", form, model);
    	
    	UserSelection userSelection = importGermplasmListController.getUserSelection();
    	
    	assertTrue(userSelection.isImportValid());
    	
    	List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);
    	
    	//Check if the content of list data table is equal to the GermplasmListData
        assertEquals(5, listDataTable.size());
        
        int x = 1;
        for(Map<String, Object> map : listDataTable){
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
        	assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
        	assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
        	assertEquals("GROUPNAME" + x, map.get(ImportGermplasmListController.CROSS));
        	assertEquals("SEEDSOURCE" + x, map.get(ImportGermplasmListController.SOURCE));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY_CODE));
        	assertEquals("", map.get(ImportGermplasmListController.CHECK));
        	x++;
        }
        
    	
    }
    
    @Test
    public void testDisplaySelectedGermplasmDetailsForTrial() throws MiddlewareQueryException{
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	
    	List<GermplasmListData> list = createGermplasmListData();
    	List<Enumeration> checkList = createCheckList();
    	doReturn(list).when(germplasmListManager).getGermplasmListDataByListId(LIST_ID, 0, list.size());
    	doReturn(Long.valueOf(list.size())).when(germplasmListManager).countGermplasmListDataByListId(LIST_ID);
    	doReturn("1").when(importGermplasmListController).getCheckId(anyString(), anyList());
    	doReturn(checkList).when(fieldbookService).getCheckList();
    	doReturn(createGermplasmList()).when(fieldbookMiddlewareService).getGermplasmListsByProjectId(Integer.valueOf(STUDY_ID), GermplasmListType.TRIAL);
    	doReturn(createListDataProject()).when(fieldbookMiddlewareService).getListDataProject(GERMPLASM_LIST_ID);
    	
    	doReturn(workbook).when(userSelection).getWorkbook();
    	doReturn(createStudyDetails()).when(workbook).getStudyDetails();
    	
    	importGermplasmListController.displaySelectedGermplasmDetails("T", form, model);
    	
    	UserSelection userSelection = importGermplasmListController.getUserSelection();
    	
    	assertTrue(userSelection.isImportValid());
    	
    	List<Map<String, Object>> listDataTable = (List<Map<String, Object>>) model.get(ImportGermplasmListController.LIST_DATA_TABLE);
    	
    	//Check if the content of list data table is equal to the GermplasmListData
        assertEquals(5, listDataTable.size());
        
        int x = 1;
        for(Map<String, Object> map : listDataTable){
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.POSITION));
        	assertEquals(checkList, map.get(ImportGermplasmListController.CHECK_OPTIONS));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.ENTRY));
        	assertEquals("DESIGNATION" + x, map.get(ImportGermplasmListController.DESIG));
        	assertEquals(String.valueOf(x), map.get(ImportGermplasmListController.GID));
        	assertEquals(null, map.get(ImportGermplasmListController.CROSS));
        	assertEquals(null, map.get(ImportGermplasmListController.SOURCE));
        	assertEquals(null, map.get(ImportGermplasmListController.ENTRY_CODE));
        	assertEquals(1, map.get(ImportGermplasmListController.CHECK));
        	x++;
        }
        
    	
    }
    
    @Test
    public void testDisplayCheckGermplasmDetails() throws MiddlewareQueryException{
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	
    	doReturn(null).when(ontologyDataManager).getTermById(anyInt());
    	
    	List<GermplasmListData> list = createGermplasmListData();
    	List<Enumeration> checkList = createCheckList();
    	doReturn(list).when(germplasmListManager).getGermplasmListDataByListId(LIST_ID, 0, list.size());
    	doReturn(Long.valueOf(list.size())).when(germplasmListManager).countGermplasmListDataByListId(LIST_ID);
    	doReturn("1").when(importGermplasmListController).getCheckId(anyString(), anyList());
    	doReturn(checkList).when(fieldbookService).getCheckList();
    	
    	importGermplasmListController.displayCheckGermplasmDetails(LIST_ID, form, model);
    	
    	UserSelection userSelection = importGermplasmListController.getUserSelection();
    
    	assertTrue(userSelection.isImportValid());
    	
    }
    
    @Test
    public void testDisplaySelectedCheckGermplasmDetails() throws MiddlewareQueryException{
    	ImportGermplasmListForm form = new ImportGermplasmListForm();
    	
    	doReturn(null).when(ontologyDataManager).getTermById(anyInt());
    	
    	List<GermplasmListData> list = createGermplasmListData();
    	List<Enumeration> checkList = createCheckList();
    	doReturn(list).when(germplasmListManager).getGermplasmListDataByListId(LIST_ID, 0, list.size());
    	doReturn(Long.valueOf(list.size())).when(germplasmListManager).countGermplasmListDataByListId(LIST_ID);
    	doReturn("1").when(importGermplasmListController).getCheckId(anyString(), anyList());
    	doReturn(checkList).when(fieldbookService).getCheckList();
    	
    	doReturn(workbook).when(userSelection).getWorkbook();
    	doReturn(createStudyDetails()).when(workbook).getStudyDetails();
    	
    	importGermplasmListController.displaySelectedCheckGermplasmDetails(form, model);
    	
    	UserSelection userSelection = importGermplasmListController.getUserSelection();
    
    	assertTrue(userSelection.isImportValid());
    	
    }
    
    private List<ListDataProject> createListDataProject() {
    	List<ListDataProject> list = new ArrayList<>();
    	for (int x = 1; x<=5; x++){
    		ListDataProject data = new ListDataProject();
    		data.setEntryId(x);
    		data.setDesignation("DESIGNATION" + x);
    		data.setEntryCode(String.valueOf(x));
    		data.setGroupName("GROUPNAME" + x);
    		data.setSeedSource("SEEDSOURCE" + x);
    		data.setGermplasmId(x);
    		data.setListDataProjectId(x);
    		data.setCheckType(CHECK_TYPE);
    		list.add(data);
    	}
		return list;
	}

	private StudyDetails createStudyDetails() {
    	StudyDetails details = new StudyDetails();
    	details.setId(STUDY_ID);
		return details;
	}

	private List<GermplasmList> createGermplasmList(){
    	List<GermplasmList> list = new ArrayList<>();
    	GermplasmList germplasmList = new GermplasmList();
    	germplasmList.setId(GERMPLASM_LIST_ID);
    	germplasmList.setProjectId(PROJECT_ID);
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

	private List<GermplasmListData> createGermplasmListData(){
    	
    	List<GermplasmListData> list = new ArrayList<>();
    	for (int x = 1; x<=5; x++){
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
