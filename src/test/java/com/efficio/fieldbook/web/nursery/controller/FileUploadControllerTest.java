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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ImportWorkbookFileService;
import com.efficio.fieldbook.web.nursery.validation.FileUploadFormValidator;
import com.efficio.fieldbook.web.nursery.form.FileUploadForm;
import com.efficio.fieldbook.service.api.FieldbookService;

/**
 * The Class FileUploadControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class FileUploadControllerTest extends AbstractJUnit4SpringContextTests {

	
	/** The fieldbook service. */
	@Autowired
	FieldbookService fieldbookService;
	
    /** The file service. */
    @Autowired
    ImportWorkbookFileService fileService;

    /** The data import service. */
    @Autowired
    DataImportService dataImportService;

	/** The controller. */
	private FileUploadController controller;
	
	/** The form. */
	private FileUploadForm form;
	
	/** The result. */
	private BindingResult result;
	
	/** The file. */
	private MultipartFile file;
	
	/** The user selection. */
	private UserSelection userSelection;
	
	/** The validator. */
	FileUploadFormValidator validator;
	
	/** The Constant FILE_NAME. */
	private static final String FILE_NAME = "Population114_Pheno_FB_1.xls";
	
	/** The Constant FILE_NAME_XLSX. */
	private static final String FILE_NAME_XLSX = "Population114_Pheno_FB_1.xlsx";
	
	/**
	 * Sets the up.
	 */
	@Before
        public void setUp() {
	    controller = new FileUploadController();
	    form = new FileUploadForm();
	    validator = new FileUploadFormValidator();
	    result = createMock(BindingResult.class);
            file = createMock(MultipartFile.class);
            
            form.setFile(file);
            userSelection = new UserSelection();     
            controller.setUserSelection(userSelection);
        }
	
	/**
	 * Test valid file xls.
	 *
	 * @throws Exception the exception
	 */
	@Test
        public void testValidFileXLS() throws Exception{
		
    	// Get the file
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
            String tempFileName = fieldbookService.storeUserWorkbook(inputStream);
            userSelection.setActualFileName(FILE_NAME);
            userSelection.setServerFileName(tempFileName);
    
            // Parse the file to create Workbook
            File file = fileService.retrieveCurrentWorkbookAsFile(userSelection);
            Workbook datasetWorkbook = dataImportService.parseWorkbook(file);
            StudyDetails studyDetails = datasetWorkbook.getStudyDetails();
            
            assertEquals(studyDetails.getStudyName().toString(), "pheno_t7");
            assertEquals(studyDetails.getTitle().toString(), "Phenotyping trials of the Population 114");
            assertEquals(studyDetails.getObjective().toString(), "To evaluate the Population 114");
//            assertEquals(studyDetails.getPmKey().toString(), "0");
            assertEquals(studyDetails.getStartDate().toString(), "20130805");
            assertEquals(studyDetails.getEndDate().toString(), "20130805");
            assertEquals(studyDetails.getStudyType().toString(), "T");
        }
	
	/**
	 * Test valid file xlsx.
	 *
	 * @throws Exception the exception
	 */
	@Test
        public void testValidFileXLSX() throws Exception{
                
        // Get the file
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME_XLSX);
            String tempFileName = fieldbookService.storeUserWorkbook(inputStream);
            userSelection.setActualFileName(FILE_NAME_XLSX);
            userSelection.setServerFileName(tempFileName);
    
            // Parse the file to create Workbook
            File file = fileService.retrieveCurrentWorkbookAsFile(userSelection);
            Workbook datasetWorkbook = dataImportService.parseWorkbook(file);
            StudyDetails studyDetails = datasetWorkbook.getStudyDetails();
            
            assertEquals(studyDetails.getStudyName().toString(), "pheno_t7");
            assertEquals(studyDetails.getTitle().toString(), "Phenotyping trials of the Population 114");
            assertEquals(studyDetails.getObjective().toString(), "To evaluate the Population 114");
//            assertEquals(studyDetails.getPmKey().toString(), "0");
            assertEquals(studyDetails.getStartDate().toString(), "20130805");
            assertEquals(studyDetails.getEndDate().toString(), "20130805");
            assertEquals(studyDetails.getStudyType().toString(), "T");
        }

	
	/**
	 * Test empty file handling.
	 */
	@Test
        public void testEmptyFileHandling() {
            form.setFile(null);
            
            result.rejectValue("file", FileUploadFormValidator.FILE_NOT_FOUND_ERROR);
            expect(result.hasErrors()).andReturn(true);
            replay(result);
    
            validator.validate(form, result);

            assertTrue(result.hasErrors());
        }

	/**
	 * Test non excel file upload.
	 */
	@Test
	public void testNonExcelFileUpload() {
            form.setFile(file);
            
            expect(file.getOriginalFilename()).andReturn("something.txt").anyTimes();
            result.rejectValue("file", FileUploadFormValidator.FILE_NOT_EXCEL_ERROR);
            expect(result.hasErrors()).andReturn(true);
            
            replay(result, file);
            
            validator.validate(form, result);
            assertTrue(result.hasErrors());
        }
}