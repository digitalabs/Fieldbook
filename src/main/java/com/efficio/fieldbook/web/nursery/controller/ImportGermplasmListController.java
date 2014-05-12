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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.nursery.validation.ImportGermplasmListValidator;

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
    public static final String CHECK_PAGINATION_TEMPLATE = "/NurseryManager/showCheckGermplasmPagination";
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    @Resource
    private GermplasmListManager germplasmListManager;

    /** The import germplasm file service. */
    @Resource
    private ImportGermplasmFileService importGermplasmFileService;
    
    @Resource
    private ValidationService validationService;
    
    @Resource
    private DataImportService dataImportService;
    
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;
	@Resource
    private FieldbookService fieldbookMiddlewareService;
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
    
    @Resource
    private MergeCheckService mergeCheckService;
    
    /** The message source. */
    @Autowired
    public MessageSource messageSource;
    
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
            form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());

            form.changePage(1);
            userSelection.setCurrentPageGermplasmList(form.getCurrentPage());
            
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
        			//form.setCurrentPage(1);
                    form.changePage(1);
                    userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

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
    
    @ResponseBody
    @RequestMapping(value="/submitCheckGermplasmList", method = RequestMethod.POST)
    public String submitCheckGermplasmList(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form
            , BindingResult result, Model model) {
        int previewPageNum = userSelection.getCurrentPageGermplasmList();
        if(form.getPaginatedImportedCheckGermplasm() != null){
	        for(int i = 0 ; i < form.getPaginatedImportedCheckGermplasm().size() ; i++){
	            ImportedGermplasm importedGermplasm = form.getPaginatedImportedCheckGermplasm().get(i);
	            int realIndex = ((previewPageNum - 1) * form.getResultPerPage()) + i;
	            getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(realIndex).setCheck(importedGermplasm.getCheck());
	            getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(realIndex).setCheckId(Integer.parseInt(importedGermplasm.getCheck()));
	            getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(realIndex).setCheckName(importedGermplasm.getCheckName());
	        }
        }
        
        return "success";
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
    @ResponseBody
    @RequestMapping(value="/next", method = RequestMethod.POST)
    public String nextScreen(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form
            , BindingResult result, Model model) throws MiddlewareQueryException {
    	
    	form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
    	form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
    	form.setImportedCheckGermplasmMainInfo(getUserSelection().getImportedCheckGermplasmMainInfo());
    	if (getUserSelection().getImportedCheckGermplasmMainInfo() != null) {
    		form.setImportedCheckGermplasm(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
    	}
        
    	//merge primary and check germplasm list
    	if (getUserSelection().getImportedCheckGermplasmMainInfo() != null && form.getImportedCheckGermplasm() != null && form.getStartIndex() != null
    			&& form.getInterval() != null && form.getMannerOfInsertion() != null) {
	    	getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(mergeCheckService.mergeGermplasmList(form.getImportedGermplasm(), 
	    	        form.getImportedCheckGermplasm(), Integer.parseInt(form.getStartIndex()), Integer.parseInt(form.getInterval()), Integer.parseInt(form.getMannerOfInsertion())));
	    	form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
    	}
    	
    	//this would validate and add CHECK factor if necessary
        importGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(), getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), userSelection);

    	userSelection.setMeasurementRowList(measurementsGeneratorService.generateRealMeasurementRows(userSelection));
    	userSelection.getWorkbook().setObservations(userSelection.getMeasurementRowList());

    	//validationService.validateObservationValues(userSelection.getWorkbook());
        dataImportService.saveDataset(userSelection.getWorkbook(), true);
		
		return "success";
    }

    @RequestMapping(value="/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayGermplasmDetails(@PathVariable Integer listId, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            form.setImportedGermplasmMainInfo(mainInfo);
            int count = (int) germplasmListManager.countGermplasmListDataByListId(listId);
            
            List<GermplasmListData> data = new ArrayList<GermplasmListData>();
            //for(int i = 0 ; i < 20 ; i++)
            	data.addAll(germplasmListManager.getGermplasmListDataByListId(listId, 0, count));
            List<ImportedGermplasm> list = transformGermplasmListDataToImportedGermplasm(data, null);
            
            form.setImportedGermplasm(list);
            
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            //form.changePage(1);
            form.changePage(1);
            userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

            getUserSelection().setImportedGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            model.addAttribute("checkLists", ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations());
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    @RequestMapping(value="/displayCheckGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayCheckGermplasmDetails(@PathVariable Integer listId, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            form.setImportedCheckGermplasmMainInfo(mainInfo);
            int count = (int) germplasmListManager.countGermplasmListDataByListId(listId);
            
            List<Enumeration> checksList = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
            String checkId =  null;
            for(Enumeration enumVar : checksList){
            	if(enumVar.getName().equalsIgnoreCase("C")){
            		checkId = enumVar.getId().toString();
            		break;
            	}
            }
            List<GermplasmListData> data = new ArrayList<GermplasmListData>();
            //for(int i = 0 ; i < 20 ; i++)
            	data.addAll(germplasmListManager.getGermplasmListDataByListId(listId, 0, count));
            List<ImportedGermplasm> list = transformGermplasmListDataToImportedGermplasm(data, checkId);
            
            form.setImportedCheckGermplasm(list);
            
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            //form.changePage(1);
            form.changeCheckPage(1);
            userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

            getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
            model.addAttribute("checkLists", checksList);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    @ResponseBody
    @RequestMapping(value="/deleteCheckGermplasmDetails/{gid}", method = RequestMethod.GET)
    public String deleteCheckGermplasmDetails(@PathVariable Integer gid, Model model) {
        try {
            
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            if(userSelection.getImportedCheckGermplasmMainInfo() != null)
                mainInfo = userSelection.getImportedCheckGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            
            List<ImportedGermplasm> checkList = mainInfo.getImportedGermplasmList().getImportedGermplasms();
            Iterator<ImportedGermplasm> iter = checkList.iterator();
            while (iter.hasNext()) {
                if (iter.next().getGid().equals(gid)) {
                    iter.remove();
                }
            }
            
            userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(checkList);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "success";
    }
    
    @RequestMapping(value="/addCheckGermplasmDetails/{entryId}", method = RequestMethod.GET)
    public String addCheckGermplasmDetails(@PathVariable Integer entryId, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            if(userSelection.getImportedCheckGermplasmMainInfo() != null)
            	mainInfo = userSelection.getImportedCheckGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            form.setImportedCheckGermplasmMainInfo(mainInfo);
            
            List<Enumeration> checksList = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
            String checkId =  null;
            for(Enumeration enumVar : checksList){
            	if(enumVar.getName().equalsIgnoreCase("CHECK")){
            		checkId = enumVar.getId().toString();
            		break;
            	}
            }           
                        
            List<ImportedGermplasm> primaryList = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
            ImportedGermplasm importedGermplasm = null;//primaryList.get(entryId-1);
            for(ImportedGermplasm impGerm : primaryList){
            	if(impGerm.getEntryId().intValue() == entryId.intValue()){
            		importedGermplasm = impGerm;
            		break;
            	}
            		
            }
            //ImportedGermplasm importedGermplasm = primaryList.get(entryId-1);
            
            importedGermplasm.setCheck(checkId);
            
            List<ImportedGermplasm> list = new ArrayList();
            if(userSelection.getImportedCheckGermplasmMainInfo() != null && 
            		userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null && 
            		userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null)
            	list = userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
            list.add(importedGermplasm);
            
            form.setImportedCheckGermplasm(list);
            
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            
            //form.changePage(1);
            form.changeCheckPage(1);
            userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

            getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
            model.addAttribute("checkLists", checksList);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    @ResponseBody
    @RequestMapping(value="/resetCheckGermplasmDetails", method = RequestMethod.GET)
    public String resetCheckGermplasmDetails( 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            //form.setImportedCheckGermplasmMainInfo(mainInfo);
            
            List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
            
            //form.setImportedCheckGermplasm(list);
            
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            //form.changePage(1);
            //form.changeCheckPage(1);
            userSelection.setCurrentPageCheckGermplasmList(1);

            getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "success";
    }
    
    
    @RequestMapping(value="/page/{pageNum}", method = RequestMethod.GET)
    public String getPaginatedList(@PathVariable int pageNum, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
        //this set the necessary info from the session variable
    	
    	form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
    	form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
        form.changePage(pageNum);
        userSelection.setCurrentPageGermplasmList(form.getCurrentPage());
        try {
			model.addAttribute("checkLists", ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations());
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    @RequestMapping(value="/checkPage/{pageNum}/{previewPageNum}", method = RequestMethod.POST)
    public String getCheckPaginatedList(@PathVariable int pageNum, @PathVariable int previewPageNum
            , @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
        //this set the necessary info from the session variable
    	
    	//we need to set the data in the measurementList
    	for(int i = 0 ; i < form.getPaginatedImportedCheckGermplasm().size() ; i++){
    		ImportedGermplasm importedGermplasm = form.getPaginatedImportedCheckGermplasm().get(i);
    		int realIndex = ((previewPageNum - 1) * form.getResultPerPage()) + i;
    		getUserSelection().getImportedCheckGermplasmMainInfo()
            .getImportedGermplasmList().getImportedGermplasms().get(realIndex).setCheck(importedGermplasm.getCheck());
    	}
    	
    	form.setImportedCheckGermplasmMainInfo(getUserSelection().getImportedCheckGermplasmMainInfo());
    	form.setImportedCheckGermplasm(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
        form.changeCheckPage(pageNum);
        userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());
        try {
			model.addAttribute("checkLists", ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations());
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    
    private List<ImportedGermplasm> transformGermplasmListDataToImportedGermplasm(List<GermplasmListData> data, String defaultCheckId) {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        int index = 1;
        if (data != null && data.size() > 0) {
            for (GermplasmListData aData : data) {
                ImportedGermplasm germplasm = new ImportedGermplasm();
                germplasm.setCheck(defaultCheckId);
                germplasm.setCross(aData.getGroupName());
                germplasm.setDesig(aData.getDesignation());
                germplasm.setEntryCode(aData.getEntryCode());
                germplasm.setEntryId(aData.getEntryId());
                germplasm.setGid(aData.getGid().toString());
                germplasm.setSource(aData.getSeedSource());
                germplasm.setIndex(index++);
                
                list.add(germplasm);
            }
        }
        return list;
    }
    
   
    /**
     * Gets the all check types.
     *
     * @return the all check types
     */
    @ResponseBody
    @RequestMapping(value="/getAllCheckTypes", method = RequestMethod.GET)
    public Map<String, String> getAllCheckTypes() {
        Map<String, String> result = new HashMap<String, String>();
        
        try {            
            List<Enumeration> allEnumerations = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
            result.put("success", "1");
            result.put("allCheckTypes", convertObjectToJson(allEnumerations));
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("success", "-1");
        }
        
        return result;
    }
    
    @ResponseBody
    @RequestMapping(value="/addUpdateCheckType/{operation}", method = RequestMethod.POST)
    public Map<String, String> addUpdateCheckType(@PathVariable int operation, 
            @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Locale local) {
        Map<String, String> result = new HashMap<String, String>();
        
        try {
            StandardVariable stdVar = ontologyService.getStandardVariable(TermId.CHECK.getId());
            Enumeration enumeration;
            String message = null;
            if (operation == 1) {
                enumeration = new Enumeration(null, form.getManageCheckCode(), form.getManageCheckValue(), 0);
                message = messageSource.getMessage("nursery.manage.check.types.add.success", 
                        new Object[] {form.getManageCheckValue()}, local); 
            } else {
                enumeration = stdVar.getEnumeration(Integer.parseInt(form.getManageCheckCode()));
                enumeration.setDescription(form.getManageCheckValue());
                message = messageSource.getMessage("nursery.manage.check.types.edit.success", 
                        new Object[] {enumeration.getName()}, local);
            }
            ontologyService.saveOrUpdateStandardVariableEnumeration(stdVar, enumeration);
            
            List<Enumeration> allEnumerations = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
            result.put("checkTypes", convertObjectToJson(allEnumerations));
            
            result.put("success", "1");
            result.put("successMessage", message);
        } catch (MiddlewareQueryException e) {
            LOG.debug(e.getMessage(), e);
            result.put("success", "-1");
            result.put("error", e.getMessage());
        } catch (MiddlewareException e) {
            LOG.debug(e.getMessage(), e);
            result.put("success", "-1");
        }
        
        return result;
    }
    
    @ResponseBody
    @RequestMapping(value="/deleteCheckType", method = RequestMethod.POST)
    public Map<String, String> deleteCheckType(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Locale local) {
        Map<String, String> result = new HashMap<String, String>();

        try {
            String name = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumeration(Integer.parseInt(form.getManageCheckCode())).getName();
            
            if (!ontologyService.validateDeleteStandardVariableEnumeration(TermId.CHECK.getId(), Integer.parseInt(form.getManageCheckCode()))) {
                result.put("success", "-1");
                result.put("error", messageSource.getMessage("nursery.manage.check.types.delete.error", 
                        new Object[] {name}, local));
            } else if (Integer.parseInt(form.getManageCheckCode()) > 0) {
                result.put("success", "-1");
                result.put("error", messageSource.getMessage("nursery.manage.check.types.delete.central", 
                        new Object[] {name}, local));
            } else {
                ontologyService.deleteStandardVariableValidValue(TermId.CHECK.getId(), Integer.parseInt(form.getManageCheckCode()));
                result.put("success", "1");
                result.put("successMessage", messageSource.getMessage("nursery.manage.check.types.delete.success", 
                        new Object[] {name}, local));
                List<Enumeration> allEnumerations = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
                result.put("checkTypes", convertObjectToJson(allEnumerations));
            }
            
        } catch (MiddlewareQueryException e) {
            LOG.debug(e.getMessage(), e);
            result.put("success", "-1");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Gets the check type list.
     *
     * @return the check type list
     */
    @ModelAttribute("checkTypes")
    public List<Enumeration> getCheckTypes() {
        try {
            return ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
