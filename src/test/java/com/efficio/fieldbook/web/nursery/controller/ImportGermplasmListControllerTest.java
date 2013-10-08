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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;

import com.efficio.fieldbook.service.api.CropOntologyService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/Fieldbook-servlet.xml"})
public class ImportGermplasmListControllerTest extends AbstractJUnit4SpringContextTests {

        @Autowired
        private ImportGermplasmFileService importGermplasmFileService;
        
        private Workbook workbookBasic;
        private Workbook workbookAdvance;
        
        @Before
        public void setUp() {
          
            try{
                //InputStream inp = new FileInputStream("");
                
                InputStream inp =
                        getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Basic-rev4b-with_data.xls");
                
                workbookBasic = WorkbookFactory.create(inp);
                
                inp =
                        getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xls");
                workbookAdvance = WorkbookFactory.create(inp);
                
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        @Test
        public void testBasicParseImportGerplasm(){

            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            try{
                
                importGermplasmFileService.doProcessNow(workbookBasic, mainInfo); 
                
                if(mainInfo.getFileIsValid()){
                        //form.setHasError("0");
                        //userSelection.setImportedGermplasmMainInfo(mainInfo);
                        
                }else{
                        //meaing there is error
                    /*
                        form.setHasError("1");
                        for(String errorMsg : mainInfo.getErrorMessages()){
                                result.rejectValue("file", errorMsg);  
                        }
                      */  
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            
            assertEquals(mainInfo.getListName(),"<Enter name for germplasm list>");
            assertEquals(mainInfo.getListTitle(),"<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
            assertEquals(mainInfo.getListType(),"LST");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
            assertEquals(mainInfo.isAdvanceImportType(), false);
        }
        
        @Test
        public void testAdvanceParseImportGerplasm(){
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            try{
                
                importGermplasmFileService.doProcessNow(workbookAdvance, mainInfo); 
                
                if(mainInfo.getFileIsValid()){
                        //form.setHasError("0");
                        //userSelection.setImportedGermplasmMainInfo(mainInfo);
                        
                }else{
                        //meaing there is error
                    /*
                        form.setHasError("1");
                        for(String errorMsg : mainInfo.getErrorMessages()){
                                result.rejectValue("file", errorMsg);  
                        }
                      */  
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            
            assertEquals(mainInfo.getListName(),"<Enter name for germplasm list>");
            assertEquals(mainInfo.getListTitle(),"<Enter description of germplasm list here and details of germplasm to be imported on the Observation sheet>");
            assertEquals(mainInfo.getListType(),"LST");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(2).getFactor(), "GID");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(3).getFactor(), "CROSS");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(4).getFactor(), "SOURCE");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(5).getFactor(), "ENTRY CODE");
            assertEquals(mainInfo.isAdvanceImportType(), true);
            //assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
        }
      
        
}
