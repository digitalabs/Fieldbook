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
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.validation.ImportGermplasmListValidator;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping(ImportGermplasmListController.URL)
public class ImportGermplasmListController extends AbstractBaseFieldbookController{

    public static final String URL = "/NurseryManager/importGermplasmList";
    
    @Resource
    private UserSelection userSelection;

    @Resource
    private ImportGermplasmFileService importGermplasmFileService;
    
    @Override
    public String getContentName() {
        return "NurseryManager/importGermplasmList";
    }
    
    @Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}
    
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
    	return super.show(model);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String showDetails(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, BindingResult result, Model model) {
    	
    	ImportGermplasmListValidator validator = new ImportGermplasmListValidator();
    	validator.validate(form, result);
    	//result.reject("importGermplasmListForm.file", "test error msg");    	
    	
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
        			userSelection.setImportedGermplasmMainInfo(mainInfo);
        			
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
    
    @RequestMapping(value="/next", method = RequestMethod.POST)
    public String nextScreen(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, BindingResult result, Model model) {
    	
    	
        return "redirect:" + AddOrRemoveTraitsController.URL;
    	
    }

}