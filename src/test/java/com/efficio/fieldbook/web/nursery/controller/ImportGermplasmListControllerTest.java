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

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class ImportGermplasmListControllerTest extends AbstractJUnit4SpringContextTests {
    
    private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmListControllerTest.class);

        @Autowired
        private ImportGermplasmFileService importGermplasmFileService;
        
        private Workbook workbookBasic;
        private Workbook workbookAdvance;
        private Workbook workbookBasicXlsx;
        private Workbook workbookAdvanceXlsx;
        private Workbook workbookInvalid;
        
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
                
                inp =
                        getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Basic-rev4b-with_data.xlsx");
                
                workbookBasicXlsx = WorkbookFactory.create(inp);
                
                inp =
                        getClass().getClassLoader().getResourceAsStream("GermplasmImportTemplate-Advanced-rev4.xlsx");
                workbookAdvanceXlsx = WorkbookFactory.create(inp);
                
                inp =
                        getClass().getClassLoader().getResourceAsStream("Population114_Pheno_FB_1.xls");
                workbookInvalid = WorkbookFactory.create(inp);
                
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        /*
        @Test
        public void testInvalidParseImportGerplasm(){

            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            try{
                
                importGermplasmFileService.doProcessNow(workbookInvalid, mainInfo); 
                assertEquals(mainInfo.getFileIsValid(), false);
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            
                      
        }
        */
        @Test
        public void testValidBasicParseImportGerplasm(){

            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            try{
                
                importGermplasmFileService.doProcessNow(workbookBasic, mainInfo); 
                
                
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            
            assertEquals(mainInfo.getListName(),"<Enter name for germplasm list>");
            assertEquals(mainInfo.getListTitle(),"<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
            assertEquals(mainInfo.getListType(),"LST");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
            assertEquals(mainInfo.isAdvanceImportType(), false);
            assertEquals(mainInfo.getFileIsValid(), true);
            //we check the parse data here
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");
            
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(), Integer.valueOf(20));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(), "IR 67632-14-2-5-1-2-B");
        }
        
        @Test
        public void testValidAdvanceParseImportGerplasm(){
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            try{
                
                importGermplasmFileService.doProcessNow(workbookAdvance, mainInfo); 
                
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
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
            assertEquals(mainInfo.getFileIsValid(), true);
            //test the parsing
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68201-21-2-B-4-B-B");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");
            
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(), Integer.valueOf(2));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(), "IR 67632-14-2-5-1-2-B");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
        }
        
        @Test
        public void testValidBasicParseImportGerplasmXlsx(){

            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            try{
                
                importGermplasmFileService.doProcessNow(workbookBasicXlsx, mainInfo); 
                
                
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            
            assertEquals(mainInfo.getListName(),"<Enter name for germplasm list>");
            assertEquals(mainInfo.getListTitle(),"<Enter description of germplasm list here then enter sequence number and names on the Observation sheet>");
            assertEquals(mainInfo.getListType(),"LST");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().size(), 20);
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(0).getFactor(), "ENTRY");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedFactors().get(1).getFactor(), "DESIGNATION");
            assertEquals(mainInfo.isAdvanceImportType(), false);
            assertEquals(mainInfo.getFileIsValid(), true);
            //we check the parse data here
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68835-58-1-1-B");
            
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getEntryId(), Integer.valueOf(20));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(19).getDesig(), "IR 67632-14-2-5-1-2-B");
        }
        
        /**
         * Test valid advance parse import gerplasm xlsx.
         */
        @Test
        public void testValidAdvanceParseImportGerplasmXlsx(){
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            try{
                
                importGermplasmFileService.doProcessNow(workbookAdvanceXlsx, mainInfo); 
                
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
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
            assertEquals(mainInfo.getFileIsValid(), true);
            //test the parsing
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryId(), Integer.valueOf(1));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getDesig(), "IR 68201-21-2-B-4-B-B");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getGid(), "1");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getCross(), "1");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getSource(), "1");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(0).getEntryCode(), "1");
            
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryId(), Integer.valueOf(2));
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getDesig(), "IR 67632-14-2-5-1-2-B");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getGid(), "2");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getCross(), "2");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getSource(), "2");
            assertEquals(mainInfo.getImportedGermplasmList().getImportedGermplasms().get(1).getEntryCode(), "2");
        }
        
        @Test
        public void testValidBasicParseImportGerplasmXlsPagination(){
          //testing when doing pagination, we simulate the pagination
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            ImportGermplasmListForm form = new ImportGermplasmListForm();
            try{
                
                importGermplasmFileService.doProcessNow(workbookBasic, mainInfo); 
                form.setImportedGermplasmMainInfo(mainInfo);
                form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
                
                
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            form.setCurrentPage(1);            
            assertEquals(form.getPaginatedImportedGermplasm().size(), form.getResultPerPage());            
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68835-58-1-1-B");//we check the parse data here
            
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getEntryId(), Integer.valueOf(10));
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getDesig(), "IR 68815-25-PMI 3-UBN 6-B-B");//we check the parse data here
            
            form.setCurrentPage(2);
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(11));
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68815-51-PMI 2-UBN 2-2-B");//we check the parse data here
            
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getEntryId(), Integer.valueOf(20));
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getDesig(), "IR 67632-14-2-5-1-2-B");//we check the parse data here
            
        }
        
        @Test
        public void testValidAdvanceParseImportGerplasmXlsPagination(){
            //testing when doing pagination, we simulate the pagination
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            ImportGermplasmListForm form = new ImportGermplasmListForm();
            try{
                
                importGermplasmFileService.doProcessNow(workbookAdvance, mainInfo); 
                form.setImportedGermplasmMainInfo(mainInfo);
                form.setImportedGermplasm(mainInfo.getImportedGermplasmList().getImportedGermplasms());
                
                
            }catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            form.setCurrentPage(1);            
            assertEquals(form.getPaginatedImportedGermplasm().size(), form.getResultPerPage());            
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(1));
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 68201-21-2-B-4-B-B");//we check the parse data here
            
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getEntryId(), Integer.valueOf(10));
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getDesig(), "IR 67632-14-2-5-1-2-B");//we check the parse data here
            
            form.setCurrentPage(2);
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(11));
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 67632-14-2-5-1-2-B");//we check the parse data here
            
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getEntryId(), Integer.valueOf(20));
            assertEquals(form.getPaginatedImportedGermplasm().get(9).getDesig(), "IR 67632-14-2-5-1-2-B");//we check the parse data here
            
            form.setCurrentPage(3);
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getEntryId(), Integer.valueOf(21));
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getDesig(), "IR 67632-14-2-5-1-2-B1");//we check the parse data here
            assertEquals(form.getPaginatedImportedGermplasm().get(0).getCross(), "21");//we check the parse data here
            
        }
      
        
}
