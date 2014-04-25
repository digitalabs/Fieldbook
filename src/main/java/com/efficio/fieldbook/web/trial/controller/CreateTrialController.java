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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.settings.TrialDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * The Class CreateTrialController.
 */
@Controller
@RequestMapping(CreateTrialController.URL)
public class CreateTrialController extends SettingsController {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(CreateTrialController.class);

    /** The Constant URL. */
    public static final String URL = "/TrialManager/createTrial";
    
    /** The Constant URL_SETTINGS. */
    public static final String URL_SETTINGS = "/TrialManager/chooseSettings";
    
    @Resource
    private TrialSelection trialSelection;
	
   
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "TrialManager/createTrial";
	}

    

    /**
     * Use existing Trial.
     *
     * @param form the form
     * @param trialId the Trial id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/trial/{trialId}", method = RequestMethod.GET)
    public String useExistingTrial(@ModelAttribute("manageSettingsForm") CreateTrialForm form, @PathVariable int trialId
            , Model model, HttpSession session) throws MiddlewareQueryException{
        if(trialId != 0){
            Workbook workbook = null;
            
            try { 
                workbook = fieldbookMiddlewareService.getTrialDataSet(trialId);
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
            
            trialSelection.setWorkbook(workbook);
            TrialDataset dataset = (TrialDataset)SettingsUtil.convertWorkbookToXmlDataset(workbook, false);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            
            //study-level
            List<SettingDetail> trialLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()), 
                    userSelection.getStudyLevelConditions(), true);
            
            //plot-level
            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()), 
                    userSelection.getPlotsLevelList(), false);
            
            //trial environment 
            List<SettingDetail> trialLevelVariableList = sortDefaultTrialVariables(updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()), 
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString(), true), 
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()), 
                    userSelection.getTrialLevelVariableList(), true));
            
            userSelection.setStudyLevelConditions(trialLevelConditions);
            userSelection.setPlotsLevelList(plotLevelConditions);
            userSelection.setTrialLevelVariableList(trialLevelVariableList);
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
            
            //build trial environment details
            List<List<ValueReference>> trialEnvList = createTrialEnvValueList(userSelection.getTrialLevelVariableList());
            form.setTrialEnvironmentValues(trialEnvList);
            form.setTrialInstances(workbook.getTotalNumberOfInstances());
            form.setLoadSettings("1");
            form.setRequiredFields(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());
        }
        setFormStaticData(form);
        model.addAttribute("createTrialForm", form);
        model.addAttribute("settingsTrialList", getTrialSettingsList());
        model.addAttribute("trialList", getTrialList());
        model.addAttribute("experimentalDesignValues", getExperimentalDesignValues());
        form.setDesignLayout(AppConstants.DESIGN_LAYOUT_INDIVIDUAL.getString());
        return super.showAjaxPage(model, URL_SETTINGS);
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
    public String show(@ModelAttribute("createTrialForm") CreateTrialForm form, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form2, Model model, HttpSession session) throws MiddlewareQueryException{
    	session.invalidate();
    	form.setProjectId(this.getCurrentProjectId());
    	form.setRequiredFields(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());
    	setFormStaticData(form);
    	return super.show(model);
    }

    /**
     * View settings.
     *
     * @param form the form
     * @param templateSettingId the template setting id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/view/{templateSettingId}", method = RequestMethod.POST)
    public String viewSettings(@ModelAttribute("createTrialForm") CreateTrialForm form, @PathVariable int templateSettingId, 
    	Model model, HttpSession session) throws MiddlewareQueryException{
    	if(templateSettingId != 0){    	
    	    //get settings of selected trial setting
            TemplateSetting templateSettingFilter = new TemplateSetting(Integer.valueOf(templateSettingId), Integer.valueOf(getCurrentProjectId()), null, getTrialTool(), null, null);
            templateSettingFilter.setIsDefaultToNull();
            List<TemplateSetting> templateSettings = workbenchService.getTemplateSettings(templateSettingFilter);
            TemplateSetting templateSetting = templateSettings.get(0); //always 1
            TrialDataset dataset = (TrialDataset)SettingsUtil.parseXmlToDatasetPojo(templateSetting.getConfiguration(), false);
            SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
            form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
            form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
            form.setPlotLevelVariables(userSelection.getPlotsLevelList());
            form.setTreatmentFactors(convertSettingDetailToTreatment(userSelection.getTreatmentFactors()));
            
            //add default trial variables such as experimental design, replicates, block size and block per replicate
            List<SettingDetail> trialLevelVariableList = sortDefaultTrialVariables(userSelection.getTrialLevelVariableList());
            form.setTrialLevelVariables(trialLevelVariableList);
            
            //create the matrix of trial environment variables
            List<List<ValueReference>> trialEnvList = createTrialEnvValueList(trialLevelVariableList, 1, true);
            form.setTrialEnvironmentValues(trialEnvList);
            
            //default and minimum no of trial instances is 1
            form.setTrialInstances(1);
            form.setSelectedSettingId(templateSetting.getTemplateSettingId());
    	}
    	form.setLoadSettings("1");
    	setFormStaticData(form);
        return super.showAjaxPage(model, URL_SETTINGS );
    }
    
    private List<TreatmentFactorDetail> convertSettingDetailToTreatment(List<SettingDetail> treatmentFactors) {
        List<TreatmentFactorDetail> newTreatmentFactors = new ArrayList<TreatmentFactorDetail>();
        int index = 0;
        for (SettingDetail settingDetail : treatmentFactors) {
            if (index%2 == 0) {
                newTreatmentFactors.add(new TreatmentFactorDetail(settingDetail.getVariable().getCvTermId(), 
                        treatmentFactors.get(index+1).getVariable().getCvTermId(), settingDetail.getValue(), 
                        treatmentFactors.get(index+1).getValue(), settingDetail.getVariable().getName(), 
                        treatmentFactors.get(index+1).getVariable().getName(), 
                        treatmentFactors.get(index+1).getVariable().getDataTypeId(),
                        treatmentFactors.get(index+1).getPossibleValuesJson()));
                index++;
            } else {
                index++;
                continue;
            }
        }
        return newTreatmentFactors;
    }
    
    private List<List<ValueReference>> createTrialEnvValueList(List<SettingDetail> trialLevelVariableList, int trialInstances, boolean addDefault) {
        List<List<ValueReference>> trialEnvValueList = new ArrayList<List<ValueReference>>();
        for (int i=0; i<trialInstances; i++) {
            List<ValueReference> trialInstanceVariables = new ArrayList<ValueReference>();
            for (SettingDetail detail : trialLevelVariableList) {
                if (detail.getVariable().getCvTermId() != null) {
                    //set value to empty except for trial instance no.
                    if (detail.getVariable().getCvTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
                        trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), String.valueOf(i+1)));
                    } else {
                        trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), ""));
                    }
                } else {
                    trialInstanceVariables.add(new ValueReference(0, ""));
                }
            }
            trialEnvValueList.add(trialInstanceVariables);
        }
        userSelection.setTrialEnvironmentValues(trialEnvValueList);
        return trialEnvValueList;
    }
    
    private List<List<ValueReference>> createTrialEnvValueList(List<SettingDetail> trialLevelVariableList) {
        List<List<ValueReference>> trialEnvValueList = new ArrayList<List<ValueReference>>();
        List<MeasurementRow> trialObservations = trialSelection.getWorkbook().getTrialObservations();        
        
        for (MeasurementRow trialObservation : trialObservations) {
            List<ValueReference> trialInstanceVariables = new ArrayList<ValueReference>();
            for (SettingDetail detail : trialLevelVariableList) {
                String headerName = WorkbookUtil.getMeasurementVariableName(trialSelection.getWorkbook().getTrialVariables(), detail.getVariable().getCvTermId());
                String value = trialObservation.getMeasurementDataValue(headerName);
                trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), value));
            }
            trialEnvValueList.add(trialInstanceVariables);
        }
        userSelection.setTrialEnvironmentValues(trialEnvValueList);
        return trialEnvValueList;
    }
    
    private List<SettingDetail> sortDefaultTrialVariables(List<SettingDetail> trialLevelVariableList) {
        //set orderBy
        StringTokenizer tokenOrder = new StringTokenizer(AppConstants.TRIAL_ENVIRONMENT_ORDER.getString(), ",");
        int i=0;
        int tokenSize = tokenOrder.countTokens();
        while (tokenOrder.hasMoreTokens()) {
            String variableId = tokenOrder.nextToken();
            for (SettingDetail settingDetail : trialLevelVariableList) {
                if (settingDetail.getVariable().getCvTermId().equals(Integer.parseInt(variableId))) {
                    settingDetail.setOrder((tokenSize-i)*-1);
                }
            }
            i++;
        }

        Collections.sort(trialLevelVariableList, new  Comparator<SettingDetail>() {
            @Override
            public int compare(SettingDetail o1, SettingDetail o2) {
                    return o1.getOrder() - o2.getOrder();
            }
        });
                
        return trialLevelVariableList;
    }
    
    private List<ValueReference> getPossibleValuesOfDefaultVariable(String variableName) {
        List<ValueReference> values = new ArrayList<ValueReference>();
        variableName = variableName.toUpperCase().replace(" ", "_");
        
        StringTokenizer token = new StringTokenizer(AppConstants.getString(variableName + AppConstants.VALUES.getString()), ",");
        
        int i = 0;
        while (token.hasMoreTokens()) {
            values.add(new ValueReference(i, token.nextToken()));
            i++;
        }
        
        return values;
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
    public String submit(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model) throws MiddlewareQueryException {
    	
    	String name = null;
    	for (SettingDetail nvar : form.getStudyLevelVariables()) {
    		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
    			name = nvar.getValue();
    			break;
    		}
    	}
    	
    	form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
    	TrialDataset dataset = (TrialDataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, form.getStudyLevelVariables(), 
    			form.getPlotLevelVariables(), form.getBaselineTraitVariables(), userSelection, form.getTrialLevelVariables());
    	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
    	userSelection.setWorkbook(workbook);

    	if (form.getDesignLayout() != null && form.getDesignLayout().equals(AppConstants.DESIGN_LAYOUT_SAME_FOR_ALL.getString())
    			&& form.getExperimentalDesignForAll() != null) {
    		
    		for (List<ValueReference> rowValues : form.getTrialEnvironmentValues()) {
    			for (ValueReference cellValue : rowValues) {
    				if (cellValue.getId().equals(TermId.EXPERIMENT_DESIGN_FACTOR.getId())) {
    					cellValue.setName(form.getExperimentalDesignForAll());
    				}
    			}
    		}
    	}

    	if (form.getTrialEnvironmentValues() != null && !form.getTrialEnvironmentValues().isEmpty()) {
    		userSelection.getWorkbook().setTrialObservations(WorkbookUtil.createMeasurementRows(form.getTrialEnvironmentValues(), workbook.getTrialVariables()));
    	}
		
     	userSelection.setTrialEnvironmentValues(form.getTrialEnvironmentValues());
    	
    	createStudyDetails(workbook, form.getStudyLevelVariables(), form.getFolderId());
 
    	return "success";
    }
    
    /**
     * Creates the study details.
     *
     * @param workbook the workbook
     * @param conditions the conditions
     * @param folderId the folder id
     */
    private void createStudyDetails(Workbook workbook, List<SettingDetail> conditions, Integer folderId) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();

        if (conditions != null && !conditions.isEmpty()) {
	        studyDetails.setTitle(getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
	        studyDetails.setObjective(getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
	        studyDetails.setStudyName(getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
	        studyDetails.setStudyType(StudyType.T);
	        
	        if (folderId != null) {
	        	studyDetails.setParentFolderId(folderId);
	        }
    	}
        studyDetails.print(1);
    }
    
    /**
     * Gets the setting detail value.
     *
     * @param details the details
     * @param termId the term id
     * @return the setting detail value
     */
    private String getSettingDetailValue(List<SettingDetail> details, int termId) {
    	String value = null;
    	
    	for (SettingDetail detail : details) {
    		if (detail.getVariable().getCvTermId().equals(termId)) {
    			value = detail.getValue();
    			break;
    		}
    	}
    	
    	return value;
    }
    
    /**
     * Sets the form static data.
     *
     * @param form the new form static data
     */
    private void setFormStaticData(CreateTrialForm form){
        form.setBreedingMethodId(AppConstants.BREEDING_METHOD_ID.getString());
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setBreedingMethodUrl(AppConstants.BREEDING_METHOD_URL.getString());
        form.setLocationUrl(AppConstants.LOCATION_URL.getString());
        form.setProjectId(this.getCurrentProjectId());
        form.setImportLocationUrl(AppConstants.IMPORT_GERMPLASM_URL.getString());
        form.setStudyNameTermId(AppConstants.STUDY_NAME_ID.getString());
        form.setStartDateId(AppConstants.START_DATE_ID.getString());
    	form.setEndDateId(AppConstants.END_DATE_ID.getString());
    	form.setTrialInstanceFactor(AppConstants.TRIAL_INSTANCE_FACTOR.getString());
    	form.setReplicates(AppConstants.REPLICATES.getString());
    	form.setBlockSize(AppConstants.BLOCK_SIZE.getString());
    	form.setExperimentalDesign(AppConstants.EXPERIMENTAL_DESIGN.getString());
    }
    
    @ModelAttribute("experimentalDesignValues")
    public List<ValueReference> getExperimentalDesignValues() throws MiddlewareQueryException {
        return fieldbookService.getAllPossibleValues(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
    }
}
