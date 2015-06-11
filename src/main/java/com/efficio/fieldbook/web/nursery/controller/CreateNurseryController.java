/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package com.efficio.fieldbook.web.nursery.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.util.JsonIoException;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

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
   
	/*
	 * (non-Javadoc)
	 * 
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
	@RequestMapping(value = "/nursery/{nurseryId}", method = RequestMethod.GET)
	public String useExistingNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @PathVariable int nurseryId, Model model,
			HttpSession session, HttpServletRequest request) throws MiddlewareQueryException {
    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);

		try {
			if (nurseryId != 0) {
				Workbook workbook = this.fieldbookMiddlewareService.getStudyVariableSettings(nurseryId, true);
				this.userSelection.setConstantsWithLabels(workbook.getConstants());
				this.fieldbookService.createIdNameVariablePairs(workbook, new ArrayList<SettingDetail>(),
						AppConstants.ID_NAME_COMBINATION.getString(), false);
	
				Dataset dataset = (Dataset) SettingsUtil.convertWorkbookToXmlDataset(workbook);
				SettingsUtil.convertXmlDatasetToPojo(this.fieldbookMiddlewareService, this.fieldbookService, dataset, this.userSelection,
						this.getCurrentProject().getUniqueID(), true, false);
	            
				// nursery-level
				List<SettingDetail> nurseryLevelConditions =
						this.updateRequiredFields(this.buildVariableIDList(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
								this.buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), true),
								this.buildRequiredVariablesFlag(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()),
								this.userSelection.getStudyLevelConditions(), false,
								AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
				this.removeBasicDetailsVariables(nurseryLevelConditions);
	            
				// plot-level
				List<SettingDetail> plotLevelConditions =
						this.updateRequiredFields(this.buildVariableIDList(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
								this.buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false),
								this.buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
								this.userSelection.getPlotsLevelList(), false, "");
	            
				// remove variables not needed
				this.removeVariablesFromExistingNursery(plotLevelConditions, AppConstants.REMOVE_FACTORS_IN_USE_PREVIOUS_STUDY.getString());
	            
				this.userSelection.setStudyLevelConditions(nurseryLevelConditions);
				this.userSelection.setPlotsLevelList(plotLevelConditions);
	            
				form.setStudyLevelVariables(this.userSelection.getStudyLevelConditions());
				form.setBaselineTraitVariables(this.userSelection.getBaselineTraitsList());
				form.setSelectionVariatesVariables(this.userSelection.getSelectionVariates());
				form.setPlotLevelVariables(this.userSelection.getPlotsLevelList());
				form.setNurseryConditions(this.userSelection.getNurseryConditions());
	            form.setLoadSettings("1");            
	            form.setProjectId(this.getCurrentProjectId());
	            
	            form.setMeasurementRowList(new ArrayList<MeasurementRow>());
				this.setFormStaticData(form, contextParams);
	        }
    	} catch (MiddlewareQueryException e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
			this.addErrorMessageToResult(form, e);
    	}
        model.addAttribute("createNurseryForm", form);
        
		return super.showAjaxPage(model, CreateNurseryController.URL_SETTINGS);
    }
    
	@RequestMapping(value = "/nursery/getChecks/{nurseryId}", method = RequestMethod.GET)
	public String getChecksForUseExistingNursery(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form,
			@PathVariable int nurseryId, Model model, HttpSession session, HttpServletRequest request) throws MiddlewareQueryException {
		if (this.userSelection.getRemovedConditions() != null) {
    		CreateNurseryForm createNurseryForm = new CreateNurseryForm();
			List<SettingDetail> checkVariables = this.getCheckVariables(this.userSelection.getRemovedConditions(), createNurseryForm);
	        form.setCheckVariables(checkVariables);
    	}
    	
    	model.addAttribute("importGermplasmListForm", form);
    	
		return super.showAjaxPage(model, CreateNurseryController.URL_CHECKS);
    }
    
    protected void addErrorMessageToResult(CreateNurseryForm form, MiddlewareQueryException e) {
    	String param = AppConstants.NURSERY.getString();
		form.setHasError("1");
		form.setErrorMessage(this.errorHandlerService.getErrorMessagesAsString(e.getCode(), new Object[] {param,
				param.substring(0, 1).toUpperCase().concat(param.substring(1, param.length())), param}, "\n"));
    }
    
    private void removeBasicDetailsVariables(List<SettingDetail> nurseryLevelConditions) {
        Iterator<SettingDetail> iter = nurseryLevelConditions.iterator();
        while (iter.hasNext()) {
			if (this.inFixedNurseryList(iter.next().getVariable().getCvTermId())) {
                iter.remove();
            }
        }
    }
    
    private boolean inFixedNurseryList(int propertyId) {
        StringTokenizer token = new StringTokenizer(AppConstants.FIXED_NURSERY_VARIABLES.getString(), ",");
		while (token.hasMoreTokens()) {
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
			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpSession session,
			HttpServletRequest request) throws MiddlewareQueryException {
    	    	
    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	String contextParams = ContextUtil.getContextParameterString(contextInfo);    	
		SessionUtility.clearSessionData(session, new String[] {SessionUtility.USER_SELECTION_SESSION_NAME,
				SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    	form.setProjectId(this.getCurrentProjectId());
		this.setFormStaticData(form, contextParams);
		this.assignDefaultValues(form);
    	form.setMeasurementRowList(new ArrayList<MeasurementRow>());
    	
		// create check variables for specify checks
		this.setCheckVariablesInForm(form2);
    	
    	return super.show(model);
    }
    
    protected void setCheckVariablesInForm(ImportGermplasmListForm form2) throws MiddlewareQueryException {
    	List<SettingDetail> checkVariables = new ArrayList<SettingDetail>();
		checkVariables =
				this.buildDefaultVariables(checkVariables, AppConstants.CHECK_VARIABLES.getString(),
						this.buildRequiredVariablesLabel(AppConstants.CHECK_VARIABLES.getString(), false));
        form2.setCheckVariables(checkVariables);
		this.userSelection.setRemovedConditions(checkVariables);
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
        
		basicDetails =
				this.buildDefaultVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString(),
						this.buildRequiredVariablesLabel(AppConstants.FIXED_NURSERY_VARIABLES.getString(), false));
        form.setBasicDetails(basicDetails);
        form.setStudyLevelVariables(nurseryDefaults);
        form.setPlotLevelVariables(plotDefaults);
		nurseryDefaults =
				this.buildDefaultVariables(nurseryDefaults, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(),
						this.buildRequiredVariablesLabel(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString(), false));
		plotDefaults =
				this.buildDefaultVariables(plotDefaults, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(),
						this.buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false));
        
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
        //get the name of the nursery
        String name = null;
    	for (SettingDetail nvar : form.getBasicDetails()) {
			if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null
					&& nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}

    	List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
    	if (form.getStudyLevelVariables() != null && !form.getStudyLevelVariables().isEmpty()) {
    		studyLevelVariables.addAll(form.getStudyLevelVariables());
    	}
    	
    	studyLevelVariables.addAll(form.getBasicDetails());
    	
    	addStudyLevelVariablesFromUserSelectionIfNecessary(studyLevelVariables, userSelection);
    	
		this.addNurseryTypeFromDesignImport(studyLevelVariables);
		this.addExperimentalDesignTypeFromDesignImport(studyLevelVariables);
    	 
		List<SettingDetail> studyLevelVariablesSession = this.userSelection.getBasicDetails();
		this.userSelection.getStudyLevelConditions().addAll(studyLevelVariablesSession);
    	
    	List<SettingDetail> baselineTraits = form.getBaselineTraitVariables();
		List<SettingDetail> baselineTraitsSession = this.userSelection.getSelectionVariates();
    	if (baselineTraits == null && form.getSelectionVariatesVariables() != null) {
    	    baselineTraits = form.getSelectionVariatesVariables();
			this.userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	} else if (form.getSelectionVariatesVariables() != null) {
    	    baselineTraits.addAll(form.getSelectionVariatesVariables());
			this.userSelection.getBaselineTraitsList().addAll(baselineTraitsSession);
    	}
    	
		Dataset dataset =
				(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, studyLevelVariables,
						form.getPlotLevelVariables(), baselineTraits, this.userSelection, form.getNurseryConditions());
		SettingsUtil.setConstantLabels(dataset, this.userSelection.getConstantsWithLabels());
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
		this.userSelection.setWorkbook(workbook);

    	this.createStudyDetails(workbook, form.getBasicDetails(), form.getFolderId(), null);
 
    	return "success";
    }
            
    private void addStudyLevelVariablesFromUserSelectionIfNecessary(List<SettingDetail> studyLevelVariables,
			UserSelection userSelection) {
		
    	for (SettingDetail settingDetailFromUserSelection : userSelection.getStudyLevelConditions()){
    		
    		boolean settingDetailExists = false;
    		
    		for (SettingDetail settingDetail : studyLevelVariables){
    			if (settingDetail.getVariable().getCvTermId().intValue() == settingDetailFromUserSelection.getVariable().getCvTermId().intValue()){
    				settingDetailExists = true;
    				break;
    			}
    		}
    		
    		if (!settingDetailExists){
    			studyLevelVariables.add(settingDetailFromUserSelection);
    		}
    		
    	}
    	
		
	}

	private void addNurseryTypeFromDesignImport(List<SettingDetail> studyLevelVariables) {
		
    	SettingDetail nurseryTypeSettingDetail = new SettingDetail();
    	SettingVariable nurseryTypeSettingVariable = new SettingVariable();
    	
		Integer nurseryTypeValue = this.userSelection.getNurseryTypeForDesign();
    	
    	nurseryTypeSettingDetail.setValue(String.valueOf(nurseryTypeValue));
    	nurseryTypeSettingVariable.setCvTermId(TermId.NURSERY_TYPE.getId());
    	nurseryTypeSettingVariable.setName("NURSERY_TYPE");
    	nurseryTypeSettingVariable.setOperation(Operation.ADD);
    	nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);
    	
		if (this.userSelection.getNurseryTypeForDesign() != null && nurseryTypeValue != null) {
   
			for (SettingDetail settingDetail : studyLevelVariables) {
				if (settingDetail.getVariable().getCvTermId() == TermId.NURSERY_TYPE.getId()) {
        			settingDetail.setValue(String.valueOf(nurseryTypeValue));
        			settingDetail.getVariable().setName("NURSERY_TYPE");
					this.userSelection.setNurseryTypeForDesign(null);
        			return;
        		}
        	}
    		
        	studyLevelVariables.add(nurseryTypeSettingDetail);
    	}
    	
		this.userSelection.setNurseryTypeForDesign(null);
    	
	}
    
    private void addExperimentalDesignTypeFromDesignImport(List<SettingDetail> studyLevelVariables) {
		
    	SettingDetail nurseryTypeSettingDetail = new SettingDetail();
    	SettingVariable nurseryTypeSettingVariable = new SettingVariable();
    	
    	nurseryTypeSettingDetail.setValue(String.valueOf(TermId.OTHER_DESIGN.getId()));
    	nurseryTypeSettingVariable.setCvTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
    	nurseryTypeSettingVariable.setName("EXPERIMENTAL_DESIGN");
    	nurseryTypeSettingVariable.setOperation(Operation.ADD);
    	nurseryTypeSettingDetail.setVariable(nurseryTypeSettingVariable);
    	
		if (this.userSelection.getExpDesignVariables() != null && !this.userSelection.getExpDesignVariables().isEmpty()) {
   
			for (SettingDetail settingDetail : studyLevelVariables) {
				if (settingDetail.getVariable().getCvTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
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
	private void setFormStaticData(CreateNurseryForm form, String contextParams) {

        // TODO move the translation of static data from form field into either the use of page model, or via Thymeleaf static evaluation

        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
		form.setBreedingMethodUrl(this.fieldbookProperties.getProgramBreedingMethodsUrl());
		form.setImportLocationUrl(this.fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams);
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
		form.setOpenGermplasmUrl(this.fieldbookProperties.getGermplasmDetailsUrl());
    	form.setBaselineTraitsSegment(AppConstants.SEGMENT_TRAITS.getString());
        form.setSelectionVariatesSegment(AppConstants.SEGMENT_SELECTION_VARIATES.getString());
        form.setIdNameVariables(AppConstants.ID_NAME_COMBINATION.getString());
		form.setRequiredFields(AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString() + ","
				+ AppConstants.FIXED_NURSERY_VARIABLES.getString());
        form.setBreedingMethodCode(AppConstants.BREEDING_METHOD_CODE.getString());
        try {
			form.setCreatedBy(this.fieldbookService.getPersonByUserId(this.getCurrentIbdbUserId()));
        } catch (MiddlewareQueryException e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
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
			return this.userSelection.getStudyLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
			return this.userSelection.getPlotsLevelList();
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
            List<SettingDetail> newList = new ArrayList<SettingDetail>();
            
			for (SettingDetail setting : this.userSelection.getBaselineTraitsList()) {
                newList.add(setting);
            }
            
			for (SettingDetail setting : this.userSelection.getNurseryConditions()) {
                newList.add(setting);
            }
                
            return newList;
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
			return this.userSelection.getSelectionVariates();
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
					this.fieldbookService.filterStandardVariablesForSetting(mode, this.getSettingDetailList(mode));

            try {
				if (this.userSelection.getTraitRefList() == null) {
					List<TraitClassReference> traitRefList = this.ontologyService.getAllTraitGroupsHierarchy(true);
					this.userSelection.setTraitRefList(traitRefList);
                }

				List<TraitClassReference> traitRefList = this.userSelection.getTraitRefList();

				// we convert it to map so that it would be easier to chekc if there is a record or not
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
				CreateNurseryController.LOG.error(e.getMessage());
            }
        } catch (Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
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

			SettingVariable svar = this.getSettingVariable(id);
            if (svar != null) {
                ObjectMapper om = new ObjectMapper();
                return om.writeValueAsString(svar);
            }

        } catch (Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
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
					Operation operation = this.removeVarFromDeletedList(var, mode);

                    var.setOperation(operation);
					this.populateSettingVariable(var);
					List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(var.getCvTermId());
                    SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
					List<ValueReference> possibleValuesFavorite =
							this.fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProject().getUniqueID());
                    newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
                    newSetting.setPossibleValuesToJson(possibleValues);
                    newSetting.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                    newSettings.add(newSetting);
                }
            }

            if (newSettings != null && !newSettings.isEmpty()) {
				return this.addNewSettingDetails(form, mode, newSettings);
            }

        } catch (Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
        }

        return "[]";
    }
    
    private Operation removeVarFromDeletedList(SettingVariable var, int mode) {
        List<SettingDetail> settingsList = new ArrayList<SettingDetail>();
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
			settingsList = this.userSelection.getDeletedStudyLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
			settingsList = this.userSelection.getDeletedPlotLevelList();
		} else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
			settingsList = this.userSelection.getDeletedBaselineTraitsList();
		} else if (mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
			settingsList = this.userSelection.getDeletedNurseryConditions();
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
	public String clearSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, HttpSession session,
			HttpServletRequest request) {
    	
    	String contextParams = ContextUtil.getContextParameterString(request);
        try {
            form.setProjectId(this.getCurrentProjectId());
			this.setFormStaticData(form, contextParams);
			this.assignDefaultValues(form);
            form.setMeasurementRowList(new ArrayList<MeasurementRow>());
		} catch (Exception e) {
			CreateNurseryController.LOG.error(e.getMessage(), e);
        }
        
        model.addAttribute("createNurseryForm", form);
		model.addAttribute("nurseryList", this.getNurseryList());
        
		return super.showAjaxPage(model, CreateNurseryController.URL_SETTINGS);
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
	private String addNewSettingDetails(CreateNurseryForm form, int mode, List<SettingDetail> newDetails) throws JsonGenerationException,
			JsonMappingException, JsonIoException {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            if (form.getStudyLevelVariables() == null) {
                form.setStudyLevelVariables(newDetails);
            } else {
                form.getStudyLevelVariables().addAll(newDetails);
            }
			if (this.userSelection.getStudyLevelConditions() == null) {
				this.userSelection.setStudyLevelConditions(newDetails);
            } else {
				this.userSelection.getStudyLevelConditions().addAll(newDetails);
            }
            
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            if (form.getPlotLevelVariables() == null) {
                form.setPlotLevelVariables(newDetails);
            } else {
                form.getPlotLevelVariables().addAll(newDetails);
            }
			if (this.userSelection.getPlotsLevelList() == null) {
				this.userSelection.setPlotsLevelList(newDetails);
            } else {
				this.userSelection.getPlotsLevelList().addAll(newDetails);
            }
		} else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
            if (form.getBaselineTraitVariables() == null) {
                form.setBaselineTraitVariables(newDetails);
            } else {
                form.getBaselineTraitVariables().addAll(newDetails);
            }
			if (this.userSelection.getBaselineTraitsList() == null) {
				this.userSelection.setBaselineTraitsList(newDetails);
            } else {
				this.userSelection.getBaselineTraitsList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            if (form.getSelectionVariatesVariables() == null) {
                form.setSelectionVariatesVariables(newDetails);
            } else {
                form.getSelectionVariatesVariables().addAll(newDetails);
            }
			if (this.userSelection.getSelectionVariates() == null) {
				this.userSelection.setSelectionVariates(newDetails);
            } else {
				this.userSelection.getSelectionVariates().addAll(newDetails);
            }
        } else {
            if (form.getNurseryConditions() == null) {
                form.setNurseryConditions(newDetails);
            } else {
                form.getNurseryConditions().addAll(newDetails);
            }
			if (this.userSelection.getNurseryConditions() == null) {
				this.userSelection.setNurseryConditions(newDetails);
            } else {
				this.userSelection.getNurseryConditions().addAll(newDetails);
            }
        }
        ObjectMapper om = new ObjectMapper();
        String jsonData = "";
        try {
        	jsonData = om.writeValueAsString(newDetails);
		} catch (IOException e) {
        	throw new JsonIoException(e.getMessage(), e);
        }
        return jsonData;
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteVariable/{mode}/{variableIds}", method = RequestMethod.POST)
	public String deleteVariable(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, @PathVariable int mode,
			@PathVariable String variableIds) {
    	List<Integer> varIdList = SettingsUtil.parseVariableIds(variableIds);
		Map<String, String> idNameRetrieveSaveMap = this.fieldbookService.getIdNamePairForRetrieveAndSave();
		for (Integer variableId : varIdList) {
	        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
	
				this.addVariableInDeletedList(this.userSelection.getStudyLevelConditions(), mode, variableId);
				this.deleteVariableInSession(this.userSelection.getStudyLevelConditions(), variableId);
				if (idNameRetrieveSaveMap.get(variableId) != null) {
					// special case so we must delete it as well
					this.addVariableInDeletedList(this.userSelection.getStudyLevelConditions(), mode,
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
					this.deleteVariableInSession(this.userSelection.getStudyLevelConditions(),
							Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
	            }
	        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
				this.addVariableInDeletedList(this.userSelection.getPlotsLevelList(), mode, variableId);
				this.deleteVariableInSession(this.userSelection.getPlotsLevelList(), variableId);
			} else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
				this.addVariableInDeletedList(this.userSelection.getBaselineTraitsList(), mode, variableId);
				this.deleteVariableInSession(this.userSelection.getBaselineTraitsList(), variableId);
			} else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
				this.addVariableInDeletedList(this.userSelection.getSelectionVariates(), mode, variableId);
				this.deleteVariableInSession(this.userSelection.getSelectionVariates(), variableId);
	        } else {
				this.addVariableInDeletedList(this.userSelection.getNurseryConditions(), mode, variableId);
				this.deleteVariableInSession(this.userSelection.getNurseryConditions(), variableId);
	        }
    	}
        return "";
    }
    
    private void addVariableInDeletedList(List<SettingDetail> currentList, int mode, int variableId) {
        this.addVariableInDeletedList(currentList, mode, variableId, false);
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
		return this.fieldbookProperties.getProgramLocationsUrl();
    }
    
    @ModelAttribute("programMethodURL")
    public String getProgramMethod() {
		return this.fieldbookProperties.getProgramBreedingMethodsUrl();
    }

    @ModelAttribute("projectID")
    public String getProgramID() {
		return this.getCurrentProjectId();
    }
    
	@RequestMapping(value = "/refresh/settings/tab", method = RequestMethod.GET)
	public String refreshSettingsTab(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, HttpSession session,
			HttpServletRequest request) throws MiddlewareQueryException {
    	    	
    	    	ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO); 
    	    	String contextParams = ContextUtil.getContextParameterString(contextInfo);

		Workbook workbook = this.userSelection.getWorkbook();
		this.userSelection.setConstantsWithLabels(workbook.getConstants());
                
		if (this.userSelection.getStudyLevelConditions() != null) {
			for (SettingDetail detail : this.userSelection.getStudyLevelConditions()) {
                		MeasurementVariable var = WorkbookUtil.getMeasurementVariable(workbook.getConditions(), detail.getVariable().getCvTermId());
				this.setSettingDetailsValueFromVariable(var, detail);
                	}
                }
                
		if (this.userSelection.getNurseryConditions() != null) {
			for (SettingDetail detail : this.userSelection.getNurseryConditions()) {
                		MeasurementVariable var = WorkbookUtil.getMeasurementVariable(workbook.getConstants(), detail.getVariable().getCvTermId());
				if (var != null) {
                			detail.setValue(var.getValue());
                		}
                	}
                }
                
		form.setStudyLevelVariables(this.userSelection.getStudyLevelConditions());
		form.setBaselineTraitVariables(this.userSelection.getBaselineTraitsList());
		form.setSelectionVariatesVariables(this.userSelection.getSelectionVariates());
		form.setPlotLevelVariables(this.userSelection.getPlotsLevelList());
		form.setNurseryConditions(this.userSelection.getNurseryConditions());
                form.setLoadSettings("1");            
                form.setProjectId(this.getCurrentProjectId());
                
                form.setMeasurementRowList(new ArrayList<MeasurementRow>());
    	            
    	        model.addAttribute("createNurseryForm", form);
		this.setFormStaticData(form, contextParams);
		return super.showAjaxPage(model, CreateNurseryController.URL_SETTINGS);
    	    }
    
	protected void setSettingDetailsValueFromVariable(MeasurementVariable var, SettingDetail detail) throws MiddlewareQueryException {
		if (var.getTermId() == TermId.BREEDING_METHOD_CODE.getId() && var.getValue() != null && !var.getValue().isEmpty()) {
			// set the value of code to ID for it to be selected in the popup
			Method method = this.fieldbookMiddlewareService.getMethodByCode(var.getValue());
			if (method != null) {
        		detail.setValue(String.valueOf(method.getMid()));
			} else {
        		detail.setValue("");
        	}
		} else if (var.getTermId() == TermId.LOCATION_ID.getId()) {
			this.setLocationVariableValue(detail, var);
		} else if (var != null) {
        	String currentVal = var.getValue();
			if (var.getTermId() != TermId.NURSERY_TYPE.getId()
					&& (detail.getPossibleValues() == null || detail.getPossibleValues().isEmpty())) {
        		detail.setValue(currentVal);
			} else {
				// special case for nursery type
				if (var.getValue() != null && detail.getPossibleValues() != null) {
        			
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

	protected void setLocationVariableValue(SettingDetail detail, MeasurementVariable var) throws MiddlewareQueryException {
		int locationId =
				var.getValue() != null && !var.getValue().isEmpty() && NumberUtils.isNumber(var.getValue()) ? Integer.valueOf(var
						.getValue()) : 0;
		Location location = this.fieldbookMiddlewareService.getLocationById(locationId);
		if (location != null) {
    		detail.setValue(String.valueOf(location.getLocid()));
    	} else {
    		detail.setValue("");
    	}	   
    }
    
	protected void setFieldbookMiddlewareService(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
    	this.fieldbookMiddlewareService = fieldbookMiddlewareService;
    }
    
    protected void setFieldbookService(FieldbookService fieldbookService) {
    	this.fieldbookService = fieldbookService;
    }
    
	@Override
    protected void setUserSelection(UserSelection userSelection) {
    	this.userSelection = userSelection;
    }
}
