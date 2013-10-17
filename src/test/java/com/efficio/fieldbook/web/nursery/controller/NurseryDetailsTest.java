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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
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
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ImportWorkbookFileService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/Fieldbook-servlet.xml"})
public class NurseryDetailsTest extends AbstractJUnit4SpringContextTests {
    
    private static final Logger LOG = LoggerFactory.getLogger(NurseryDetailsTest.class);
    
    @Autowired
    ImportWorkbookFileService fileService;
    
    @Autowired
    FieldbookService fieldbookService;
    
    @Autowired
    DataImportService dataImportService;
    
    private static final String FILE_NAME_VALID = "Population114_Pheno_FB_1.xls";
    private static final String FILE_NAME_INVALID = "GermplasmImportTemplate-Basic-rev4b-with_data.xls";
    
    NurseryDetailsController controllerValid;
    NurseryDetailsController controllerInvalid;

    @Before
    public void setUp() {
    }
    
    @Test
    public void testValidNurseryWorkbook() throws Exception {

        // Get the file
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME_VALID);
        String tempFileName = fieldbookService.storeUserWorkbook(inputStream);
        UserSelection userSelection = new UserSelection();
        userSelection.setActualFileName(FILE_NAME_VALID);
        userSelection.setServerFileName(tempFileName);

        // Parse the file to create Workbook
        File file = fileService.retrieveCurrentWorkbookAsFile(userSelection);
        Workbook datasetWorkbook = dataImportService.parseWorkbook(file);
        userSelection.setWorkbook(datasetWorkbook);

        controllerValid = new NurseryDetailsController();
        controllerValid.setUserSelection(userSelection);

        // Test if the workbook in the controller is valid
        Workbook workbook = controllerValid.getUserSelection().getWorkbook();
        
        assertTrue(workbook.getConditions() != null && workbook.getConditions().size() > 0);
        assertTrue(workbook.getFactors() != null && workbook.getFactors().size() > 0);
        assertTrue(workbook.getConstants() != null && workbook.getConstants().size() > 0);
        assertTrue(workbook.getVariates() != null && workbook.getVariates().size() > 0);
        
        // Output the nursery details
        LOG.debug("========== CONDITIONS ==========");
        printMeasurementVariables(workbook.getConditions(), 4);
        LOG.debug("========== FACTORS ==========");
        printMeasurementVariables(workbook.getFactors(), 4);
        LOG.debug("========== CONSTANTS ==========");
        printMeasurementVariables(workbook.getConstants(), 4);
        LOG.debug("========== VARIATES ==========");
        printMeasurementVariables(workbook.getVariates(), 4);
    }
    

    
    @Test
    public void testInvalidNurseryWorkbook() throws Exception {
        
        Workbook datasetWorkbook = null;
        
        try{
            
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME_INVALID);
            
            String tempFileName = fieldbookService.storeUserWorkbook(inputStream);
            UserSelection userSelection = new UserSelection();
            userSelection.setActualFileName(FILE_NAME_VALID);
            userSelection.setServerFileName(tempFileName);
    
            File file = fileService.retrieveCurrentWorkbookAsFile(userSelection);
            datasetWorkbook = dataImportService.parseWorkbook(file);
            userSelection.setWorkbook(datasetWorkbook);
    
            controllerInvalid = new NurseryDetailsController();
            controllerInvalid.setUserSelection(userSelection);
    
        } catch (WorkbookParserException e){
            assertTrue(datasetWorkbook == null); // Workbook was not parsed properly due to format errors
        }

    }
    

    private void printMeasurementVariables(List<MeasurementVariable> mVariables, int indent){
        for (MeasurementVariable mVar : mVariables){
            mVar.print(indent);
        }
        Debug.println(1,"");
    }
      
        
}
