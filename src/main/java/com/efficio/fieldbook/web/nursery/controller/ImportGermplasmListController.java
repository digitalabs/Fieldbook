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

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.form.UpdateGermplasmCheckForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This controller handles the 2nd step in the nursery manager process.
 * 
 * @author Daniel Jao
 */
@Controller
@RequestMapping({ImportGermplasmListController.URL, ImportGermplasmListController.URL_2, ImportGermplasmListController.URL_3 , ImportGermplasmListController.URL_4})
public class ImportGermplasmListController extends SettingsController {
    
	private static final String SUCCESS = "success";

	private static final String ERROR = "error";

	private static final String TABLE_HEADER_LIST = "tableHeaderList";

	protected static final String TYPE2 = "type";

	protected static final String LIST_DATA_TABLE = "listDataTable";

	protected static final String CHECK_LISTS = "checkLists";

	protected static final String ENTRY_CODE = "entryCode";

	protected static final String SOURCE = "source";

	protected static final String CROSS = "cross";

	protected static final String CHECK = "check";

	protected static final String GID = "gid";

	protected static final String DESIG = "desig";

	protected static final String ENTRY = "entry";

	protected static final String CHECK_OPTIONS = "checkOptions";

	protected static final String POSITION = "position";

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
    
    @Resource
    private OntologyDataManager ontologyDataManager;
    
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
		String[] selectedCheck = form.getSelectedCheck();
		boolean isNursery = userSelection.getWorkbook().getStudyDetails().getStudyType() == StudyType.N ? true : false;			
		boolean hasTemporaryWorkbook = false;
		
	    if (userSelection.getTemporaryWorkbook() != null) {
            WorkbookUtil.manageExpDesignVariablesAndObs(userSelection.getWorkbook(), userSelection.getTemporaryWorkbook());
            WorkbookUtil.addMeasurementDataToRowsExp(userSelection.getWorkbook().getFactors(), userSelection.getWorkbook().getObservations(), 
                    false, userSelection, ontologyService, fieldbookService);
            WorkbookUtil.addMeasurementDataToRowsExp(userSelection.getWorkbook().getVariates(), userSelection.getWorkbook().getObservations(), 
                    true, userSelection, ontologyService, fieldbookService);
            
            
            if (userSelection.getExperimentalDesignVariables() != null){
            	Set<MeasurementVariable> unique = new HashSet<>(userSelection.getWorkbook().getFactors());
                unique.addAll(userSelection.getExperimentalDesignVariables());
            	userSelection.getWorkbook().getFactors().clear();
            	userSelection.getWorkbook().getFactors().addAll(unique);


                Set<MeasurementVariable> makeUniqueVariates = new HashSet<>(userSelection.getTemporaryWorkbook().getVariates());
                makeUniqueVariates.addAll(userSelection.getWorkbook().getVariates());
                userSelection.getWorkbook().getVariates().clear();
                userSelection.getWorkbook().getVariates().addAll(makeUniqueVariates);
            }
            
            Map<Integer, MeasurementVariable> observationVariables = WorkbookUtil.createVariableList(userSelection.getWorkbook().getFactors(), userSelection.getWorkbook().getVariates());
            
            WorkbookUtil.deleteDeletedVariablesInObservations(observationVariables, userSelection.getWorkbook().getObservations());
            userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
            WorkbookUtil.updateTrialObservations(userSelection.getWorkbook(),userSelection.getTemporaryWorkbook());
            userSelection.setTemporaryWorkbook(null);
            hasTemporaryWorkbook = true;
            isDeleteObservations = true;
        
        }  
	    
