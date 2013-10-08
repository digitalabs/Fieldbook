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

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.validation.ImportGermplasmListValidator;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

// TODO: Auto-generated Javadoc
/**
 * The Class ImportGermplasmListController.
 */
@Controller
@RequestMapping(ImportGermplasmListController.URL)
public class ImportGermplasmListController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/importGermplasmList";
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;

    /** The import germplasm file service. */
    @Resource
    private ImportGermplasmFileService importGermplasmFileService;
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/importGermplasmList";
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getUserSelection()
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }
    
    /**
     * Show the main import page
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
        //this set the necessary info from the session variable
        form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
        if(getUserSelection().getImportedGermplasmMainInfo() != null && getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null){
            //this would be use to display the imported germplasm info
            form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
        }
    	return super.show(model);
    }

    /**
     * Process the imported file and just show the information again
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String showDetails(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, BindingResult result, Model model) {
    	
    	ImportGermplasmListValidator validator = new ImportGermplasmListValidator();
    	validator.validate(form, result);
    	//result.reject("importGermplasmListForm.file", "test error msg");    	
    	getUserSelection().setImportValid(false);
        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
        	form.setHasError("1");
            return show(form,model);
        }else{
        	try{
        		ImportedGermplasmMainInfo mainInfo =importGermplasmFileService.storeImportGermplasmWorkbook(form.getFile());
        		mainInfo = importGermplasmFileService.processWorkbook(mainInfo);
        		
        		if(mainInfo.getFileIsValid()){
        			form.setHasError("0");
        			getUserSelection().setImportedGermplasmMainInfo(mainInfo);
        			getUserSelection().setImportValid(true);
        			form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
        			form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
        			//after this one, it goes back to the same screen, but the list should already be displayed
        		}else{
        			//meaing there is error
        			form.setHasError("1");
        			for(String errorMsg : mainInfo.getErrorMessages()){
        				result.rejectValue("file", errorMsg);  
        			}
        			
        		}
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        	
        	
        }
        return show(form,model);
    	
    }
    
    /**
     * Goes to the Next screen.  Added validation if a germplasm list was properly uploaded
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/next", method = RequestMethod.POST)
    public String nextScreen(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, BindingResult result, Model model) {
    	
    	if(getUserSelection().isImportValid())
    	    return "redirect:" + AddOrRemoveTraitsController.URL;
    	else{
    	    form.setHasError("1");
    	    result.reject("error.no.import.germplasm.list", "Please import germplasm");
    	    return show(form,model);
    	}
    	
    }

}