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
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;

import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.controller.FileUploadController;
import com.efficio.fieldbook.web.nursery.service.ImportWorkbookFileService;
import com.efficio.fieldbook.web.nursery.validation.FileUploadFormValidator;
import com.efficio.fieldbook.web.nursery.form.FileUploadForm;
import com.efficio.fieldbook.service.api.FieldbookService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/Fieldbook-servlet.xml"})
public class FileUploadControllerTest extends AbstractJUnit4SpringContextTests {

	
	@Autowired
	FieldbookService fieldbookService;
	
	@Autowired
        ImportWorkbookFileService fileService;
	
	@Autowired
        DataImportService dataImportService;
	
	private FileUploadController controller;
	private FileUploadForm form;
	private BindingResult result;
	private Model model;
	private MultipartFile file;
	private HttpSession session;
	private UserSelection userSelection;
	
	private static final String fileName = "Population114_Pheno_FB_1.xls";
	
	@Before
        public void setUp() {
	    controller = new FileUploadController();
	    form = new FileUploadForm();
	    result = createMock(BindingResult.class);
            model = createMock(Model.class);
            file = createMock(MultipartFile.class);
            session = new MockHttpSession();
            
            form.setFile(file);
            userSelection = new UserSelection();     
            controller.setUserSelection(userSelection);
        }
	
	@Test
        public void testValidFile() throws Exception{
		
    	// Get the file
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            String tempFileName = fieldbookService.storeUserWorkbook(inputStream);
            userSelection.setActualFileName(fileName);
            userSelection.setServerFileName(tempFileName);
    
            // Parse the file to create Workbook
            File file = fileService.retrieveCurrentWorkbookAsFile(userSelection);
            Workbook datasetWorkbook = dataImportService.parseWorkbook(file);
            StudyDetails studyDetails = datasetWorkbook.getStudyDetails();
            
            assertEquals(studyDetails.getStudyName().toString(), "pheno_t7");
            assertEquals(studyDetails.getTitle().toString(), "Phenotyping trials of the Population 114");
            assertEquals(studyDetails.getObjective().toString(), "To evaluate the Population 114");
            assertEquals(studyDetails.getPmKey().toString(), "0");
            assertEquals(studyDetails.getStartDate().toString(), "20130805");
            assertEquals(studyDetails.getEndDate().toString(), "20130805");
            assertEquals(studyDetails.getStudyType().toString(), "T");
        }

	
	@Test
        public void testEmptyFileHandling() {
            form.setFile(null);
            
            result.rejectValue("file", FileUploadFormValidator.FILE_NOT_FOUND_ERROR);
            expect(result.hasErrors()).andReturn(true);
            replay(result);
    
            String navigationResult = controller.uploadFile(form, result, model, session);
    
            // verify if the expected methods in the mock object were called
            verify(result);
    
            assertNotSame("redirect:" + NurseryDetailsController.URL, navigationResult);
        }

	@Test
	public void testNonExcelFileUpload() {
            form.setFile(file);
            
            expect(file.getOriginalFilename()).andReturn("something.txt").anyTimes();
            result.rejectValue("file", FileUploadFormValidator.FILE_NOT_EXCEL_ERROR);
            expect(result.hasErrors()).andReturn(true);
            
            replay(result, file);
            
            String navigationResult = controller.uploadFile(form, result, model, session);
    
            // verify if the expected methods in the mock object were called
            verify(result, file);
    
            assertNotSame("redirect:" + NurseryDetailsController.URL, navigationResult);
        }
}