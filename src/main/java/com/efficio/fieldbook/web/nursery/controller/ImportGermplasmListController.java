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
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.form.UpdateGermplasmCheckForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

// TODO: Auto-generated Javadoc
/**
 * This controller handles the 2nd step in the nursery manager process.
 * 
 * @author Daniel Jao
 */
@Controller
@RequestMapping({ImportGermplasmListController.URL, ImportGermplasmListController.URL_2, ImportGermplasmListController.URL_3 , ImportGermplasmListController.URL_4})
public class ImportGermplasmListController extends AbstractBaseFieldbookController{
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmListController.class);
    
    /** The Constant URL. */
    public static final String URL = "/NurseryManager/importGermplasmList";
    public static final String URL_2 = "/NurseryManager/GermplasmList";
    public static final String URL_3 = "/TrialManager/GermplasmList";
    public static final String URL_4 = "/ListManager/GermplasmList";
    
    /** The Constant PAGINATION_TEMPLATE. */
    public static final String PAGINATION_TEMPLATE = "/NurseryManager/showGermplasmPagination";
    public static final String EDIT_CHECK = "/Common/editCheck";
    
    /** The Constant CHECK_PAGINATION_TEMPLATE. */
    public static final String CHECK_PAGINATION_TEMPLATE = "/NurseryManager/showCheckGermplasmPagination";
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    /** The germplasm list manager. */
    @Resource
    private GermplasmListManager germplasmListManager;

    /** The import germplasm file service. */
    @Resource
    private ImportGermplasmFileService importGermplasmFileService;
    
    /** The validation service. */
    @Resource
    private ValidationService validationService;
    
    /** The data import service. */
    @Resource
    private DataImportService dataImportService;
    
    /** The measurements generator service. */
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;
	
	/** The fieldbook middleware service. */
	@Resource
    private FieldbookService fieldbookMiddlewareService;
	@Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
    
    /** The merge check service. */
    @Resource
    private MergeCheckService mergeCheckService;
    
    /** The message source. */
    @Autowired
    public MessageSource messageSource;
    
    private static String DEFAULT_CHECK_VALUE = "C";
    private static String DEFAULT_TEST_VALUE = "T";
    
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
    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }
    
    /**
     * Show the main import page.
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
     * Goes to the Next screen.  Added validation if a germplasm list was properly uploaded
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(value={"/next", "/submitAll"}, method = RequestMethod.POST)
    public String nextScreen(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form
            , BindingResult result, Model model, HttpServletRequest req) throws MiddlewareQueryException {
    		//start: section for taking note of the check germplasm
        boolean isDeleteObservations = false;
		String selectedCheck[] = form.getSelectedCheck();
		boolean isNursery = userSelection.getWorkbook().getStudyDetails().getStudyType() == StudyType.N ? true : false;
	    if (userSelection.getTemporaryWorkbook() != null) {
            WorkbookUtil.manageExpDesignVariablesAndObs(userSelection.getWorkbook(), userSelection.getTemporaryWorkbook());
            WorkbookUtil.addMeasurementDataToRowsExp(userSelection.getWorkbook().getFactors(), userSelection.getWorkbook().getObservations(), 
                    false, userSelection, ontologyService, fieldbookService);
            WorkbookUtil.addMeasurementDataToRowsExp(userSelection.getWorkbook().getVariates(), userSelection.getWorkbook().getObservations(), 
                    true, userSelection, ontologyService, fieldbookService);
            HashMap<Integer, MeasurementVariable> observationVariables = WorkbookUtil.createVariableList(userSelection.getWorkbook().getFactors(), userSelection.getWorkbook().getVariates());
            WorkbookUtil.deleteDeletedVariablesInObservations(observationVariables, userSelection.getWorkbook().getObservations());
            userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
            userSelection.setTemporaryWorkbook(null);
            isDeleteObservations = true;
        
        } else if (isNursery){
            if (selectedCheck != null && selectedCheck.length != 0) {
            	
            	ImportedGermplasmMainInfo importedGermplasmMainInfoToUse = getUserSelection().getImportedCheckGermplasmMainInfo();
            	if(importedGermplasmMainInfoToUse == null){
            		//since for trial, we are using only the original info
            		importedGermplasmMainInfoToUse = getUserSelection().getImportedGermplasmMainInfo();
            	}
            	if(importedGermplasmMainInfoToUse != null){
    	            for (int i = 0; i < selectedCheck.length; i++) {
    	                int realIndex = i;		                
    	                if (NumberUtils.isNumber(selectedCheck[i])) {		                	
    	                	importedGermplasmMainInfoToUse.getImportedGermplasmList().getImportedGermplasms().get(realIndex).setCheck(selectedCheck[i]);
    	                	importedGermplasmMainInfoToUse.getImportedGermplasmList().getImportedGermplasms().get(realIndex).setCheckId(Integer.parseInt(selectedCheck[i]));		                	
    	                }
    	            }
            	}
            }else{
            	//we set the check to null
            	if(getUserSelection().getImportedGermplasmMainInfo() != null && 
            			getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null
            			&& getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null){
            		//this is to keep track of the original list before merging with the checks
            		for(ImportedGermplasm germplasm : getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()){
            			germplasm.setCheckId(null);
            			germplasm.setCheck("");
            		}
            	}
            }
            
            //end: section for taking note of the check germplasm
            if (getUserSelection().getImportedGermplasmMainInfo() != null) {
            	form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
            	form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
            	form.setImportedCheckGermplasmMainInfo(getUserSelection().getImportedCheckGermplasmMainInfo());
            	if (getUserSelection().getImportedCheckGermplasmMainInfo() != null) {
            		form.setImportedCheckGermplasm(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
            	}
            	if(getUserSelection().getImportedGermplasmMainInfo() != null && 
            			getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null){
            		//this is to keep track of the original list before merging with the checks
            		getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().copyImportedGermplasms();
            	}
            	//merge primary and check germplasm list
            	if (getUserSelection().getImportedCheckGermplasmMainInfo() != null && form.getImportedCheckGermplasm() != null && form.getStartIndex() != null
            			&& form.getInterval() != null && form.getMannerOfInsertion() != null) {
            		String lastDragCheckList = form.getLastDraggedChecksList();        		
            		if("0".equalsIgnoreCase(lastDragCheckList)){
            			//we do the cleaning here	
            			List<ImportedGermplasm> newNurseryGermplasm = cleanGermplasmList(form.getImportedGermplasm(), 
            	    	        form.getImportedCheckGermplasm());
            			form.setImportedGermplasm(newNurseryGermplasm);
            		}
            		int interval = 0;
            		if(form.getInterval() != null && !form.getInterval().equalsIgnoreCase("")){
            			interval = Integer.parseInt(form.getInterval());
            		}
            		String defaultTestCheckId = getCheckId(DEFAULT_TEST_VALUE, fieldbookService.getCheckList());
            		
            		List<ImportedGermplasm> newImportedGermplasm = mergeCheckService.mergeGermplasmList(form.getImportedGermplasm(), 
        	    	        form.getImportedCheckGermplasm(), 
        	    	        Integer.parseInt(form.getStartIndex()), 
        	    	        interval, 
        	    	        Integer.parseInt(form.getMannerOfInsertion()), defaultTestCheckId);
            		
        	    	getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(newImportedGermplasm);
        	    	form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
            	}
        	
            	//this would validate and add CHECK factor if necessary
                importGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(), getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), userSelection);
                userSelection.setMeasurementRowList(measurementsGeneratorService.generateRealMeasurementRows(userSelection));
            }
        } else {
            isDeleteObservations = true;
            userSelection.setMeasurementRowList(null);
        }
                    
        userSelection.getWorkbook().setObservations(userSelection.getMeasurementRowList());
        
        fieldbookService.createIdCodeNameVariablePairs(userSelection.getWorkbook(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
        fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<SettingDetail>(), AppConstants.ID_NAME_COMBINATION.getString(), true);        
        int studyId = dataImportService.saveDataset(userSelection.getWorkbook(), true, isDeleteObservations);        
        //for saving the list data project        
        saveListDataProject(isNursery, studyId);
        return Integer.toString(studyId);
    }       
    
    private void saveListDataProject(boolean isNursery, int studyId) throws NumberFormatException, MiddlewareQueryException{
    	//we call here to have
    	
        if(getUserSelection().getImportedGermplasmMainInfo() != null && getUserSelection().getImportedGermplasmMainInfo().getListId() != null){
        	//we save the list
        	//we need to create a new germplasm list
        	Integer listId = getUserSelection().getImportedGermplasmMainInfo().getListId();
        	List<ImportedGermplasm> importedGermplasmList = isNursery ? getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getOriginalImportedGermplasms() : getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
        	List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(importedGermplasmList);
        	fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, isNursery ? GermplasmListType.NURSERY : GermplasmListType.TRIAL, listId, listDataProject, getCurrentIbdbUserId());
        }else{
        	//we delete the record in the db
        	fieldbookMiddlewareService.deleteListDataProjects(studyId, isNursery ? GermplasmListType.NURSERY : GermplasmListType.TRIAL);
        }
        if(getUserSelection().getImportedCheckGermplasmMainInfo() != null){
        	if(getUserSelection().getImportedCheckGermplasmMainInfo().getListId() != null){
        		//came from a list
        		Integer listId = getUserSelection().getImportedCheckGermplasmMainInfo().getListId();
        		List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
        		fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, GermplasmListType.CHECK, listId, listDataProject,getCurrentIbdbUserId());
        		
        	}else if(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null &&
        			getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null
        			&& !getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().isEmpty()){
                List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
                fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, GermplasmListType.CHECK, null, listDataProject, getCurrentIbdbUserId());
                
        	}else{
            	//we delete it
            	fieldbookMiddlewareService.deleteListDataProjects(studyId, GermplasmListType.CHECK);
            }
        }else{
        	if(isNursery){
        		//we delete it
        		fieldbookMiddlewareService.deleteListDataProjects(studyId, GermplasmListType.CHECK);
        	}
        }
    }   
    
    private List<ImportedGermplasm> cleanGermplasmList(List<ImportedGermplasm> primaryList, 
			List<ImportedGermplasm> checkList){
    	
    	if(checkList == null || checkList.size() == 0 ) {
    		return primaryList;
    	}
    	
    	List<ImportedGermplasm> newPrimaryList = new ArrayList<ImportedGermplasm>();
    	Map<Integer, ImportedGermplasm> checkGermplasmMap = new HashMap<Integer, ImportedGermplasm>();
    	for (ImportedGermplasm checkGermplasm : checkList) {
    		checkGermplasmMap.put(checkGermplasm.getIndex(), checkGermplasm);	
    	}
    	
    	for (ImportedGermplasm primaryGermplasm : primaryList) {
    		if(checkGermplasmMap.get(primaryGermplasm.getIndex()) == null) {
    			newPrimaryList.add(primaryGermplasm);
    		}
    	}
    	return newPrimaryList;
    }
    /**
     * Display germplasm details.
     *
     * @param listId the list id
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/displayGermplasmDetails/{listId}/{type}", method = RequestMethod.GET)
    public String displayGermplasmDetails(@PathVariable Integer listId, @PathVariable String type, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            form.setImportedGermplasmMainInfo(mainInfo);
            int count = (int) germplasmListManager.countGermplasmListDataByListId(listId);
            mainInfo.setListId(listId);
            List<GermplasmListData> data = new ArrayList<GermplasmListData>();
            data.addAll(germplasmListManager.getGermplasmListDataByListId(listId, 0, count));
            List<ImportedGermplasm> list = transformGermplasmListDataToImportedGermplasm(data, null);
            String defaultTestCheckId = getCheckId(DEFAULT_TEST_VALUE, fieldbookService.getCheckList());
            form.setImportedGermplasm(list);
            List<Map<String, Object>> dataTableDataList = new ArrayList<Map<String, Object>>();
            List<Enumeration> checkList = fieldbookService.getCheckList();
            boolean isNursery = false;
            if(type != null && type.equalsIgnoreCase(StudyType.N.getName())){
            	isNursery = true;
        	}else if(type != null && type.equalsIgnoreCase(StudyType.T.getName())){
        		isNursery = false;
        	}
        	for(ImportedGermplasm germplasm : list){
            	Map<String, Object> dataMap = new HashMap<String, Object>();       
            	
            	dataMap.put("position", germplasm.getIndex().toString());
				dataMap.put("checkOptions", checkList);
				dataMap.put("entry", germplasm.getEntryId().toString());
				dataMap.put("desig", germplasm.getDesig().toString());
				dataMap.put("gid", germplasm.getGid().toString());
				
				
				if(!isNursery){
					germplasm.setCheck(defaultTestCheckId);
					germplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
					dataMap.put("check", defaultTestCheckId);
					
					List<SettingDetail> factorsList = userSelection.getPlotsLevelList();
					if(factorsList != null){
		    			//we iterate the map for dynamic header of trial
		    			for(int counter = 0 ; counter < factorsList.size() ; counter++){
		    				SettingDetail factorDetail= factorsList.get(counter);
		    				if(factorDetail != null && factorDetail.getVariable() != null){		    					
		    					dataMap.put(factorDetail.getVariable().getCvTermId()+AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(), getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
		    				}
		    			}
		    		}
				}else{				
					dataMap.put("cross", germplasm.getCross().toString());
					dataMap.put("source", germplasm.getSource().toString());
					dataMap.put("entryCode", germplasm.getEntryCode().toString());
					dataMap.put("check", "");
				}
				
        		dataTableDataList.add(dataMap);
            }
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            form.changePage(1);
            userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

            getUserSelection().setImportedGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
            model.addAttribute("checkLists", fieldbookService.getCheckList());
            model.addAttribute("listDataTable", dataTableDataList);
            model.addAttribute("type", type);
            model.addAttribute("tableHeaderList", getGermplasmTableHeader(type, userSelection.getPlotsLevelList()));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    @RequestMapping(value="/displaySelectedGermplasmDetails/{type}", method = RequestMethod.GET)
    public String displaySelectedGermplasmDetails(@PathVariable String type, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            
            mainInfo.setAdvanceImportType(true);
            form.setImportedGermplasmMainInfo(mainInfo);
            
            int studyId = userSelection.getWorkbook().getStudyDetails().getId();
            List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
            
            boolean isNursery = false;
            GermplasmListType germplasmListType = null;
            if(type != null && type.equalsIgnoreCase(StudyType.N.getName())){
                isNursery = true;
                germplasmListType = GermplasmListType.NURSERY;
            }else if(type != null && type.equalsIgnoreCase(StudyType.T.getName())){
                isNursery = false;
                germplasmListType = GermplasmListType.TRIAL;
            }
            
            List<GermplasmList> germplasmLists = fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(studyId), germplasmListType);
            
            if(germplasmLists != null && !germplasmLists.isEmpty()){
                GermplasmList germplasmList = germplasmLists.get(0);
                
                if (germplasmList != null && germplasmList.getListRef() != null) {
                    form.setLastDraggedPrimaryList(germplasmList.getListRef().toString());
                    mainInfo.setListId(germplasmList.getId());
                }
                List<ListDataProject> data = fieldbookMiddlewareService.getListDataProject(germplasmList.getId());
                list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
            }
            
            String defaultTestCheckId = getCheckId(DEFAULT_TEST_VALUE, fieldbookService.getCheckList());
            form.setImportedGermplasm(list);
            List<Map<String, Object>> dataTableDataList = new ArrayList<Map<String, Object>>();
            List<Enumeration> checkList = fieldbookService.getCheckList();
            
            for(ImportedGermplasm germplasm : list){
                Map<String, Object> dataMap = new HashMap<String, Object>();       
                
                dataMap.put("position", germplasm.getIndex().toString());
                dataMap.put("checkOptions", checkList);
                dataMap.put("entry", germplasm.getEntryId().toString());
                dataMap.put("desig", germplasm.getDesig().toString());
                dataMap.put("gid", germplasm.getGid().toString());
                
                
                if(!isNursery){
                    if (germplasm.getCheck() == null || germplasm.getCheck().equals("0")) {
                        germplasm.setCheck(defaultTestCheckId);
                        germplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
                        dataMap.put("check", defaultTestCheckId);
                    } else {
                        dataMap.put("check", germplasm.getCheckId());
                    }
                    
                    List<SettingDetail> factorsList = userSelection.getPlotsLevelList();
                    if(factorsList != null){
                        //we iterate the map for dynamic header of trial
                        for(int counter = 0 ; counter < factorsList.size() ; counter++){
                            SettingDetail factorDetail= factorsList.get(counter);
                            if(factorDetail != null && factorDetail.getVariable() != null){                             
                                dataMap.put(factorDetail.getVariable().getCvTermId()+AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(), getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
                            }
                        }
                    }
                }else{              
                    dataMap.put("cross", germplasm.getCross().toString());
                    dataMap.put("source", germplasm.getSource().toString());
                    dataMap.put("entryCode", germplasm.getEntryCode().toString());
                    dataMap.put("check", "");
                }
                
                dataTableDataList.add(dataMap);
            }
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            form.changePage(1);
            userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

            getUserSelection().setImportedGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
            model.addAttribute("checkLists", fieldbookService.getCheckList());
            model.addAttribute("listDataTable", dataTableDataList);
            model.addAttribute("type", type);
            model.addAttribute("tableHeaderList", getGermplasmTableHeader(type, userSelection.getPlotsLevelList()));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    @RequestMapping(value="/displaySelectedCheckGermplasmDetails", method = RequestMethod.GET)
    public String displaySelectedCheckGermplasmDetails(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            form.setImportedCheckGermplasmMainInfo(mainInfo);
            
            List<Enumeration> checksList = fieldbookService.getCheckList();
            
            int studyId = userSelection.getWorkbook().getStudyDetails().getId();
            List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
            
            List<GermplasmList> germplasmListCheck = fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(studyId), GermplasmListType.CHECK);
            
            if(germplasmListCheck != null && !germplasmListCheck.isEmpty()){
                GermplasmList checkList = germplasmListCheck.get(0);
                if (checkList != null & checkList.getListRef() != null && !checkList.getListRef().equals(0)) {
                    form.setKeyForOverwrite(checkList.getListRef());
                    form.setLastCheckSourcePrimary(0);
                    form.setLastDraggedChecksList(checkList.getListRef().toString());
                } else {
                    form.setLastCheckSourcePrimary(1);
                }
                
                List<ListDataProject> data = fieldbookMiddlewareService.getListDataProject(checkList.getId());
                list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
            }
                        
            generateCheckListModel(model, list, checksList);    
            
            form.setImportedCheckGermplasm(list);
            
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            form.changeCheckPage(1);
            userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

            getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    private List<TableHeader> getGermplasmTableHeader(String type, List<SettingDetail> factorsList){
    	Locale locale = LocaleContextHolder.getLocale();
    	List<TableHeader> tableHeaderList = new ArrayList<TableHeader>();
    	if(type != null && type.equalsIgnoreCase(StudyType.N.getName())){
 
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.position", null, locale), "position"));
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.entry", null, locale), "entry"));
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.designation", null, locale), "desig"));
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.gid", null, locale), "gid"));
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.cross", null, locale), "cross"));
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.source", null, locale), "source"));
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.entrycode", null, locale), "entryCode"));
    		
    	}else if(type != null && type.equalsIgnoreCase(StudyType.T.getName())){
    		if(factorsList != null){
    			//we iterate the map for dynamic header of trial
    			for(int counter = 0 ; counter < factorsList.size() ; counter++){
    				SettingDetail factorDetail= factorsList.get(counter);
    				if(factorDetail != null && factorDetail.getVariable() != null &&
    						!SettingsUtil.inHideVariableFields(factorDetail.getVariable().getCvTermId(), AppConstants.HIDE_GERMPLASM_DESCRIPTOR_HEADER_TABLE.getString())){		    					
    					tableHeaderList.add(new TableHeader(factorDetail.getVariable().getName(), factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString()));
    				}
					    				
    			}
    		}
    	}
    	return tableHeaderList;
    }
    private String getGermplasmData(String termId, ImportedGermplasm germplasm){
    	String val = "";
    	if(termId != null && NumberUtils.isNumber(termId)){
    		Integer term = Integer.valueOf(termId);
    		if(term.intValue() == TermId.GID.getId()){
    			val =  germplasm.getGid().toString(); 
    		}else if(term.intValue() == TermId.ENTRY_CODE.getId()){
    			val = germplasm.getEntryCode().toString();
    		}else if(term.intValue() == TermId.ENTRY_NO.getId()){
    			 val = germplasm.getEntryId().toString();
    		}else if(term.intValue() == TermId.SOURCE.getId() || term.intValue() == TermId.GERMPLASM_SOURCE.getId()){
    			val = germplasm.getSource().toString();
    		}else if(term.intValue() == TermId.CROSS.getId()){
    			val = germplasm.getCross().toString();
    		}else if(term.intValue() == TermId.DESIG.getId()){
    			val = germplasm.getDesig().toString(); 
    		}else if(term.intValue() == TermId.CHECK.getId()){
    			val = germplasm.getCheck().toString(); 
    		}     		    			    		
    	}
    	return val;
    }
    @RequestMapping(value="/refreshListDetails", method = RequestMethod.GET)
    public String refereshListDetails( Model model, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form) {
        
        try {
        	String type = "T";
            List<Map<String, Object>> dataTableDataList = new ArrayList<Map<String, Object>>();
            List<Enumeration> checkList = fieldbookService.getCheckList();
            List<ImportedGermplasm> list =  getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
            
            //we need to take note of the check here
            
        	for(ImportedGermplasm germplasm : list){
            	Map<String, Object> dataMap = new HashMap<String, Object>();            	
				dataMap.put("position", germplasm.getIndex().toString());
				dataMap.put("checkOptions", checkList);
				dataMap.put("entry", germplasm.getEntryId().toString());
				dataMap.put("desig", germplasm.getDesig().toString());
				dataMap.put("gid", germplasm.getGid().toString());
				List<SettingDetail> factorsList = userSelection.getPlotsLevelList();
				if(factorsList != null){
	    			//we iterate the map for dynamic header of trial
	    			for(int counter = 0 ; counter < factorsList.size() ; counter++){
	    				SettingDetail factorDetail= factorsList.get(counter);
	    				if(factorDetail != null && factorDetail.getVariable() != null){		    					
	    					dataMap.put(factorDetail.getVariable().getCvTermId()+AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(), getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
	    				}
	    			}
	    		}							
				dataMap.put("check", germplasm.getCheck() != null ? germplasm.getCheck().toString() : "");
				
        		dataTableDataList.add(dataMap);
            }

            model.addAttribute("checkLists", fieldbookService.getCheckList());
            model.addAttribute("listDataTable", dataTableDataList);
            model.addAttribute("type", type);
            model.addAttribute("tableHeaderList", getGermplasmTableHeader(type, userSelection.getPlotsLevelList()));
            
            
            form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());           
            form.setImportedGermplasm(list);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    private String getCheckId(String checkCode,  List<Enumeration> checksList) throws MiddlewareQueryException{
         String checkId =  "";
         
         for(Enumeration enumVar : checksList){
         	if(enumVar.getName().equalsIgnoreCase(checkCode)){
         		checkId = enumVar.getId().toString();
         		break;
         	}
         }
         return checkId;
    }
    /**
     * Display check germplasm details.
     *
     * @param listId the list id
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/displayCheckGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayCheckGermplasmDetails(@PathVariable Integer listId, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            form.setImportedCheckGermplasmMainInfo(mainInfo);
            int count = (int) germplasmListManager.countGermplasmListDataByListId(listId);
            mainInfo.setListId(listId);
            
            List<Enumeration> checksList = fieldbookService.getCheckList();
            String checkId =  getCheckId(DEFAULT_CHECK_VALUE, checksList);
            
            List<GermplasmListData> data = new ArrayList<GermplasmListData>();
            data.addAll(germplasmListManager.getGermplasmListDataByListId(listId, 0, count));
            List<ImportedGermplasm> list = transformGermplasmListDataToImportedGermplasm(data, checkId);
                        
        	generateCheckListModel(model, list, checksList);    
        	
            form.setImportedCheckGermplasm(list);
            
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            form.changeCheckPage(1);
            userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

            getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    /**
     * Display check germplasm details.
     *
     * @param listId the list id
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/reload/check/list/{type}", method = RequestMethod.GET)
    public String reloadCheckList(@PathVariable String type, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, 
            Model model) {
    	boolean isNursery = false;
        if(type != null && type.equalsIgnoreCase(StudyType.N.getName())){
        	isNursery = true;
    	}else if(type != null && type.equalsIgnoreCase(StudyType.T.getName())){
    		isNursery = false;
    	}
        try {
            
            List<Enumeration> checksList = fieldbookService.getCheckList();
            List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
            if(isNursery && userSelection.getImportedCheckGermplasmMainInfo() != null && 
            		userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null && 
            		userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null){
            	//we set it here
            	list = userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
            	form.setImportedCheckGermplasm(list);
            }
            generateCheckListModel(model, list, checksList);                       
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    private void generateCheckListModel(Model model,  List<ImportedGermplasm> list, List<Enumeration> checksList){
    	List<Map<String, Object>> dataTableDataList = new ArrayList<Map<String, Object>>();
    	if(list != null){
	    	for(ImportedGermplasm germplasm : list){
	        	Map<String, Object> dataMap = new HashMap<String, Object>();            	
				dataMap.put("desig", germplasm.getDesig().toString());
				dataMap.put("gid", germplasm.getGid().toString());
				dataMap.put("check", germplasm.getCheck().toString());				
				dataMap.put("entry", germplasm.getEntryId());
				dataMap.put("index", germplasm.getIndex());			
				dataMap.put("checkOptions", checksList);
	    		dataTableDataList.add(dataMap);
	        }        	           
    	}
        model.addAttribute("checkLists", checksList);
        model.addAttribute("checkListDataTable", dataTableDataList);
    	
    }
    /**
     * Delete check germplasm details.
     *
     * @param gid the gid
     * @param model the model
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="/deleteCheckGermplasmDetails/{gid}", method = RequestMethod.GET)
    public String deleteCheckGermplasmDetails(@PathVariable Integer gid, Model model) {
        try {
            
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            if(userSelection.getImportedCheckGermplasmMainInfo() != null) {
                mainInfo = userSelection.getImportedCheckGermplasmMainInfo();
            }
            mainInfo.setAdvanceImportType(true);
            
            List<ImportedGermplasm> checkList = mainInfo.getImportedGermplasmList().getImportedGermplasms();
            Iterator<ImportedGermplasm> iter = checkList.iterator();
            while (iter.hasNext()) {
                if (iter.next().getGid().equalsIgnoreCase(gid.toString())) {
                    iter.remove();
                    break;
                }
            }
            
            userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(checkList);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "success";
    }
    
    /**
     * Adds the check germplasm details.
     *
     * @param entryId the entry id
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/addCheckGermplasmDetails/{entryId}", method = RequestMethod.GET)
    public String addCheckGermplasmDetails(@PathVariable Integer entryId, 
    		@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form,
    		@RequestParam("selectedCheckVal") String selectedCheckVal,
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            if(userSelection.getImportedCheckGermplasmMainInfo() != null) {
            	mainInfo = userSelection.getImportedCheckGermplasmMainInfo();
            }
            mainInfo.setAdvanceImportType(true);
            form.setImportedCheckGermplasmMainInfo(mainInfo);
            
            List<Enumeration> checksList = fieldbookService.getCheckList();
            String checkId = getCheckId(DEFAULT_CHECK_VALUE, checksList);               
                     
            List<ImportedGermplasm> primaryList = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
            ImportedGermplasm importedGermplasm = null;
            for(ImportedGermplasm impGerm : primaryList){
            	if(impGerm.getEntryId().intValue() == entryId.intValue()){
            		importedGermplasm = impGerm.copy();
            		break;
            	}
            		
            }
            
            importedGermplasm.setCheck(checkId);
            
            List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
            if(userSelection.getImportedCheckGermplasmMainInfo() != null && 
            		userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null && 
            		userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null){
            	//we set it here
            	list = userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
            	
            }
            list.add(importedGermplasm);
            
            form.setImportedCheckGermplasm(list);
            
           
        	generateCheckListModel(model, list, checksList);    

            
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);

            form.changeCheckPage(1);
            userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

            getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    /**
     * Reset check germplasm details.
     *
     * @param model the model
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="/resetCheckGermplasmDetails", method = RequestMethod.GET)
    public String resetCheckGermplasmDetails( 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);
            
            List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
                        
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
            
            userSelection.setCurrentPageCheckGermplasmList(1);

            getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
            getUserSelection().setImportValid(true);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "success";
    }
    
    @RequestMapping(value="/edit/check/{index}/{dataTableIndex}/{type}", method = RequestMethod.GET)
    public String editCheck( @ModelAttribute("updatedGermplasmCheckForm") UpdateGermplasmCheckForm form, 
    		Model model, @PathVariable int index, @PathVariable int dataTableIndex,
    		@PathVariable String type, @RequestParam(value="currentVal") String currentVal) {
        
    	
        try {
        	ImportedGermplasm importedCheckGermplasm = null; 
        	if(type != null && type.equalsIgnoreCase(StudyType.T.getName())){
        		importedCheckGermplasm = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(dataTableIndex);
        	}else if(type != null && type.equalsIgnoreCase(StudyType.N.getName())){
    			importedCheckGermplasm = getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(dataTableIndex);	
        	}
        	importedCheckGermplasm.setCheck(currentVal);
        	List<Enumeration> allEnumerations = fieldbookService.getCheckList();

        	model.addAttribute("allCheckTypes", allEnumerations);
        	form.setCheckVal(currentVal);
        	form.setIndex(index);
        	form.setDataTableIndex(dataTableIndex);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, EDIT_CHECK);
    }
    @ResponseBody
    @RequestMapping(value="/update/check/{type}", method = RequestMethod.POST)
    public String updateCheck( @ModelAttribute("updatedGermplasmCheckForm") UpdateGermplasmCheckForm form, @PathVariable String type, Model model) {
        
        try {
        	ImportedGermplasm importedCheckGermplasm = null; 
        	if(type != null && type.equalsIgnoreCase(StudyType.T.getName())){
        		importedCheckGermplasm = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(form.getIndex());
        	}else if(type != null && type.equalsIgnoreCase(StudyType.N.getName())){
    			importedCheckGermplasm = getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(form.getIndex());	
        	}
        	importedCheckGermplasm.setCheck(form.getCheckVal());
        	importedCheckGermplasm.setCheckId(Integer.valueOf(form.getCheckVal()));
        	
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "success";
    }
    
    /**
     * Reset check germplasm details.
     *
     * @param model the model
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="/resetNurseryGermplasmDetails", method = RequestMethod.GET)
    public String resetNurseryGermplasmDetails( 
            Model model) {
        
        try {
            ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
            mainInfo.setAdvanceImportType(true);            
            List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
                        
            ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
            importedGermplasmList.setImportedGermplasms(list);
            mainInfo.setImportedGermplasmList(importedGermplasmList);
                        

            getUserSelection().setImportedGermplasmMainInfo(null);
            getUserSelection().setImportValid(true);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "success";
    }
    
    
    /**
     * Gets the paginated list.
     *
     * @param pageNum the page num
     * @param form the form
     * @param model the model
     * @return the paginated list
     */
    @RequestMapping(value="/page/{pageNum}", method = RequestMethod.GET)
    public String getPaginatedList(@PathVariable int pageNum, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
        //this set the necessary info from the session variable
    	
    	form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
    	form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
        form.changePage(pageNum);
        userSelection.setCurrentPageGermplasmList(form.getCurrentPage());
        try {
			model.addAttribute("checkLists", fieldbookService.getCheckList());
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    /**
     * Gets the check paginated list.
     *
     * @param pageNum the page num
     * @param previewPageNum the preview page num
     * @param form the form
     * @param model the model
     * @return the check paginated list
     */
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
			model.addAttribute("checkLists", fieldbookService.getCheckList());
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    
    /**
     * Transform germplasm list data to imported germplasm.
     *
     * @param data the data
     * @param defaultCheckId the default check id
     * @return the list
     */
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
                germplasm.setGroupName(aData.getGroupName());
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
            List<Enumeration> allEnumerations = fieldbookService.getCheckList();
            result.put("success", "1");
            result.put("allCheckTypes", convertObjectToJson(allEnumerations));
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("success", "-1");
        }
        
        return result;
    }
    
    /**
     * Adds the update check type.
     *
     * @param operation the operation
     * @param form the form
     * @param local the local
     * @return the map
     */
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
            if (!validateEnumerationDescription(stdVar.getEnumerations(), enumeration)) {
            	result.put("success", "-1");
            	result.put("error",  messageSource.getMessage("error.add.check.duplicate.description", null, local));
            } else {
            	ontologyService.saveOrUpdateStandardVariableEnumeration(stdVar, enumeration);
                List<Enumeration> allEnumerations = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
                result.put("checkTypes", convertObjectToJson(allEnumerations));
                
                result.put("success", "1");
                result.put("successMessage", message);
            }
            
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
    
    /**
     * Delete check type.
     *
     * @param form the form
     * @param local the local
     * @return the map
     */
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
            return fieldbookService.getCheckList();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
    
    private boolean validateEnumerationDescription(List<Enumeration> enumerations, Enumeration newEnumeration) {
    	if (enumerations != null && !enumerations.isEmpty() && newEnumeration != null && newEnumeration.getDescription() != null) {
    		for (Enumeration enumeration : enumerations) {
    			if (enumeration.getDescription() != null 
    					&& newEnumeration.getDescription().trim().equalsIgnoreCase(enumeration.getDescription().trim())
    					&& !enumeration.getId().equals(newEnumeration.getId())) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
}
