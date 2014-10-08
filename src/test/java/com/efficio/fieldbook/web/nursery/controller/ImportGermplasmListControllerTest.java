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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;

public class ImportGermplasmListControllerTest extends AbstractBaseControllerTest {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmListControllerTest.class);

    /** The import germplasm file service. */
    @Autowired
    private ImportGermplasmFileService importGermplasmFileService;

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

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {

        try {
            // InputStream inp = new FileInputStream("");

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
        
}
