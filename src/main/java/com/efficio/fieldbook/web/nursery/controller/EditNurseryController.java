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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(EditNurseryController.URL)
public class EditNurseryController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EditNurseryController.class);

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/editNursery";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/NurseryManager/addOrRemoveTraits";
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;
	
    /** The fieldbook service. */
    @Resource
    private FieldbookService fieldbookService;
   
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
	return "NurseryManager/editNursery";
    }

    /**
     * Use existing nursery.
     *
     * @param form the form
     * @param form2 the form2
     * @param nurseryId the nursery id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, 
    		@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, 
            @PathVariable int nurseryId,@RequestParam(required=false) String isAjax, 
            Model model, HttpServletRequest req, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);
    	
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
        if(nurseryId != 0){     
            //settings part
            Workbook workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);

            form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), buildVariates(workbook.getVariates())));
            
            Dataset dataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
            
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId(), false);
            
            //nursery-level
            List<SettingDetail> nurseryLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), false, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
            
            List<SettingDetail> basicDetails = getBasicDetails(nurseryLevelConditions, form);
            
            removeBasicDetailsVariables(nurseryLevelConditions);
            
            userSelection.setBasicDetails(basicDetails);
            form.setStudyId(nurseryId);
            form.setBasicDetails(userSelection.getBasicDetails());
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setSelectionVariatesVariables(userSelection.getSelectionVariates());
            
            form.setNurseryConditions(userSelection.getNurseryConditions());
            //form.setSelectedSettingId(1);
            form.setLoadSettings("1");
            form.setFolderId(Integer.valueOf((int)workbook.getStudyDetails().getParentFolderId()));
            if (form.getFolderId() == 1) {
            	if (nurseryId > 0) {
            		form.setFolderName(AppConstants.PUBLIC_NURSERIES.getString());
            	}
            	else {
            		form.setFolderName(AppConstants.PROGRAM_NURSERIES.getString());
            	}
            }
            else {
            	form.setFolderName(fieldbookMiddlewareService.getFolderNameById(form.getFolderId()));
            }
            
            
            //measurements part
            if (workbook != null) {
                SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations());
            	setMeasurementsData(form, workbook);
            }
            
            //make factors uneditable if experiments exist already
            if (form.isMeasurementDataExisting()) {
                for (SettingDetail setting : userSelection.getPlotsLevelList()) {
                    setting.setDeletable(false);
                }
            }
            
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
        }
        setFormStaticData(form, contextParams);
        model.addAttribute("createNurseryForm", form);
        if(isAjax != null && isAjax.equalsIgnoreCase("1")) {
        	return super.showAjaxPage(model, getContentName());
        }
        
        return super.show(model);
    }
    
    /**
     * Sets the measurements data.
     *
     * @param form the form
     * @param workbook the workbook
     */
    private void setMeasurementsData(CreateNurseryForm form, Workbook workbook) {
    	userSelection.setMeasurementRowList(workbook.getObservations());
        form.setMeasurementRowList(userSelection.getMeasurementRowList());
        form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
        form.setStudyName(workbook.getStudyDetails().getStudyName());
        form.changePage(1);
        userSelection.setCurrentPage(form.getCurrentPage());
        userSelection.setWorkbook(workbook);
    }
    
    /**
     * Gets the basic details.
     *
     * @param nurseryLevelConditions the nursery level conditions
     * @return the basic details
     */
    private List<SettingDetail> getBasicDetails(List<SettingDetail> nurseryLevelConditions, CreateNurseryForm form) {
        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        
        StringTokenizer token = new StringTokenizer(AppConstants.FIXED_NURSERY_VARIABLES.getString(), ",");
        while(token.hasMoreTokens()){
            Integer termId = Integer.valueOf(token.nextToken());
            boolean isFound = false;
            for (SettingDetail setting : nurseryLevelConditions) {
                if (termId.equals(setting.getVariable().getCvTermId())) {
                    isFound = true;
                    if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {
                        try {
                        	if (setting.getValue() != null && !setting.getValue().isEmpty() && NumberUtils.isNumber(setting.getValue())) {
                        		form.setCreatedBy(fieldbookService.getPersonById(Integer.parseInt(setting.getValue())));
                        	}
                        }
                        catch (MiddlewareQueryException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    } else if (termId.equals(Integer.valueOf(TermId.STUDY_UPDATE.getId()))) {
                        DateFormat dateFormat = new SimpleDateFormat(DateUtil.DB_DATE_FORMAT);
                        Date date = new Date();
                        setting.setValue(dateFormat.format(date));
                    }
                    basicDetails.add(setting);
                }
            }  
            if(!isFound){
                try {
                    basicDetails.add(createSettingDetail(termId, null));
                    if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {
                        try {
                            form.setCreatedBy(fieldbookService.getPersonById(this.getCurrentIbdbUserId()));
                        }
                        catch (MiddlewareQueryException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                } catch (MiddlewareQueryException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        return basicDetails;
    }
    
    /**
     * Removes the basic details variables.
     *
     * @param nurseryLevelConditions the nursery level conditions
     */
    private void removeBasicDetailsVariables(List<SettingDetail> nurseryLevelConditions) {
        Iterator<SettingDetail> iter = nurseryLevelConditions.iterator();
        while (iter.hasNext()) {
            if (inFixedNurseryList(iter.next().getVariable().getCvTermId())) {
                iter.remove();
            }
        }
    }
    
    /**
     * In fixed nursery list.
     *
     * @param propertyId the property id
     * @return true, if successful
     */
    private boolean inFixedNurseryList(int propertyId) {
        StringTokenizer token = new StringTokenizer(AppConstants.FIXED_NURSERY_VARIABLES.getString(), ",");
        while(token.hasMoreTokens()){
            if (Integer.parseInt(token.nextToken()) == propertyId) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Show.
     *
     * @param form the form
     * @param form2 the form2
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
    				@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, 
    				Model model, HttpServletRequest req, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException {
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    	setFormStaticData(form, contextParams);
    	assignDefaultValues(form);
    	return super.show(model);
    }
    
    /**
     * Assign default values.
     *
     * @param form the form
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void assignDefaultValues(CreateNurseryForm form) throws MiddlewareQueryException {
        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> plotDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryConditions = new ArrayList<SettingDetail>();
        
        basicDetails = buildDefaultVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString(), buildRequiredVariablesLabel(AppConstants.FIXED_NURSERY_VARIABLES.getString(), false));
        form.setBasicDetails(basicDetails);
        form.setStudyLevelVariables(nurseryDefaults);
        form.setPlotLevelVariables(plotDefaults);
        nurseryDefaults = buildDefaultVariables(nurseryDefaults, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true));
        plotDefaults = buildDefaultVariables(plotDefaults, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false));
        
        this.userSelection.setBasicDetails(basicDetails);
        this.userSelection.setStudyLevelConditions(nurseryDefaults);
        this.userSelection.setPlotsLevelList(plotDefaults);
        this.userSelection.setBaselineTraitsList(baselineTraitsList);
        this.userSelection.setNurseryConditions(nurseryConditions);
    }    

    /**
     * Submit.
     *
     * @param form the form
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Map<String, String> submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
        //get the name of the nursery
    	String name = null;
    	for (SettingDetail nvar : form.getBasicDetails()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}

    	//combine all study conditions (basic details and management details and hidden variables)
    	List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
    	if (form.getStudyLevelVariables() != null && !form.getStudyLevelVariables().isEmpty()) {
    		studyLevelVariables.addAll(form.getStudyLevelVariables());
    	}
    	studyLevelVariables.addAll(form.getBasicDetails());
    	    	 
    	List<SettingDetail> studyLevelVariablesSession = userSelection.getBasicDetails();
    	userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);
    	if (userSelection.getRemovedConditions() != null) {
    	    studyLevelVariables.addAll(userSelection.getRemovedConditions());
    	    userSelection.getStudyLevelConditions().addAll(userSelection.getRemovedConditions());
    	}
    	    	
    	//add hidden variables like OCC in factors list
    	if (userSelection.getRemovedFactors() != null) {
    		form.getPlotLevelVariables().addAll(userSelection.getRemovedFactors());
    		userSelection.getPlotsLevelList().addAll(userSelection.getRemovedFactors());
    	}
    	
    	//combine all variates (traits and selection variates)
    	List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
    	List<SettingDetail> baselineTraitsSession = userSelection.getSelectionVariates();
    	if (baselineTraits == null) {
    	    baselineTraits = form.getSelectionVariatesVariables();
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	} else if (form.getSelectionVariatesVariables() != null) {
    	    baselineTraits.addAll(form.getSelectionVariatesVariables());
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	}

    	if (form.getPlotLevelVariables() == null) {
    		form.setPlotLevelVariables(new ArrayList<SettingDetail>());
    	}
    	if (baselineTraits == null) {
    		baselineTraits = new ArrayList<SettingDetail>();
    	}
    	if (form.getNurseryConditions() == null) {
    		form.setNurseryConditions(new ArrayList<SettingDetail>());
    	}
    	
    	//include deleted list if measurements are available
    	addDeletedSettingsList(studyLevelVariables, userSelection.getDeletedStudyLevelConditions(), 
    	    userSelection.getStudyLevelConditions());
    	addDeletedSettingsList(form.getPlotLevelVariables(), userSelection.getDeletedPlotLevelList(), 
    	    userSelection.getPlotsLevelList());
    	addDeletedSettingsList(baselineTraits, userSelection.getDeletedBaselineTraitsList(), 
    	    userSelection.getBaselineTraitsList());
    	addDeletedSettingsList(form.getNurseryConditions(), userSelection.getDeletedNurseryConditions(), 
            userSelection.getNurseryConditions());
        
		int trialDatasetId = userSelection.getWorkbook().getTrialDatasetId();
	    //retain measurement dataset id
	    int measurementDatasetId = userSelection.getWorkbook().getMeasurementDatesetId(); 

    	Dataset dataset = (Dataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, studyLevelVariables, 
    	        form.getPlotLevelVariables(), baselineTraits, userSelection, form.getNurseryConditions());
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
    	workbook.setOriginalObservations(userSelection.getWorkbook().getOriginalObservations());
    	workbook.setTrialDatasetId(trialDatasetId);
    	workbook.setMeasurementDatesetId(measurementDatasetId);
    	workbook.setTrialObservations(userSelection.getWorkbook().getTrialObservations());
    	    	
    	this.createStudyDetails(workbook, form.getBasicDetails(), form.getFolderId(), form.getStudyId());
    	userSelection.setWorkbook(workbook);
    	        
    	Map<String, String> resultMap = new HashMap<String, String>();
    	//saving of measurement rows
    	if (userSelection.getMeasurementRowList() != null && userSelection.getMeasurementRowList().size() > 0) {
            try {
                addMeasurementDataToRows(workbook.getFactors(), false);
                addMeasurementDataToRows(workbook.getVariates(), true);
                
                workbook.setMeasurementDatasetVariables(null);
                form.setMeasurementRowList(userSelection.getMeasurementRowList());
                form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
                workbook.setObservations(form.getMeasurementRowList());
                
                userSelection.setWorkbook(workbook);
                //validationService.validateObservationValues(workbook);
                
                fieldbookService.createIdCodeNameVariablePairs(userSelection.getWorkbook(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
                fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), userSelection.getRemovedConditions(), AppConstants.ID_NAME_COMBINATION.getString(), true);
                fieldbookMiddlewareService.saveMeasurementRows(workbook);
                workbook.setTrialObservations(
                		fieldbookMiddlewareService.buildTrialObservations(trialDatasetId, workbook.getTrialConditions(), workbook.getTrialConstants()));
                workbook.setOriginalObservations(workbook.getObservations());
                
                resultMap.put("status", "1");
                resultMap.put("hasMeasurementData", String.valueOf(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), buildVariates(workbook.getVariates()))));
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage());
                resultMap.put("status", "-1");
                resultMap.put("errorMessage", e.getMessage());
            }
            return resultMap;
    	} else {
    	    resultMap.put("status", "1");
    	    return resultMap;
    	}
    	
    }
    
    /**
     * Adds the deleted settings list.
     *
     * @param formList the form list
     * @param deletedList the deleted list
     * @param sessionList the session list
     */
    private void addDeletedSettingsList(List<SettingDetail> formList, List<SettingDetail> deletedList, List<SettingDetail> sessionList) {
        if (deletedList != null) {
            List<SettingDetail> newDeletedList = new ArrayList<SettingDetail>();
            for (SettingDetail setting : deletedList) {
                if (setting.getVariable().getOperation().equals(Operation.UPDATE)) {
                    setting.getVariable().setOperation(Operation.DELETE);
                    newDeletedList.add(setting);
                }
            }
            if (!newDeletedList.isEmpty()) {
                if (formList == null) formList = new ArrayList<SettingDetail>();
                formList.addAll(newDeletedList);
                if (sessionList == null) sessionList = new ArrayList<SettingDetail>();
                sessionList.addAll(newDeletedList);
            }
        }
    }
          
    /**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    private void setFormStaticData(CreateNurseryForm form, String contextParams){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(fieldbookProperties.getProgramBreedintMethodsUrl());
        form.setLocationUrl(fieldbookProperties.getProgramLocationsUrl());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams);
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    	form.setOpenGermplasmUrl(fieldbookProperties.getGermplasmDetailsUrl());
    	form.setBaselineTraitsSegment(AppConstants.SEGMENT_TRAITS.getString());
    	form.setSelectionVariatesSegment(AppConstants.SEGMENT_SELECTION_VARIATES.getString());
    	form.setCharLimit(Integer.parseInt(AppConstants.CHAR_LIMIT.getString()));
    	form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
        form.setIdCodeNameCombination(AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString() + "," + AppConstants.ID_CODE_NAME_COMBINATION_VARIATE.getString());
        form.setBreedingMethodCode(AppConstants.BREEDING_METHOD_CODE.getString());
    }
    
    /**
     * Check measurement data.
     *
     * @param form the form
     * @param model the model
     * @param mode the mode
     * @param variableId the variable id
     * @return the map
     */
    @ResponseBody
    @RequestMapping(value = "/checkMeasurementData/{mode}/{variableId}", method = RequestMethod.GET)
    public Map<String, String> checkMeasurementData(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int mode, @PathVariable int variableId) {
        Map<String, String> resultMap = new HashMap<String, String>();
        boolean hasData = false;
        
        //if there are measurement rows, check if values are already entered
        if (userSelection.getMeasurementRowList() != null && !userSelection.getMeasurementRowList().isEmpty()) {
            for (MeasurementRow row: userSelection.getMeasurementRowList()) {
                for (MeasurementData data: row.getDataList()) {
                    if (data.getMeasurementVariable().getTermId() == variableId && data.getValue() != null && !data.getValue().isEmpty()) {
                        hasData = true;
                        break;
                    }
                }
                if (hasData) break;
            }
        }

        if (hasData)
            resultMap.put("hasMeasurementData", "1");
        else 
            resultMap.put("hasMeasurementData", "0");
        
        return resultMap;
    }
        
    /**
     * Removes the deleted set update.
     *
     * @param settingList the setting list
     * @param variableList the variable list
     */
    private void removeDeletedSetUpdate(List<SettingDetail> settingList, List<MeasurementVariable> variableList) {
    	if (settingList != null) {
    		//remove all variables having delete and add operation
	    	Iterator<SettingDetail> iter = settingList.iterator();
	        while (iter.hasNext()) {
	        	SettingDetail setting = iter.next();
	        	if (setting.getVariable().getOperation() != null && setting.getVariable().getOperation().equals(Operation.DELETE)) {
	        		iter.remove();
	        	} else if (setting.getVariable().getOperation() != null && setting.getVariable().getOperation().equals(Operation.ADD)) {
	        		setting.getVariable().setOperation(Operation.UPDATE);
	        	} 
	        }
    	}
        
    	if (variableList != null) {
    		//remove all variables having delete and add operation
	        Iterator<MeasurementVariable> iter2 = variableList.iterator();
	        while (iter2.hasNext()) {
	        	MeasurementVariable var = iter2.next();
	        	if (var.getOperation() != null && var.getOperation().equals(Operation.DELETE)) {
	        		iter2.remove();
	        	} else if (var.getOperation() != null && var.getOperation().equals(Operation.ADD)) {
	        		var.setOperation(Operation.UPDATE);
	        	}
	        }
    	}
    }
    
    /**
     * Reset deleted lists.
     */
    private void resetDeletedLists() {
    	userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
    	userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
    	userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
    	userSelection.setDeletedNurseryConditions(new ArrayList<SettingDetail>());
    }
    
    /**
     * Gets the data type.
     *
     * @param dataTypeId the data type id
     * @return the data type
     */
    private String getDataType(int dataTypeId) {
	    //datatype ids: 1120, 1125, 1128, 1130
	    if (dataTypeId == TermId.CHARACTER_VARIABLE.getId() || dataTypeId == TermId.TIMESTAMP_VARIABLE.getId() || 
	            dataTypeId == TermId.CHARACTER_DBID_VARIABLE.getId() || dataTypeId == TermId.CATEGORICAL_VARIABLE.getId()) {
	        return "C";
	    } else {
	        return "N";
	    }
	}
    
    /**
     * Removes the selection variates from traits.
     *
     * @param traits the traits
     * @throws MiddlewareQueryException the middleware query exception
     */
    private void removeSelectionVariatesFromTraits(List<SettingDetail> traits) throws MiddlewareQueryException {
    	if (traits != null) {
    		Iterator<SettingDetail> iter = traits.iterator();
    		while (iter.hasNext()) {
    			SettingDetail var = iter.next();
    			if (SettingsUtil.inPropertyList(ontologyService.getProperty(var.getVariable().getProperty()).getId())) {
    				iter.remove();
    			}
    		}
    	}
    }
    
    /**
     * Transform possible values.
     *
     * @param enumerations the enumerations
     * @return the list
     */
    private List<ValueReference> transformPossibleValues(List<Enumeration> enumerations) {
		List<ValueReference> list = new ArrayList<ValueReference>();
		
		if (enumerations != null) {
			for (Enumeration enumeration : enumerations) {
				list.add(new ValueReference(enumeration.getId(), enumeration.getName(), enumeration.getDescription()));
			}
		}
		
		return list;
	}
    
    /**
     * Removes the hidden variables.
     *
     * @param nurseryLevelConditions the nursery level conditions
     */
    private void removeHiddenVariables(List<SettingDetail> settingList, String hiddenVarList) {
        if (settingList != null) {
            
            Iterator<SettingDetail> iter = settingList.iterator();
            while (iter.hasNext()) {
                if (SettingsUtil.inHideVariableFields(iter.next().getVariable().getCvTermId(), hiddenVarList)) {
                    iter.remove();
                }
            }
        }
    }
    
    private void addMeasurementDataToRows(List<MeasurementVariable> variableList, boolean isVariate) throws MiddlewareQueryException{
        //add new variables in measurement rows
        for (MeasurementVariable variable : variableList) {
            if (variable.getOperation().equals(Operation.ADD)) {
                StandardVariable stdVariable = ontologyService.getStandardVariable(variable.getTermId());
                for (MeasurementRow row : userSelection.getMeasurementRowList()) {
                    MeasurementData measurementData = new MeasurementData(variable.getName(), 
                            "", true,  
                            getDataType(variable.getDataTypeId()),
                            variable);
                    
                    measurementData.setPhenotypeId(null);
                    int insertIndex = getInsertIndex(row.getDataList(), isVariate);
                    row.getDataList().add(insertIndex, measurementData);
                }
                
                if (ontologyService.getProperty(variable.getProperty()).getTerm().getId() == TermId.BREEDING_METHOD_PROP.getId() && isVariate) {
                    variable.setPossibleValues(fieldbookService.getAllBreedingMethods(true));
                } else {
                    variable.setPossibleValues(transformPossibleValues(stdVariable.getEnumerations()));
                }
            }
        }
    }
    
    private int getInsertIndex(List<MeasurementData> dataList, boolean isVariate) {
        int index = -1;
        if (dataList != null) {
            if (!isVariate) {
                for (MeasurementData data : dataList) {
                    if ((!data.getMeasurementVariable().isFactor())) {
                        return index;
                    }
                    index++;
                }
            } else {
                return dataList.size();
            }
        }
        return index;
    }
    
    private void removeDeletedVariablesInMeasurements(List<SettingDetail> deletedList, Workbook workbook) {
        if (deletedList != null) {
            for (SettingDetail setting : deletedList) {
                //remove from measurement rows
                int index = 0;
                int varIndex = 0;
                for (MeasurementRow row : userSelection.getMeasurementRowList()) {
                    if (index == 0) {
                        for (MeasurementData var : row.getDataList()) {
                            if (var.getMeasurementVariable().getTermId() == setting.getVariable().getCvTermId()) {
                                break;
                            }
                            varIndex++;
                        }
                    }
                    row.getDataList().remove(varIndex);
                    index++;
                }
                //remove from header
                if (workbook.getMeasurementDatasetVariables() != null) {
                    Iterator<MeasurementVariable> iter = workbook.getMeasurementDatasetVariables().iterator();
                    while(iter.hasNext()) {
                        if (iter.next().getTermId() == setting.getVariable().getCvTermId()) {
                            iter.remove();
                        }
                    }
                }
            }
        }
    }
    
    private void addSettingDetail(List<SettingDetail> removedConditions, Map<String, SettingDetail> removedConditionsMap, 
            Map<String, MeasurementVariable> studyConditionMap, String id, String value) throws MiddlewareQueryException {
        if (removedConditionsMap.get(id) == null) {
            removedConditions.add(createSettingDetail(Integer.parseInt(id), studyConditionMap.get(id).getName()));
        }
        if (removedConditions != null) {
            for (SettingDetail setting : removedConditions) {
                if (setting.getVariable().getCvTermId() == Integer.parseInt(id)) {
                    setting.setValue(value);
                    setting.getVariable().setOperation(Operation.UPDATE);
                }
            }
        }
    }
    
    private void removeCodeVariablesIfNeeded(List<SettingDetail> variableList, String idCodeNamePairs) {
        Map<String, SettingDetail> variableListMap = new HashMap<String, SettingDetail>();
        if (variableList != null) {
            for(SettingDetail setting : variableList){
                if(setting != null){
                    variableListMap.put(Integer.toString(setting.getVariable().getCvTermId()), setting);
                }
            }
        }
        
        StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
        if(tokenizer.hasMoreTokens()){
          //we iterate it
            while(tokenizer.hasMoreTokens()){
                String pair = tokenizer.nextToken();
                StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
                String idTermId = tokenizerPair.nextToken();
                String codeTermId = tokenizerPair.nextToken();
                
                Iterator<SettingDetail> iter = variableList.iterator();
                while (iter.hasNext()) {
                    Integer cvTermId = iter.next().getVariable().getCvTermId();
                    if (cvTermId.equals(Integer.parseInt(codeTermId)) && variableListMap.get(idTermId) != null) {
                        iter.remove();
                    }
                }
            }
        }
    }
    
    private void addNameVariables(List<SettingDetail> removedConditions, Workbook workbook, String idCodeNamePairs) throws MiddlewareQueryException {
        Map<String, MeasurementVariable> studyConditionMap = new HashMap<String, MeasurementVariable>();
        Map<String, SettingDetail> removedConditionsMap = new HashMap<String, SettingDetail>();
        if (workbook != null && idCodeNamePairs != null && !idCodeNamePairs.equalsIgnoreCase("")) {
            //we get a map so we can check easily instead of traversing it again
            for(MeasurementVariable var : workbook.getConditions()){
                if(var != null){
                    studyConditionMap.put(Integer.toString(var.getTermId()), var);
                }
            }
            
            if (removedConditions != null) {
                for(SettingDetail setting : removedConditions){
                    if(setting != null){
                        removedConditionsMap.put(Integer.toString(setting.getVariable().getCvTermId()), setting);
                    }
                }
            }
            
            StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
            if(tokenizer.hasMoreTokens()){
              //we iterate it
                while(tokenizer.hasMoreTokens()){
                    String pair = tokenizer.nextToken();
                    StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
                    String idTermId = tokenizerPair.nextToken();
                    String codeTermId = tokenizerPair.nextToken();
                    String nameTermId = tokenizerPair.nextToken();
                    
                    Method method = null;
                    if (studyConditionMap.get(idTermId) != null) {
                        method = studyConditionMap.get(idTermId).getValue().isEmpty() ? null : fieldbookMiddlewareService.getMethodById(Integer.parseInt(studyConditionMap.get(idTermId).getValue()));
                    } else if (studyConditionMap.get(codeTermId) != null) {
                        method = studyConditionMap.get(codeTermId).getValue().isEmpty() ? null : fieldbookMiddlewareService.getMethodByCode(studyConditionMap.get(codeTermId).getValue());
                    }
                    
                    //add code to the removed conditions if code is not yet in the list
                    if (studyConditionMap.get(idTermId) != null && studyConditionMap.get(codeTermId) != null && removedConditionsMap.get(codeTermId) == null) {
                        addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, codeTermId, 
                                method == null ? "" : method.getMcode());
                    }
                    
                    //add name to the removed conditions if name is not yet in the list
                    if (studyConditionMap.get(nameTermId) != null && removedConditionsMap.get(nameTermId) == null) {
                        addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, nameTermId, 
                                method == null ? "" : method.getMname());
                    }
                }
            }
        }        
    }
    
    /**
     * Reset session variables after save.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/recreate/session/variables", method = RequestMethod.GET)
    public String resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
    		HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);

    	Workbook workbook = userSelection.getWorkbook();
        form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), buildVariates(workbook.getVariates())));
    	//update variables in measurement rows
    	if (userSelection.getMeasurementRowList() != null && !userSelection.getMeasurementRowList().isEmpty()) {
    		MeasurementRow row = userSelection.getMeasurementRowList().get(0);
    		for (MeasurementVariable mvar : workbook.getMeasurementDatasetVariables()) {
    			if (mvar.getOperation() == Operation.UPDATE) {
    				for (MeasurementVariable rvar : row.getMeasurementVariables()) {
    					if (mvar.getTermId() == rvar.getTermId()) {
    						if (mvar.getName() != null && !"".equals(mvar.getName())) {
    							rvar.setName(mvar.getName());
    						}
    						break;
    					}
    				}
    			}
    		}
    	}
    	
    	//remove deleted variables in measurement rows & header for variates
    	removeDeletedVariablesInMeasurements(userSelection.getDeletedPlotLevelList(), workbook);
    	removeDeletedVariablesInMeasurements(userSelection.getDeletedBaselineTraitsList(), workbook);
    	
    	//remove deleted variables in the original lists
    	//and change add operation to update
    	removeDeletedSetUpdate(userSelection.getStudyLevelConditions(), workbook.getConditions());
    	removeDeletedSetUpdate(userSelection.getPlotsLevelList(), workbook.getFactors());
    	removeDeletedSetUpdate(userSelection.getBaselineTraitsList(), workbook.getVariates());
    	removeDeletedSetUpdate(userSelection.getNurseryConditions(), workbook.getConstants());
    	removeDeletedSetUpdate(userSelection.getSelectionVariates(), null);
    	workbook.setMeasurementDatasetVariables(null);
    	
    	//reorder variates based on measurementrow order
    	int index = 0;
		List<MeasurementVariable> newVariatesList = new ArrayList<MeasurementVariable>();
		if (userSelection.getMeasurementRowList() != null) {
    		for (MeasurementRow row : userSelection.getMeasurementRowList()) {
    			if (index == 0) {
    				for (MeasurementData var : row.getDataList()) {
    					for (MeasurementVariable varToArrange : workbook.getVariates()) {
    						if (var.getMeasurementVariable().getTermId() == varToArrange.getTermId()) {
    							newVariatesList.add(varToArrange);
    						}
    			    	}
    				}
    			}
    			index++;
    			break;
    		}
		}
    	workbook.setVariates(newVariatesList);
		    	
    	//remove deleted variables in the deleted lists
    	resetDeletedLists();
    	
    	//add name variables
    	
    	if (userSelection.getRemovedConditions() == null) {
    	    userSelection.setRemovedConditions(new ArrayList<SettingDetail>());
    	}
    	addNameVariables(userSelection.getRemovedConditions(), workbook, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
    	
    	
    	//remove basic details & hidden variables from study level variables
    	removeBasicDetailsVariables(userSelection.getStudyLevelConditions());
    	removeHiddenVariables(userSelection.getStudyLevelConditions(), AppConstants.HIDE_NURSERY_FIELDS.getString());
    	removeCodeVariablesIfNeeded(userSelection.getStudyLevelConditions(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
    	removeHiddenVariables(userSelection.getPlotsLevelList(), AppConstants.HIDE_PLOT_FIELDS.getString());
    	
    	//set value of breeding method code back to code after saving
    	SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations());
    	
    	//set measurement session variables to form
    	setMeasurementsData(form, workbook);
        
        //remove selection variates from traits list
        removeSelectionVariatesFromTraits(userSelection.getBaselineTraitsList());
        
    	setFormStaticData(form, contextParams);
        model.addAttribute("createNurseryForm", form);
    	
        return super.showAjaxPage(model, URL_SETTINGS);
    }
    
    /**
     * Show variable details.
     *
     * @param id the id
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="/showVariableDetails/{id}", method = RequestMethod.GET)
    public String showVariableDetails(@PathVariable int id) {
    	try {

    		SettingVariable svar = getSettingVariable(id);
    		if (svar != null) {
    			ObjectMapper om = new ObjectMapper();
    			return om.writeValueAsString(svar);
    		}
    		
    	} catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	return "[]";
    }
    
    private List<Integer> buildVariates(List<MeasurementVariable> variates) {
        List<Integer> variateList = new ArrayList<Integer>();
        if (variates != null) {
            for (MeasurementVariable var : variates) {
                variateList.add(new Integer(var.getTermId()));
            }
        }
        return variateList;
    }
    
    @ResponseBody
    @RequestMapping(value="/deleteMeasurementRows", method = RequestMethod.POST)
    public Map<String, String> deleteMeasurementRows() {
        Map<String, String> resultMap = new HashMap<String, String>();
        
        try {
            fieldbookMiddlewareService.deleteObservationsOfStudy(userSelection.getWorkbook().getMeasurementDatesetId());
            resultMap.put("status", "1");
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage());
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());   
        }
        
        userSelection.setMeasurementRowList(null);
        userSelection.getWorkbook().setOriginalObservations(null);
        userSelection.getWorkbook().setObservations(null);
        return resultMap;
    }
    
    @ModelAttribute("nameTypes")
    public List<UserDefinedField> getNameTypes(){
        try {
            List<UserDefinedField> nameTypes = fieldbookMiddlewareService.getGermplasmNameTypes();
            
            return nameTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
}