	    if (isNursery && !hasTemporaryWorkbook){
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
            	if (getUserSelection().getImportedCheckGermplasmMainInfo() != null && form.getImportedCheckGermplasm() != null 
            			&& SettingsUtil.checkVariablesHaveValues(form.getCheckVariables())) {
            		String lastDragCheckList = form.getLastDraggedChecksList();        		
            		if("0".equalsIgnoreCase(lastDragCheckList)){
            			//we do the cleaning here	
            			List<ImportedGermplasm> newNurseryGermplasm = cleanGermplasmList(form.getImportedGermplasm(), 
            	    	        form.getImportedCheckGermplasm());
            			form.setImportedGermplasm(newNurseryGermplasm);
            		}
            		
            		int interval = getIntervalValue(form);

            		String defaultTestCheckId = getCheckId(DEFAULT_TEST_VALUE, fieldbookService.getCheckList());
            		
            		List<ImportedGermplasm> newImportedGermplasm = mergeCheckService.mergeGermplasmList(form.getImportedGermplasm(), 
        	    	        form.getImportedCheckGermplasm(), 
        	    	        Integer.parseInt(SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_START.getId())), 
        	    	        interval, 
        	    	        SettingsUtil.getCodeInPossibleValues(SettingsUtil.getFieldPossibleVales(fieldbookService, TermId.CHECK_PLAN.getId()), SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_PLAN.getId())), 
        	    	        defaultTestCheckId);
            		
