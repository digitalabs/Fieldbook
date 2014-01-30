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

import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.validation.ImportGermplasmListValidator;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * This controller handles the 2nd step in the nursery manager process.
 * 
 * @author Daniel Jao
 */
@Controller
@RequestMapping(ImportGermplasmListController.URL)
public class ImportGermplasmListController extends AbstractBaseFieldbookController{
    
    private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmListController.class);
    
    /** The Constant URL. */
    public static final String URL = "/NurseryManager/importGermplasmList";
    public static final String PAGINATION_TEMPLATE = "/NurseryManager/showGermplasmPagination";
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;

    /** The import germplasm file service. */
    @Resource
    private ImportGermplasmFileService importGermplasmFileService;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
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
    public String show(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form
            , Model model) {
        //this set the necessary info from the session variable
        form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
        if(getUserSelection().getImportedGermplasmMainInfo() != null 
                && getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null){
            //this would be use to display the imported germplasm info
            form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo()
                    .getImportedGermplasmList().getImportedGermplasms());
            form.setCurrentPage(1);
        }
    	return super.show(model);
    }
    
    /**
     * Get for the pagination of the list
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/page/{pageNum}", method = RequestMethod.GET)
    public String getPaginatedList(@PathVariable int pageNum
            , @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
        //this set the necessary info from the session variable
        form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
        if(getUserSelection().getImportedGermplasmMainInfo() != null 
                && getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null){
            //this would be use to display the imported germplasm info
            form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo()
                    .getImportedGermplasmList().getImportedGermplasms());
            form.setCurrentPage(pageNum);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
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
    public String showDetails(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form
            , BindingResult result, Model model) {
    	
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
        		ImportedGermplasmMainInfo mainInfo =importGermplasmFileService
        		        .storeImportGermplasmWorkbook(form.getFile());
        		mainInfo = importGermplasmFileService.processWorkbook(mainInfo);
        		
        		if(mainInfo.getFileIsValid()){
        			form.setHasError("0");
        			getUserSelection().setImportedGermplasmMainInfo(mainInfo);
        			getUserSelection().setImportValid(true);
        			form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
        			form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo()
        			        .getImportedGermplasmList().getImportedGermplasms());
        			form.setCurrentPage(1);
        			//after this one, it goes back to the same screen, 
        			// but the list should already be displayed
        		}else{
        			//meaning there is error
        			form.setHasError("1");
        			for(String errorMsg : mainInfo.getErrorMessages()){
        				result.rejectValue("file", errorMsg);  
        			}
        			
        		}
        	}catch(Exception e){
                LOG.error(e.getMessage(), e);
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
     * @throws MiddlewareQueryException 
     */
    @RequestMapping(value="/next", method = RequestMethod.POST)
    public String nextScreen(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form
            , BindingResult result, Model model) throws MiddlewareQueryException {
    	
    	if(getUserSelection().isImportValid()){
    		/*
    		//we set the check value here
    		StringTokenizer tokenizer = new StringTokenizer(form.getCheckValues(), ",");
    		int index = 0;
    		while(tokenizer.hasMoreTokens()){
    			String checkVal = "1".equalsIgnoreCase(tokenizer.nextToken()) ? "is check" : "";
    			ImportedGermplasm germplasm = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(index++);
    			germplasm.setCheck(checkVal);
    		}
    		*/
    		boolean hasCheck = false;
    		List<ImportedGermplasm> sessionImportedGermplasmList = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
    		for(int i = 0 ; i < form.getImportedGermplasm().size() ; i++){
    			ImportedGermplasm germplasm = form.getImportedGermplasm().get(i);
    			String checkVal = "";
    			if(germplasm.getCheck() != null){
    				checkVal = germplasm.getCheck();
    				hasCheck = true;
    			}
    			sessionImportedGermplasmList.get(i).setCheck(checkVal);
    		}
    		
    		if(hasCheck){
    			//we need to add the CHECK factor if its not existing
    			List<MeasurementVariable> measurementVariables = userSelection.getWorkbook().getFactors();
    			MeasurementVariable checkVariable = new MeasurementVariable("CHECK", "TYPE OF ENTRY", "CODE", "ASSIGNED", "CHECK", "C", "", "ENTRY");
    			Integer checkVariableTermId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(checkVariable.getProperty(), checkVariable.getScale(), checkVariable.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(checkVariable.getLabel()));
    			boolean checkFactorExisting = false;
    			for(MeasurementVariable var : measurementVariables){
    				Integer termId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(), var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
    				if(termId != null && checkVariableTermId != null && termId.intValue() == checkVariableTermId.intValue()){
    					checkFactorExisting = true;
    					break;
    				}
    			}
    			if(checkFactorExisting == false){
					userSelection.getWorkbook().reset();
					userSelection.getWorkbook().setCheckFactorAddedOnly(true);
    				userSelection.getWorkbook().getFactors().add(checkVariable);
    			}
    		}else{
    			//we remove since it was dynamically added only
    			if(userSelection.getWorkbook().isCheckFactorAddedOnly() == true){
    				//we need to remove it
    				userSelection.getWorkbook().reset();
    				List<MeasurementVariable> factors = userSelection.getWorkbook().getFactors();
    				factors.remove(factors.size() - 1);
    				userSelection.getWorkbook().setFactors(factors);
    			}
    		}
    		//getUserSelection().setImportedGermplasmMainInfo(form)
    		
    	    return "redirect:" + AddOrRemoveTraitsController.URL;
    	}
    	else{
    	    form.setHasError("1");
    	    result.reject("error.no.import.germplasm.list", "Please import germplasm");
    	    return show(form,model);
    	}
    	
    }

}
