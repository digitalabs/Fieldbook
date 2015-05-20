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

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.util.JsonIoException;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.*;

import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.*;
/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(CreateNurseryController.URL)
public class CreateNurseryController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(CreateNurseryController.class);

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/createNursery";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/NurseryManager/chooseSettings";
    
    private static final String URL_CHECKS = "/NurseryManager/includes/importGermplasmListCheckSection";
    
    @Resource
    private OntologyService ontologyService;
	
    @Resource
    private ErrorHandlerService errorHandlerService;
   
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
	return "/NurseryManager/createNursery";
    }

    /**
     * Use existing nursery.
     *
     * @param form the form
     * @param nurseryId the nursery id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/nursery/{nurseryId}", method = RequestMethod.GET)
    public String useExistingNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @PathVariable int nurseryId
            , Model model, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);

    	try{
	    	if(nurseryId != 0){     
	            Workbook workbook = fieldbookMiddlewareService.getStudyVariableSettings(nurseryId, true);
	            userSelection.setConstantsWithLabels(workbook.getConstants());
	            fieldbookService.createIdNameVariablePairs(workbook, new ArrayList<SettingDetail>(),AppConstants.ID_NAME_COMBINATION.getString(), false);
	
	            Dataset dataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
	            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProject().getUniqueID(), true, false);
	            
	            //nursery-level
	            List<SettingDetail> nurseryLevelConditions = updateRequiredFields(buildVariableIDList(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
	                    buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true), 
	                    buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()), 
	                    userSelection.getStudyLevelConditions(), false, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
	            removeBasicDetailsVariables(nurseryLevelConditions);
	            
	            //plot-level
	            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildVariableIDList(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
	                    buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false), 
	                    buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
	                    userSelection.getPlotsLevelList(), false, "");
	            
	            //remove variables not needed
	            removeVariablesFromExistingNursery(plotLevelConditions, AppConstants.REMOVE_FACTORS_IN_USE_PREVIOUS_STUDY.getString());
	            
	            userSelection.setStudyLevelConditions(nurseryLevelConditions);
	            userSelection.setPlotsLevelList(plotLevelConditions);
	            
	            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
	            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
	            form.setSelectionVariatesVariables(userSelection.getSelectionVariates());
	            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
	            form.setNurseryConditions(userSelection.getNurseryConditions());
	            form.setLoadSettings("1");            
	            form.setProjectId(this.getCurrentProjectId());
	            
	            form.setMeasurementRowList(new ArrayList<MeasurementRow>());
	            setFormStaticData(form, contextParams);
	        }
    	} catch (MiddlewareQueryException e) {
    		LOG.error(e.getMessage(), e);
    		addErrorMessageToResult(form, e);
    	}
        model.addAttribute("createNurseryForm", form);
        
        return super.showAjaxPage(model, URL_SETTINGS);
    }
    
    @RequestMapping(value="/nursery/getChecks/{nurseryId}", method = RequestMethod.GET)
    public String getChecksForUseExistingNursery(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, @PathVariable int nurseryId
            , Model model, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	if (userSelection.getRemovedConditions() != null) {
    		CreateNurseryForm createNurseryForm = new CreateNurseryForm();
	    	List<SettingDetail> checkVariables = getCheckVariables(userSelection.getRemovedConditions(), createNurseryForm);
	        form.setCheckVariables(checkVariables);
    	}
    	
    	model.addAttribute("importGermplasmListForm", form);
    	
    	return super.showAjaxPage(model, URL_CHECKS);
    }
    
    protected void addErrorMessageToResult(CreateNurseryForm form, MiddlewareQueryException e) {
    	String param = AppConstants.NURSERY.getString();
		form.setHasError("1");
		form.setErrorMessage(errorHandlerService.getErrorMessagesAsString(e.getCode(), 
				new Object[] {param, param.substring(0, 1).toUpperCase()
				.concat(param.substring(1, param.length())), param}, "\n"));
    }
    
    private void removeBasicDetailsVariables(List<SettingDetail> nurseryLevelConditions) {
        Iterator<SettingDetail> iter = nurseryLevelConditions.iterator();
        while (iter.hasNext()) {
            if (inFixedNurseryList(iter.next().getVariable().getCvTermId())) {
                iter.remove();
            }
        }
    }
    
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
    			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, 
    			HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);    	
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    	form.setProjectId(this.getCurrentProjectId());
    	setFormStaticData(form, contextParams);
    	assignDefaultValues(form);
    	form.setMeasurementRowList(new ArrayList<MeasurementRow>());
    	
    	//create check variables for specify checks
    	setCheckVariablesInForm(form2);
    	
    	return super.show(model);
    }
    
    protected void setCheckVariablesInForm(ImportGermplasmListForm form2) throws MiddlewareQueryException {
    	List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();
        checkVariables = buildDefaultVariables(checkVariables, AppConstants.CHECK_VARIABLES.getString(), 
        		buildRequiredVariablesLabel(AppConstants.CHECK_VARIABLES.getString(), false));        		
        form2.setCheckVariables(checkVariables);
        userSelection.setRemovedConditions(checkVariables);
	}

	/**
     * Assign default values.
     *
     * @param form the form
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected void assignDefaultValues(CreateNurseryForm form) throws MiddlewareQueryException {
        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> plotDefaults = new ArrayList<SettingDetail>();
        List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryConditions = new ArrayList<SettingDetail>();
        
        basicDetails = buildDefaultVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString(), buildRequiredVariablesLabel(AppConstants.FIXED_NURSERY_VARIABLES.getString(), false));
        form.setBasicDetails(basicDetails);
        form.setStudyLevelVariables(nurseryDefaults);
        form.setPlotLevelVariables(plotDefaults);
        nurseryDefaults = buildDefaultVariables(nurseryDefaults, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), false));
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
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
    	
    	String name = null;
    	for (SettingDetail nvar : form.getBasicDetails()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}

    	List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
    	if (form.getStudyLevelVariables() != null && !form.getStudyLevelVariables().isEmpty()) {
    		studyLevelVariables.addAll(form.getStudyLevelVariables());
    	}
    	
    	studyLevelVariables.addAll(form.getBasicDetails());
    	
    	addNurseryTypeFromDesignImport(studyLevelVariables);
    	addExperimentalDesignTypeFromDesignImport(studyLevelVariables);
    	 
    	List<SettingDetail> studyLevelVariablesSession = userSelection.getBasicDetails();
    	userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);
    	
    	List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
    	List<SettingDetail> baselineTraitsSession = userSelection.getSelectionVariates();
    	if (baselineTraits == null && form.getSelectionVariatesVariables() != null) {
    	    baselineTraits = form.getSelectionVariatesVariables();
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	} else if (form.getSelectionVariatesVariables() != null) {
    	    baselineTraits.addAll(form.getSelectionVariatesVariables());
    	    userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	}
    	
    	Dataset dataset = (Dataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, studyLevelVariables, 
    	        form.getPlotLevelVariables(), baselineTraits, userSelection, form.getNurseryConditions());
    	SettingsUtil.setConstantLabels(dataset, userSelection.getConstantsWithLabels());
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
    	userSelection.setWorkbook(workbook);

    	this.createStudyDetails(workbook, form.getBasicDetails(), form.getFolderId(), null);
 
    	return "success";
    }
            
    
    private void addNurseryTypeFromDesignImport(List<SettingDetail> studyLevelVariables) {
		
    	SettingDetail nurseryTypeSettingDetail = new SettingDetail();
    	SettingVariable nurseryTypeSettingVariable = new SettingVariable();
    	
    	Integer nurseryTypeValue = userSelection.getNurseryTypeForDesign();
    	
    	nurseryTypeSettingDetail.setValue(String.valueOf(nurseryTypeValue));
    	nurseryTypeSettingVariable.setCvTermId(TermId.NURSERY_TYPE.getId());
    	nurseryTypeSettingVariable.setName("NURSERY_TYPE");
    	nurseryTypeSettingVariable.setOperation(Operation.ADD);
    	nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);
    	
    	if (userSelection.getNurseryTypeForDesign() != null && nurseryTypeValue != null){
   
    		for (SettingDetail settingDetail : studyLevelVariables){
        		if (settingDetail.getVariable().getCvTermId() == TermId.NURSERY_TYPE.getId()){
        			settingDetail.setValue(String.valueOf(nurseryTypeValue));
        			settingDetail.getVariable().setName("NURSERY_TYPE");
        			userSelection.setNurseryTypeForDesign(null);
        			return;
        		}
        	}
    		
        	studyLevelVariables.add(nurseryTypeSettingDetail);
    	}
    	
    	userSelection.setNurseryTypeForDesign(null);
    	
	}
    
    private void addExperimentalDesignTypeFromDesignImport(List<SettingDetail> studyLevelVariables) {
		
    	SettingDetail nurseryTypeSettingDetail = new SettingDetail();
    	SettingVariable nurseryTypeSettingVariable = new SettingVariable();
    	
    	
    	nurseryTypeSettingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
    	nurseryTypeSettingVariable.setCvTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
    	nurseryTypeSettingVariable.setName("EXPERIMENTAL_DESIGN");
    	nurseryTypeSettingVariable.setOperation(Operation.ADD);
    	nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);
    	
    	if (userSelection.getExpDesignVariables() != null && !userSelection.getExpDesignVariables().isEmpty()){
   
    		for (SettingDetail settingDetail : studyLevelVariables){
        		if (settingDetail.getVariable().getCvTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()){
        			settingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
        			settingDetail.getVariable().setName("EXPERIMENTAL_DESIGN");
        			return;
        		}
        	}

        	studyLevelVariables.add(nurseryTypeSettingDetail);
    	}
    	
	}

	/**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    private void setFormStaticData(CreateNurseryForm form, String contextParams){

        // TODO move the translation of static data from form field into either the use of page model, or via Thymeleaf static evaluation

        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(fieldbookProperties.getProgramBreedingMethodsUrl());
        form.setImportLocationUrl(fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams);
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    	form.setOpenGermplasmUrl(fieldbookProperties.getGermplasmDetailsUrl());
    	form.setBaselineTraitsSegment(AppConstants.SEGMENT_TRAITS.getString());
        form.setSelectionVariatesSegment(AppConstants.SEGMENT_SELECTION_VARIATES.getString());
        form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
        form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + "," + AppConstants.FIXED_NURSERY_VARIABLES.getString());
        form.setBreedingMethodCode(AppConstants.BREEDING_METHOD_CODE.getString());
        try {
            form.setCreatedBy(fieldbookService.getPersonByUserId(this.getCurrentIbdbUserId()));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    /**
     * Gets the setting detail list.
     *
     * @param mode the mode
     * @return the setting detail list
     */
    private List<SettingDetail> getSettingDetailList(int mode) {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            return userSelection.getStudyLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            return userSelection.getPlotsLevelList();
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
            List<SettingDetail> newList = new ArrayList<SettingDetail>();
            
            for (SettingDetail setting : userSelection.getBaselineTraitsList()) {
                newList.add(setting);
            }
            
            for (SettingDetail setting : userSelection.getNurseryConditions()) {
                newList.add(setting);
            }
                
            return newList;
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            return userSelection.getSelectionVariates();
        }
        return new ArrayList<SettingDetail>();
    }


    // TODO : refactor out of this class and into the more general ManageSettingsController
    /**
     * Displays the Add Setting popup.
     *
     * @param mode the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "displayAddSetting/{mode}", method = RequestMethod.GET)
    public Map<String, Object> showAddSettingPopup(@PathVariable int mode) {
                Map<String, Object> result = new HashMap<String, Object>();
        try {

            List<StandardVariableReference> standardVariableList =
                    fieldbookService.filterStandardVariablesForSetting(mode, getSettingDetailList(mode));

            try {
                if (userSelection.getTraitRefList() == null) {
                    List<TraitClassReference> traitRefList = (List<TraitClassReference>)
                            ontologyService.getAllTraitGroupsHierarchy(true);
                    userSelection.setTraitRefList(traitRefList);
                }

                List<TraitClassReference> traitRefList = userSelection.getTraitRefList();

                //we convert it to map so that it would be easier to chekc if there is a record or not
                HashMap<String, StandardVariableReference> mapVariableRef = new HashMap<String, StandardVariableReference>();
                if (standardVariableList != null && !standardVariableList.isEmpty()) {
                    for (StandardVariableReference varRef : standardVariableList) {
                        mapVariableRef.put(varRef.getId().toString(), varRef);
                    }
                }

                String treeData = TreeViewUtil.convertOntologyTraitsToJson(traitRefList, mapVariableRef);
                String searchTreeData = TreeViewUtil.convertOntologyTraitsToSearchSingleLevelJson(traitRefList, mapVariableRef);
                result.put("treeData", treeData);
                result.put("searchTreeData", searchTreeData);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Show variable details.
     *
     * @param id the id
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "showVariableDetails/{id}", method = RequestMethod.GET)
    public String showVariableDetails(@PathVariable int id) {
        try {

            SettingVariable svar = getSettingVariable(id);
            if (svar != null) {
                ObjectMapper om = new ObjectMapper();
                return om.writeValueAsString(svar);
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "[]";
    }
    
    /**
     * Adds the settings.
     *
     * @param form the form
     * @param model the model
     * @param mode the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST, headers = {"Content-type=application/json"})
    public String addSettings(@RequestBody CreateNurseryForm form, Model model, @PathVariable int mode) {
        List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
        try {
            List<SettingVariable> selectedVariables = form.getSelectedVariables();
            if (selectedVariables != null && !selectedVariables.isEmpty()) {
                for (SettingVariable var : selectedVariables) {
                    Operation operation = removeVarFromDeletedList(var, mode);

                    var.setOperation(operation);
                    populateSettingVariable(var);
                    List<ValueReference> possibleValues =
                            fieldbookService.getAllPossibleValues(var.getCvTermId());
                    SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
                    List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProject().getUniqueID());
                    newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
                    newSetting.setPossibleValuesToJson(possibleValues);
                    newSetting.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                    newSettings.add(newSetting);
                }
            }

            if (newSettings != null && !newSettings.isEmpty()) {
                return addNewSettingDetails(form, mode, newSettings);
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return "[]";
    }
    
    private Operation removeVarFromDeletedList(SettingVariable var, int mode) {
        List<SettingDetail> settingsList = new ArrayList<SettingDetail>();
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            settingsList = userSelection.getDeletedStudyLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            settingsList = userSelection.getDeletedPlotLevelList();
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()){
            settingsList = userSelection.getDeletedBaselineTraitsList();
        } else if (mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()){
            settingsList = userSelection.getDeletedNurseryConditions();
        }
        
        Operation operation = Operation.ADD;
        if (settingsList != null) {
            Iterator<SettingDetail> iter = settingsList.iterator();
            while (iter.hasNext()) {
                SettingVariable deletedVariable = iter.next().getVariable();
                if (deletedVariable.getCvTermId().equals(Integer.valueOf(var.getCvTermId()))) {
                    operation = deletedVariable.getOperation();
                    iter.remove();
                }
            }
        }        
        return operation;
    }
    
    /**
     * Clear settings.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(value = "/clearSettings", method = RequestMethod.GET)
    public String clearSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
                Model model, HttpSession session, HttpServletRequest request) {
    	
    	String contextParams = ContextUtil.getContextParameterString(request);
        try {
            form.setProjectId(this.getCurrentProjectId());
            setFormStaticData(form, contextParams);
            assignDefaultValues(form);
            form.setMeasurementRowList(new ArrayList<MeasurementRow>());
        } catch(Exception e) {
                LOG.error(e.getMessage(), e);
        }
        
        model.addAttribute("createNurseryForm", form);
        model.addAttribute("nurseryList", getNurseryList());
        
        return super.showAjaxPage(model, URL_SETTINGS);
    }
    
    /**
     * Adds the new setting details.
     *
     * @param form the form
     * @param mode the mode
     * @param newDetails the new details
     * @return the string
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     * @throws JsonIoException 
     * @throws Exception the exception
     */
    private String addNewSettingDetails(CreateNurseryForm form, int mode
            , List<SettingDetail> newDetails) throws JsonGenerationException, JsonMappingException, JsonIoException {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            if (form.getStudyLevelVariables() == null) {
                form.setStudyLevelVariables(newDetails);
            } else {
                form.getStudyLevelVariables().addAll(newDetails);
            }
            if (userSelection.getStudyLevelConditions() == null) {
                userSelection.setStudyLevelConditions(newDetails);
            } else {
                userSelection.getStudyLevelConditions().addAll(newDetails);
            }
            
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            if (form.getPlotLevelVariables() == null) {
                form.setPlotLevelVariables(newDetails);
            } else {
                form.getPlotLevelVariables().addAll(newDetails);
            }
            if (userSelection.getPlotsLevelList() == null) {
                userSelection.setPlotsLevelList(newDetails);
            } else {
                userSelection.getPlotsLevelList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()){
            if (form.getBaselineTraitVariables() == null) {
                form.setBaselineTraitVariables(newDetails);
            } else {
                form.getBaselineTraitVariables().addAll(newDetails);
            }
            if (userSelection.getBaselineTraitsList() == null) {
                userSelection.setBaselineTraitsList(newDetails);
            } else {
                userSelection.getBaselineTraitsList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            if (form.getSelectionVariatesVariables() == null) {
                form.setSelectionVariatesVariables(newDetails);
            } else {
                form.getSelectionVariatesVariables().addAll(newDetails);
            }
            if (userSelection.getSelectionVariates() == null) {
                userSelection.setSelectionVariates(newDetails);
            } else {
                userSelection.getSelectionVariates().addAll(newDetails);
            }
        } else {
            if (form.getNurseryConditions() == null) {
                form.setNurseryConditions(newDetails);
            } else {
                form.getNurseryConditions().addAll(newDetails);
            }
            if (userSelection.getNurseryConditions() == null) {
                userSelection.setNurseryConditions(newDetails);
            } else {
                userSelection.getNurseryConditions().addAll(newDetails);
            }
        }
        ObjectMapper om = new ObjectMapper();
        String jsonData = "";
        try {
        	jsonData = om.writeValueAsString(newDetails);
        } catch(IOException e) {
        	throw new JsonIoException(e.getMessage(), e);
        }
        return jsonData;
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteVariable/{mode}/{variableIds}", method = RequestMethod.POST)
    public String deleteVariable(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int mode, @PathVariable String variableIds) {
    	List<Integer> varIdList = SettingsUtil.parseVariableIds(variableIds);
    	Map<String, String> idNameRetrieveSaveMap = fieldbookService.getIdNamePairForRetrieveAndSave();
    	for(Integer variableId : varIdList) {
	        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
	
	            addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode, variableId);
	            deleteVariableInSession(userSelection.getStudyLevelConditions(), variableId);
	            if(idNameRetrieveSaveMap.get(variableId) != null){
	            	//special case so we must delete it as well
	            	addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode, Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
	                deleteVariableInSession(userSelection.getStudyLevelConditions(), Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
	            }
	        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
	            addVariableInDeletedList(userSelection.getPlotsLevelList(), mode, variableId);
	            deleteVariableInSession(userSelection.getPlotsLevelList(), variableId);
	        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()){
	            addVariableInDeletedList(userSelection.getBaselineTraitsList(), mode, variableId);
	            deleteVariableInSession(userSelection.getBaselineTraitsList(), variableId);
	        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()){
	            addVariableInDeletedList(userSelection.getSelectionVariates(), mode, variableId);
	            deleteVariableInSession(userSelection.getSelectionVariates(), variableId);
	        } else {
	            addVariableInDeletedList(userSelection.getNurseryConditions(), mode, variableId);
	            deleteVariableInSession(userSelection.getNurseryConditions(), variableId);
	        }
    	}
        return "";
    }
    
    private void addVariableInDeletedList(List<SettingDetail> currentList, int mode, int variableId) {
        SettingDetail newSetting = null;
        for (SettingDetail setting : currentList) {
            if (setting.getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
                newSetting = setting;
            }
        }
        
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            if (userSelection.getDeletedStudyLevelConditions() == null) {
                userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
            } 
            userSelection.getDeletedStudyLevelConditions().add(newSetting);            
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            if (userSelection.getDeletedPlotLevelList() == null) {
                userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
            } 
            userSelection.getDeletedPlotLevelList().add(newSetting);
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
            if (userSelection.getDeletedBaselineTraitsList() == null) {
                userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
            } 
            userSelection.getDeletedBaselineTraitsList().add(newSetting);
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            if (userSelection.getDeletedBaselineTraitsList() == null) {
                userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
            } 
            userSelection.getDeletedBaselineTraitsList().add(newSetting);
        } else if (mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
            if (userSelection.getDeletedNurseryConditions() == null) {
                userSelection.setDeletedNurseryConditions(new ArrayList<SettingDetail>());
            } 
            userSelection.getDeletedNurseryConditions().add(newSetting);
        }
    }
    
    private void deleteVariableInSession(List<SettingDetail> variableList, int variableId) {
        Iterator<SettingDetail> iter = variableList.iterator();
        while (iter.hasNext()) {
            if (iter.next().getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
                iter.remove();
            }
        }
    }

    @ModelAttribute("programLocationURL")
    public String getProgramLocation() {
        return fieldbookProperties.getProgramLocationsUrl();
    }
    
    @ModelAttribute("programMethodURL")
    public String getProgramMethod() {
        return fieldbookProperties.getProgramBreedingMethodsUrl();
    }

    @ModelAttribute("projectID")
    public String getProgramID() {
        return getCurrentProjectId();
    }
    
    @RequestMapping(value="/refresh/settings/tab", method = RequestMethod.GET)
	    public String refreshSettingsTab(@ModelAttribute("createNurseryForm") CreateNurseryForm form
	            , Model model, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException{
    	    	
    	    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	    	String contextParams = ContextUtil.getContextParameterString(contextInfo);

    	    	Workbook workbook = userSelection.getWorkbook();
                userSelection.setConstantsWithLabels(workbook.getConstants());                
                
                
                if(userSelection.getStudyLevelConditions() != null){
                	for(SettingDetail detail : userSelection.getStudyLevelConditions()){
                		MeasurementVariable var = WorkbookUtil.getMeasurementVariable(workbook.getConditions(), detail.getVariable().getCvTermId());
                		setSettingDetailsValueFromVariable(var, detail);                		
                	}
                }
                
                if(userSelection.getNurseryConditions() != null){
                	for(SettingDetail detail : userSelection.getNurseryConditions()){
                		MeasurementVariable var = WorkbookUtil.getMeasurementVariable(workbook.getConstants(), detail.getVariable().getCvTermId());
                		if(var != null){
                			detail.setValue(var.getValue());
                		}
                	}
                }
                
                form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
                form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
                form.setSelectionVariatesVariables(userSelection.getSelectionVariates());
                form.setPlotLevelVariables(userSelection.getPlotsLevelList());
                form.setNurseryConditions(userSelection.getNurseryConditions());
                form.setLoadSettings("1");            
                form.setProjectId(this.getCurrentProjectId());
                
                form.setMeasurementRowList(new ArrayList<MeasurementRow>());
    	            
    	        model.addAttribute("createNurseryForm", form);
    	        setFormStaticData(form, contextParams);
    	        return super.showAjaxPage(model, URL_SETTINGS);
    	    }
    
    protected void setSettingDetailsValueFromVariable(MeasurementVariable var, SettingDetail detail) throws MiddlewareQueryException{
    	if (var.getTermId() == TermId.BREEDING_METHOD_CODE.getId()
                && var.getValue() != null && !var.getValue().isEmpty()) {
            //set the value of code to ID for it to be selected in the popup
        	Method method = fieldbookMiddlewareService.getMethodByCode(var.getValue());
        	if(method != null){
        		detail.setValue(String.valueOf(method.getMid()));
        	}else{
        		detail.setValue("");
        	}
        }else if (var.getTermId() == TermId.LOCATION_ID.getId()){
        	setLocationVariableValue(detail, var);
        }else if(var != null){
        	String currentVal = var.getValue();
        	if(var.getTermId() != TermId.NURSERY_TYPE.getId() && (detail.getPossibleValues() == null || detail.getPossibleValues().isEmpty())){
        		detail.setValue(currentVal);
        	}else{
        		//special case for nursery type
        		if(var.getValue() != null && detail.getPossibleValues() != null){
        			
        			for (ValueReference possibleValue : detail.getPossibleValues()) {
        	    		if (var.getValue().equalsIgnoreCase(possibleValue.getDescription())) {
        	    			detail.setValue(possibleValue.getId().toString());
        	    			break;
        	    		}
        	    	}
        		}                        		
        	}
		}
    }
    protected void setLocationVariableValue(SettingDetail detail, MeasurementVariable var) throws MiddlewareQueryException{
    	int locationId =  var.getValue() != null && !var.getValue().isEmpty() && NumberUtils.isNumber(var.getValue()) ? Integer.valueOf(var.getValue()) : 0;
    	Location location = fieldbookMiddlewareService.getLocationById(locationId);
    	if(location != null) {
    		detail.setValue(String.valueOf(location.getLocid()));
    	} else {
    		detail.setValue("");
    	}	   
    }
    
    protected void setFieldbookMiddlewareService(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService){
    	this.fieldbookMiddlewareService = fieldbookMiddlewareService;
    }
    
    protected void setFieldbookService(FieldbookService fieldbookService) {
    	this.fieldbookService = fieldbookService;
    }
    
    protected void setUserSelection(UserSelection userSelection) {
    	this.userSelection = userSelection;
    }
}