        	    	getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(newImportedGermplasm);
        	    	form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
            	}
            	
            	//this would validate and add CHECK factor if necessary
                importGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(), getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), userSelection);
                userSelection.setMeasurementRowList(measurementsGeneratorService.generateRealMeasurementRows(userSelection));
                
                //add or remove check variables if needed
                fieldbookService.manageCheckVariables(userSelection, form);
            }
        } else if (!hasTemporaryWorkbook) {
            isDeleteObservations = true;
            userSelection.setMeasurementRowList(null);
        }
                    
        userSelection.getWorkbook().setObservations(userSelection.getMeasurementRowList());
        
        fieldbookService.createIdCodeNameVariablePairs(userSelection.getWorkbook(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
        fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<SettingDetail>(), AppConstants.ID_NAME_COMBINATION.getString(), true);
        int studyId = dataImportService.saveDataset(userSelection.getWorkbook(), true, isDeleteObservations, getCurrentProject().getUniqueID());        
        fieldbookService.saveStudyImportedCrosses(userSelection.getImportedCrossesId(), studyId);
        //for saving the list data project        
        saveListDataProject(isNursery, studyId);
        
        
        fieldbookService.saveStudyColumnOrdering(studyId, userSelection.getWorkbook().getStudyName(), form.getColumnOrders(), userSelection.getWorkbook());
        
        return Integer.toString(studyId);
    }       
    
    private int getIntervalValue(ImportGermplasmListForm form) {
    	String interval = SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_INTERVAL.getId()); 
    	if (interval != null && !("").equals(interval)) {
    		return Integer.parseInt(interval);
    	}
    	return 0;
	}

	private void saveListDataProject(boolean isNursery, int studyId) throws MiddlewareQueryException{
    	//we call here to have
    	
        if(getUserSelection().getImportedGermplasmMainInfo() != null && getUserSelection().getImportedGermplasmMainInfo().getListId() != null){
        	//we save the list
        	//we need to create a new germplasm list
        	Integer listId = getUserSelection().getImportedGermplasmMainInfo().getListId();
        	List<ImportedGermplasm> importedGermplasmList;
        	
        	if (isNursery){
        		if (getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getOriginalImportedGermplasms() != null){
        			importedGermplasmList = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getOriginalImportedGermplasms();
        		}else{
        			importedGermplasmList = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
        		}
        	}else{
        		importedGermplasmList = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
        	}

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
    	
    	if(checkList == null || checkList.isEmpty()) {
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
            	
            	dataMap.put(POSITION, germplasm.getIndex().toString());
				dataMap.put(CHECK_OPTIONS, checkList);
				dataMap.put(ENTRY, germplasm.getEntryId().toString());
				dataMap.put(DESIG, germplasm.getDesig().toString());
				dataMap.put(GID, germplasm.getGid().toString());
				
				
				if(!isNursery){
					germplasm.setCheck(defaultTestCheckId);
					germplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
					dataMap.put(CHECK, defaultTestCheckId);
					
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
					dataMap.put(CROSS, germplasm.getCross().toString());
					dataMap.put(SOURCE, germplasm.getSource().toString());
					dataMap.put(ENTRY_CODE, germplasm.getEntryCode().toString());
					dataMap.put(CHECK, "");
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
            
            model.addAttribute(CHECK_LISTS, fieldbookService.getCheckList());
            model.addAttribute(LIST_DATA_TABLE, dataTableDataList);
            model.addAttribute(TYPE2, type);
            model.addAttribute(TABLE_HEADER_LIST, getGermplasmTableHeader(type, userSelection.getPlotsLevelList()));
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
                
                dataMap.put(POSITION, germplasm.getIndex().toString());
                dataMap.put(CHECK_OPTIONS, checkList);
                dataMap.put(ENTRY, germplasm.getEntryId().toString());
                dataMap.put(DESIG, germplasm.getDesig().toString());
                dataMap.put(GID, germplasm.getGid().toString());
                
                
                if(!isNursery){
                    if (germplasm.getCheck() == null || ("0").equals(germplasm.getCheck())) {
                        germplasm.setCheck(defaultTestCheckId);
                        germplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
                        dataMap.put(CHECK, defaultTestCheckId);
                    } else {
                        dataMap.put(CHECK, germplasm.getCheckId());
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
                    dataMap.put(CROSS, germplasm.getCross().toString());
                    dataMap.put(SOURCE, germplasm.getSource().toString());
                    dataMap.put(ENTRY_CODE, germplasm.getEntryCode().toString());
                    dataMap.put(CHECK, "");
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
            
            model.addAttribute(CHECK_LISTS, fieldbookService.getCheckList());
            model.addAttribute(LIST_DATA_TABLE, dataTableDataList);
            model.addAttribute(TYPE2, type);
            model.addAttribute(TABLE_HEADER_LIST, getGermplasmTableHeader(type, userSelection.getPlotsLevelList()));
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
            
            model.addAttribute(TABLE_HEADER_LIST, getGermplasmCheckTableHeader());
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, CHECK_PAGINATION_TEMPLATE);
    }
    
    private List<TableHeader> getGermplasmTableHeader(String type, List<SettingDetail> factorsList){
    	Locale locale = LocaleContextHolder.getLocale();
    	List<TableHeader> tableHeaderList = new ArrayList<TableHeader>();
    	if(type != null && type.equalsIgnoreCase(StudyType.N.getName())){
 
    		tableHeaderList.add(new TableHeader(messageSource.getMessage("nursery.import.header.position", null, locale), POSITION));
    		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_ID.getTermNameFromOntology(ontologyDataManager), ENTRY));
    		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(ontologyDataManager), DESIG));
    		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(ontologyDataManager), GID));
    		tableHeaderList.add(new TableHeader(ColumnLabels.PARENTAGE.getTermNameFromOntology(ontologyDataManager), CROSS));
    		tableHeaderList.add(new TableHeader(ColumnLabels.SEED_SOURCE.getTermNameFromOntology(ontologyDataManager), SOURCE));
    		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_CODE.getTermNameFromOntology(ontologyDataManager), ENTRY_CODE));
    		
    	}else if(type != null && type.equalsIgnoreCase(StudyType.T.getName()) && factorsList != null){
			//we iterate the map for dynamic header of trial
			for(int counter = 0 ; counter < factorsList.size() ; counter++){
				SettingDetail factorDetail= factorsList.get(counter);
				if(factorDetail != null && factorDetail.getVariable() != null &&
						!SettingsUtil.inHideVariableFields(factorDetail.getVariable().getCvTermId(), AppConstants.HIDE_GERMPLASM_DESCRIPTOR_HEADER_TABLE.getString())){		    					
					tableHeaderList.add(new TableHeader(factorDetail.getVariable().getName(), factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString()));
				}
				    				
			}
    	}
    	return tableHeaderList;
    }
    
    private List<TableHeader> getGermplasmCheckTableHeader(){
    	List<TableHeader> tableHeaderList = new ArrayList<TableHeader>();
		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_TYPE.getTermNameFromOntology(ontologyDataManager), CHECK));
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(ontologyDataManager), DESIG));
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
				dataMap.put(POSITION, germplasm.getIndex().toString());
				dataMap.put(CHECK_OPTIONS, checkList);
				dataMap.put(ENTRY, germplasm.getEntryId().toString());
				dataMap.put(DESIG, germplasm.getDesig().toString());
				dataMap.put(GID, germplasm.getGid().toString());
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
				dataMap.put(CHECK, germplasm.getCheck() != null ? germplasm.getCheck().toString() : "");
				
        		dataTableDataList.add(dataMap);
            }

            model.addAttribute(CHECK_LISTS, fieldbookService.getCheckList());
            model.addAttribute(LIST_DATA_TABLE, dataTableDataList);
            model.addAttribute(TYPE2, type);
            model.addAttribute(TABLE_HEADER_LIST, getGermplasmTableHeader(type, userSelection.getPlotsLevelList()));
            model.addAttribute("hasMeasurement", hasMeasurement());
            
            
            
            form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());           
            form.setImportedGermplasm(list);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    protected Boolean hasMeasurement(){
    	return userSelection.getMeasurementRowList() != null && !userSelection.getMeasurementRowList().isEmpty();
    }
    
    protected String getCheckId(String checkCode,  List<Enumeration> checksList) throws MiddlewareQueryException{
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
            
            model.addAttribute(TABLE_HEADER_LIST, getGermplasmCheckTableHeader());
            
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
            
            model.addAttribute(TABLE_HEADER_LIST, getGermplasmCheckTableHeader());
            
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
				dataMap.put(DESIG, germplasm.getDesig().toString());
				dataMap.put(GID, germplasm.getGid().toString());
				dataMap.put(CHECK, germplasm.getCheck().toString());				
				dataMap.put(ENTRY, germplasm.getEntryId());
				dataMap.put("index", germplasm.getIndex());			
				dataMap.put(CHECK_OPTIONS, checksList);
	    		dataTableDataList.add(dataMap);
	        }        	           
    	}
        model.addAttribute(CHECK_LISTS, checksList);
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
        return SUCCESS;
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
            
            model.addAttribute(TABLE_HEADER_LIST, getGermplasmCheckTableHeader());
            
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
        return SUCCESS;
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
        return SUCCESS;
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
        return SUCCESS;
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
			model.addAttribute(CHECK_LISTS, fieldbookService.getCheckList());
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
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
			model.addAttribute(CHECK_LISTS, fieldbookService.getCheckList());
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
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
        if (data != null && !data.isEmpty()) {
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
            result.put(SUCCESS, "1");
            result.put("allCheckTypes", convertObjectToJson(allEnumerations));
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put(SUCCESS, "-1");
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
            	result.put(SUCCESS, "-1");
            	result.put(ERROR,  messageSource.getMessage("error.add.check.duplicate.description", null, local));
            } else {
            	ontologyService.saveOrUpdateStandardVariableEnumeration(stdVar, enumeration);
                List<Enumeration> allEnumerations = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
                result.put("checkTypes", convertObjectToJson(allEnumerations));
                
                result.put(SUCCESS, "1");
                result.put("successMessage", message);
            }
            
        } catch (MiddlewareQueryException e) {
            LOG.debug(e.getMessage(), e);
            result.put(SUCCESS, "-1");
            result.put(ERROR, e.getMessage());
        } catch (MiddlewareException e) {
            LOG.debug(e.getMessage(), e);
            result.put(SUCCESS, "-1");
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
                result.put(SUCCESS, "-1");
                result.put(ERROR, messageSource.getMessage("nursery.manage.check.types.delete.error", 
                        new Object[] {name}, local));
            } else {
                ontologyService.deleteStandardVariableValidValue(TermId.CHECK.getId(), Integer.parseInt(form.getManageCheckCode()));
                result.put(SUCCESS, "1");
                result.put("successMessage", messageSource.getMessage("nursery.manage.check.types.delete.success", 
                        new Object[] {name}, local));
                List<Enumeration> allEnumerations = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
                result.put("checkTypes", convertObjectToJson(allEnumerations));
            }
            
        } catch (MiddlewareQueryException e) {
            LOG.debug(e.getMessage(), e);
            result.put(SUCCESS, "-1");
            result.put(ERROR, e.getMessage());
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
        return new ArrayList<>();
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
