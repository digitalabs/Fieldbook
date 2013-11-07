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
package com.efficio.fieldbook.web.trial.controller;


import java.io.IOException;

import org.generationcp.middleware.service.api.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.service.ImportWorkbookFileService;
import com.efficio.fieldbook.web.nursery.validation.FileUploadFormValidator;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.trial.form.FileUploadForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * The Class FileUploadController.
 */
@Controller
@RequestMapping(TrialFileUploadController.URL)
public class TrialFileUploadController extends AbstractBaseFieldbookController{

    
    private static final Logger LOG = LoggerFactory.getLogger(TrialFileUploadController.class);
    
    /** The Constant URL. */
    public static final String URL = "/TrialManager/fileUpload";
    
    /** The fieldbook service. */
    @Resource
    private FieldbookService fieldbookService;
	
    /** The user selection. */
    @Resource
    private TrialSelection trialSelection;	
	
    /** The data import service. */
    @Resource
    private DataImportService dataImportService;
    
    /** The import workbook file service. */
    @Resource
    private ImportWorkbookFileService importWorkbookFileService;

	
    /**
     * Shows the file upload screen
     *
     * @param uploadForm the upload form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("fileUploadForm") FileUploadForm uploadForm, Model model, HttpSession session) {
    	session.invalidate();
    	return super.show(model);
    }

    /**
     * Uploads file if it passes validation
     *
     * @param uploadForm the upload form
     * @param result the result
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("fileUploadForm") FileUploadForm uploadForm, BindingResult result, Model model, HttpSession session) {
    	FileUploadFormValidator validator = new FileUploadFormValidator();
        validator.validate(uploadForm, result);

        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
        	return show(uploadForm,model,session);
        } else {


            try {
            	String tempFileName = fieldbookService.storeUserWorkbook(uploadForm.getFile().getInputStream());
            	trialSelection.setServerFileName(tempFileName);
            	trialSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
                /*
                Workbook datasetWorkbook = null;
                File file = importWorkbookFileService.retrieveCurrentWorkbookAsFile(trialSelection);
                datasetWorkbook = dataImportService.parseWorkbook(file);
                trialSelection.setWorkbook(datasetWorkbook);
                */
            }catch (IOException e) {
                LOG.error(e.getMessage(), e);
                result.reject("uploadForm.file", "Error occurred while uploading file.");
            }/* catch(WorkbookParserException e){
                LOG.error(e.getMessage(), e);
                result.reject("uploadForm.file", "Error occurred while parsing file.");
            }*/
            
            //return "redirect:" + NurseryDetailsController.URL;
            return show(uploadForm,model,session);
        }
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/fileUpload";
    }
    
    /**
     * Gets the form.
     *
     * @return the form
     */
    @ModelAttribute("form")
    public FileUploadForm getForm() {
        return new FileUploadForm();
    }
    
    /**
     * Sets the fieldbook service.
     *
     * @param fieldbookService the new fieldbook service
     */
    public void setFieldbookService(FieldbookService fieldbookService) {
        this.fieldbookService = fieldbookService;
    }

    
    public TrialSelection getTrialSelection() {
        return trialSelection;
    }

    
    public void setTrialSelection(TrialSelection trialSelection) {
        this.trialSelection = trialSelection;
    }

    
    
}