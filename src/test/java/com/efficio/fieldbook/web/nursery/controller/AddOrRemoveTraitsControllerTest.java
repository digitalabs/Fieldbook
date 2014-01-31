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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Debug;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.ImportWorkbookFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;

/**
 * The Class NurseryDetailsTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class AddOrRemoveTraitsControllerTest extends AbstractJUnit4SpringContextTests {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(NurseryDetailsTest.class);
    
    /** The file service. */
    
    /** The fieldbook service. */
    @Autowired
    FieldbookService fieldbookService;
    
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;

    /** The import germplasm file service. */
    @Autowired
    private ImportGermplasmFileService importGermplasmFileService;


    /** The workbook advance. */
    private org.apache.poi.ss.usermodel.Workbook workbookAdvance;
    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
    	 try {
             // InputStream inp = new FileInputStream("");


    		 InputStream inp = getClass().getClassLoader().getResourceAsStream(
                     "GermplasmImportTemplate-Advanced-rev4.xls");

             workbookAdvance = (org.apache.poi.ss.usermodel.Workbook) WorkbookFactory.create(inp);

         } catch (Exception e) {
             LOG.error(e.getMessage(), e);
         }
    }
    
    /**
     * Test valid nursery workbook.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAddOrRemoveTraitTest() throws Exception {
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
         List<MeasurementVariable> factors = new ArrayList();
         MeasurementVariable checkVariable = new MeasurementVariable("CHECK", "TYPE OF ENTRY", "CODE", "ASSIGNED", "CHECK", "C", "", "ENTRY");
         factors.add(checkVariable);
    	userSelection.getWorkbook().setFactors(factors);
        userSelection.getWorkbook().setVariates(new ArrayList());
        userSelection.setImportedGermplasmMainInfo(mainInfo);        
        List<MeasurementRow> measurementRows = measurementsGeneratorService.generateRealMeasurementRows(userSelection);
    	assertEquals(29, measurementRows.size());
    	assertEquals(1, measurementRows.get(0).getDataList().size());
    }
        
}
