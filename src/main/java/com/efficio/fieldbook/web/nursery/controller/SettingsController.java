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

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.*;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.annotation.Resource;
import java.util.*;

/**
 * The Class SettingsController.
 */
public abstract class SettingsController extends AbstractBaseFieldbookController{

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);
	 
	/** The workbench service. */
	@Resource
	protected WorkbenchService workbenchService;
	
	/** The fieldbook service. */
	@Resource
	protected FieldbookService fieldbookService;
	
	/** The fieldbook middleware service. */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	/** The user selection. */
    @Resource
    protected UserSelection userSelection;    
	
	/** The measurements generator service. */
	@Resource
	protected MeasurementsGeneratorService measurementsGeneratorService;
	
	/** The validation service. */
	@Resource
	protected ValidationService validationService;
	
	/** The data import service. */
	@Resource
	protected DataImportService dataImportService;
	
	@Resource
	protected OntologyService ontologyService;
	
	@Resource
	protected ContextUtil contextUtil;
	
	
    /**
     * Gets the settings list.
     *
     * @return the settings list
     */
    @ModelAttribute("settingsList")
    public List<TemplateSetting> getNurserySettingsList() {
        try {
        	TemplateSetting templateSettingFilter = new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), null, getNurseryTool(), null, null);
        	templateSettingFilter.setIsDefaultToNull();
            List<TemplateSetting> templateSettingsList = workbenchService.getTemplateSettings(templateSettingFilter);
            templateSettingsList.add(0, new TemplateSetting(Integer.valueOf(0), Integer.valueOf(getCurrentProjectId()), "", null, "", false));
            return templateSettingsList;

        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
		
        return new ArrayList<TemplateSetting>();
    }
    
    /**
     * Gets the trial settings list.
     *
     * @return the trial settings list
     */
    @ModelAttribute("settingsTrialList")
    public List<TemplateSetting> getTrialSettingsList() {
        try {
        	TemplateSetting templateSettingFilter = new TemplateSetting(null, Integer.valueOf(getCurrentProjectId()), null, getTrialTool(), null, null);
        	templateSettingFilter.setIsDefaultToNull();
            List<TemplateSetting> templateSettingsList = workbenchService.getTemplateSettings(templateSettingFilter);
            templateSettingsList.add(0, new TemplateSetting(Integer.valueOf(0), Integer.valueOf(getCurrentProjectId()), "", null, "", false));
            return templateSettingsList;

        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
		
        return new ArrayList<TemplateSetting>();
    }
    
    /**
     * Gets the settings list.
     *
     * @return the settings list
     */
    @ModelAttribute("nurseryList")
    public List<StudyDetails> getNurseryList() {
        try {
            return fieldbookMiddlewareService.getAllLocalNurseryDetails(
            		contextUtil.getCurrentProgramUUID());
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
                
        return new ArrayList<StudyDetails>();
    }
    
    /**
     * Gets the trial list.
     *
     * @return the trial list
     */
    @ModelAttribute("trialList")
    public List<StudyDetails> getTrialList() {
        try {
            return fieldbookMiddlewareService.getAllLocalTrialStudyDetails(
                    contextUtil.getCurrentProgramUUID());
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
                
        return new ArrayList<StudyDetails>();
    }
    
    /**
     * Builds the required factors.
     *
     * @param requiredFields the required fields
     * @return the list
     */
    protected List<Integer> buildVariableIDList(String requiredFields) {
        return FieldbookUtil.getInstance().buildVariableIDList(requiredFields);
    }
    
    /**
     * Builds the required factors label.
     *
     * @param requiredFields the required fields
     * @param hasLabels the has labels
     * @return the list
     */
    protected List<String> buildRequiredVariablesLabel(String requiredFields, boolean hasLabels) {
    	
        List<String> requiredVariables = new ArrayList<String>();
        StringTokenizer token = new StringTokenizer(requiredFields, ",");
        while(token.hasMoreTokens()){
            if (hasLabels) {
                requiredVariables.add(AppConstants.getString(token.nextToken() + AppConstants.LABEL.getString()));
            } else {
                requiredVariables.add(null);
                token.nextToken();
            }
        }        
        
        return requiredVariables;
    }

    /**
     * Builds the required factors flag.
     *
     * @param requiredFields the required fields
     * @return the boolean[]
     */
    protected boolean[] buildRequiredVariablesFlag(String requiredFields) {
        StringTokenizer token = new StringTokenizer(requiredFields, ",");
        boolean[] requiredVariablesFlag = new boolean[token.countTokens()];
        for (int i = 0; i < requiredVariablesFlag.length; i++) {
            requiredVariablesFlag[i] = false;
        }
        return requiredVariablesFlag;
    } 
    
    private String getCodeCounterpart(String idCodeNameCombination) {
        StringTokenizer tokenizer = new StringTokenizer(idCodeNameCombination, "|");
        if(tokenizer.hasMoreTokens()){
            tokenizer.nextToken();
            return tokenizer.nextToken();
        } else {
            return "0";
        }
    }
    
    /**
     * Update required fields.
     *
     * @param requiredVariables the required variables
     * @param requiredVariablesLabel the required variables label
     * @param requiredVariablesFlag the required variables flag
     * @param variables the variables
     * @param hasLabels the has labels
     * @return the list
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected List<SettingDetail> updateRequiredFields(List<Integer> requiredVariables, List<String> requiredVariablesLabel, 
            boolean[] requiredVariablesFlag, List<SettingDetail> variables, boolean hasLabels, String idCodeNameCombination) throws MiddlewareQueryException{
        
        //create a map of id and its id-code-name combination
        Map<String, String> idCodeNameMap = new HashMap<String, String>();
        if (idCodeNameCombination != null & !idCodeNameCombination.isEmpty()) {
            StringTokenizer tokenizer = new StringTokenizer(idCodeNameCombination, ",");
            if(tokenizer.hasMoreTokens()){
                while(tokenizer.hasMoreTokens()){
                    String pair = tokenizer.nextToken();
                    StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
                    idCodeNameMap.put(tokenizerPair.nextToken(), pair);
                }
            }
        }
        
        //save hidden conditions in a map
        Map<String, SettingDetail> variablesMap = new HashMap<String, SettingDetail>();
        if (variables != null) {
            for (SettingDetail variable : userSelection.getRemovedConditions()) {
                variablesMap.put(variable.getVariable().getCvTermId().toString(), variable);
            }
        }
        
        for (SettingDetail variable : variables) {
            Integer  stdVar = null;
            if (variable.getVariable().getCvTermId() != null) {
            	stdVar = variable.getVariable().getCvTermId();
            } else {
            	stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getVariable().getProperty(), 
                    variable.getVariable().getScale(), variable.getVariable().getMethod(), 
                    PhenotypicType.valueOf(variable.getVariable().getRole()));
            }
            
            //mark required variables that are already in the list
            int ctr = 0;
            for (Integer requiredFactor: requiredVariables) {
                String code = "0";
                //if the variable is in the id-code-name combination list, get code counterpart of id
                if (idCodeNameMap.get(String.valueOf(stdVar)) != null) {
                    code = getCodeCounterpart(idCodeNameMap.get(String.valueOf(stdVar)));
                }
                //if the id already exists do not add the code counterpart as a required field
                if (requiredFactor.equals(stdVar) || requiredFactor.equals(Integer.parseInt(code))) {
                    requiredVariablesFlag[ctr] = true;
                    variable.setOrder((requiredVariables.size()-ctr)*-1);
                    if (hasLabels) {
                        variable.getVariable().setName(requiredVariablesLabel.get(ctr));
                    }
                }
                ctr++;
            }
        }
        
        //add required variables that are not in existing nursery
        for (int i = 0; i < requiredVariablesFlag.length; i++) {
            if (!requiredVariablesFlag[i]) {
                SettingDetail newSettingDetail = createSettingDetail(requiredVariables.get(i), requiredVariablesLabel.get(i));
                newSettingDetail.setOrder((requiredVariables.size()-i)*-1);
                //set value of breeding method code if name is provided but id is not 
                if (TermId.BREEDING_METHOD_CODE.getId() == requiredVariables.get(i) 
                        && variablesMap.get(String.valueOf(TermId.BREEDING_METHOD.getId())) != null
                        && variablesMap.get(String.valueOf(TermId.BREEDING_METHOD_ID.getId())) == null) {
                    Method method = fieldbookMiddlewareService.getMethodByName(variablesMap.get(String.valueOf(TermId.BREEDING_METHOD.getId())).getValue());
                    newSettingDetail.setValue(method.getMid() == null ? "" : method.getMid().toString());
                }
                
                variables.add(newSettingDetail);
            }
        }
        
        //sort by required fields
        Collections.sort(variables, new  Comparator<SettingDetail>() {
            @Override
            public int compare(SettingDetail o1, SettingDetail o2) {
                    return o1.getOrder() - o2.getOrder();
            }
        });
        
        return variables;
    }
    
    /**
     * Builds the default variables.
     *
     * @param defaults the defaults
     * @param requiredFields the required fields
     * @param requiredVariablesLabel the required variables label
     * @return the list
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected List<SettingDetail> buildDefaultVariables(List<SettingDetail> defaults, String requiredFields, List<String> requiredVariablesLabel) throws MiddlewareQueryException{
        StringTokenizer token = new StringTokenizer(requiredFields, ",");
        int ctr = 0;
        while(token.hasMoreTokens()){
            defaults.add(createSettingDetail(Integer.valueOf(token.nextToken()), requiredVariablesLabel.get(ctr)));
            ctr++;
        }
        return defaults;
    }
    
    /**
     * Creates the setting detail.
     *
     * @param id the id
     * @param name the name
     * @return the setting detail
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected SettingDetail createSettingDetail(int id, String name) throws MiddlewareQueryException {
            String variableName = "";
            StandardVariable stdVar = getStandardVariable(id);
            if (name != null && !name.isEmpty()) {
                variableName = name;
            } else {
                variableName = stdVar.getName();
            }
            
            if (stdVar != null && stdVar.getName() != null) {
                SettingVariable svar = new SettingVariable(
                        variableName, stdVar.getDescription(), stdVar.getProperty().getName(),
                        stdVar.getScale().getName(), stdVar.getMethod().getName(), stdVar.getStoredIn().getName(),
                        stdVar.getDataType().getName(), stdVar.getDataType().getId(),
                        stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
                        stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
                svar.setCvTermId(stdVar.getId());
                svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
                svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
                svar.setOperation(Operation.ADD);

                    List<ValueReference> possibleValues = fieldbookService.getAllPossibleValues(id);
                    SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
                    if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
                        settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
                    } else if (id == TermId.STUDY_UID.getId()) {
                        settingDetail.setValue(this.getCurrentIbdbUserId().toString());
                    } else if (id == TermId.STUDY_UPDATE.getId()) {
                        settingDetail.setValue(DateUtil.getCurrentDateAsStringValue());
                    }
                    settingDetail.setPossibleValuesToJson(possibleValues);
                    List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProject().getUniqueID());
                    settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                    return settingDetail;
            } else {
                SettingVariable svar = new SettingVariable();
                svar.setCvTermId(stdVar.getId());
                return new SettingDetail(svar, null, null, false);
            }
    }
    
    /**
     * Populates Setting Variable.
     *
     * @param var the var
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected void populateSettingVariable(SettingVariable var) throws MiddlewareQueryException {
    	StandardVariable  stdvar = getStandardVariable(var.getCvTermId());
    	if (stdvar != null) {
			var.setDescription(stdvar.getDescription());
			var.setProperty(stdvar.getProperty().getName());
			var.setScale(stdvar.getScale().getName());
			var.setMethod(stdvar.getMethod().getName());
			var.setDataType(stdvar.getDataType().getName());
			var.setRole(stdvar.getStoredIn().getName());
			var.setCropOntologyId(stdvar.getCropOntologyId() != null ? stdvar.getCropOntologyId() : "");
			var.setTraitClass(stdvar.getIsA() != null ? stdvar.getIsA().getName() : "");
			var.setDataTypeId(stdvar.getDataType().getId());
			var.setMinRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMinValue() != null ? stdvar.getConstraints().getMinValue() : null);
			var.setMaxRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMaxValue() != null ? stdvar.getConstraints().getMaxValue() : null);
			var.setWidgetType();
    	}
    }
    
    /**
     * Get setting variable.
     *
     * @param id the id
     * @return the setting variable
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected SettingVariable getSettingVariable(int id) throws MiddlewareQueryException {
		StandardVariable stdVar = getStandardVariable(id);
		if (stdVar != null) {
			SettingVariable svar = new SettingVariable(stdVar.getName(), 
			        stdVar.getDescription(), stdVar.getProperty().getName(),
					stdVar.getScale().getName(), stdVar.getMethod().getName(), 
					stdVar.getStoredIn().getName(), 
					stdVar.getDataType().getName(), stdVar.getDataType().getId(),
					stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
					stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(
                    stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			return svar;
		}
		return null;
    }
    
    /**
     * Get standard variable.
     *
     * @param id the id
     * @return the standard variable
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected StandardVariable getStandardVariable(int id) throws MiddlewareQueryException {
    	StandardVariable variable = userSelection.getCacheStandardVariable(id);
    	if (variable == null) {
    		variable = fieldbookMiddlewareService.getStandardVariable(id);
    		if (variable != null) {
    			userSelection.putStandardVariableInCache(variable);
    		}
    	}
    	
    	return variable;
    }
    
    /**
     * Creates the study details.
     *
     * @param workbook the workbook
     * @param conditions the conditions
     * @param folderId the folder id
     */
    public void createStudyDetails(Workbook workbook, List<SettingDetail> conditions, Integer folderId, Integer studyId) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();

        if (conditions != null && !conditions.isEmpty()) {
        	if(studyId != null){
        		studyDetails.setId(studyId);
        	}
	        studyDetails.setTitle(SettingsUtil.getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
	        studyDetails.setObjective(SettingsUtil.getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
	        studyDetails.setStudyName(SettingsUtil.getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
	        studyDetails.setStartDate(SettingsUtil.getSettingDetailValue(conditions, TermId.START_DATE.getId()));
	        studyDetails.setEndDate(SettingsUtil.getSettingDetailValue(conditions, TermId.END_DATE.getId()));
	        studyDetails.setStudyType(StudyType.N);
	        
	        if (folderId != null) {
	        	studyDetails.setParentFolderId(folderId);
	        }
    	}
        studyDetails.print(1);
    }

    /**
     * Checks if the measurement table has user input data for a particular variable id
     * @param variableId
     * @return
     */
    public boolean hasMeasurementDataEntered(int variableId) {
        for (MeasurementRow row : userSelection.getMeasurementRowList()) {
            for (MeasurementData data : row.getDataList()) {
                if (data.getMeasurementVariable().getTermId() == variableId && data.getValue() != null && !data.getValue().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if the measurement table has user input data for a particular variable id
     * @param variableId, List<MeasurementRow>
     * @return
     */
    public static boolean hasMeasurementDataEntered(int variableId, List<MeasurementRow> measurementRow) {
        for (MeasurementRow row : measurementRow) {
            for (MeasurementData data : row.getDataList()) {
                if (data.getMeasurementVariable() != null  &&
                		data.getMeasurementVariable().getTermId() == variableId 
                		&& data.getValue() != null && !data.getValue().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean hasMeasurementDataEnteredForVariables(List<Integer> variableIds, UserSelection userSelectionTemp) {
    	for(Integer variableId : variableIds){
	        for (MeasurementRow row : userSelectionTemp.getMeasurementRowList()) {
	            for (MeasurementData data : row.getDataList()) {
	                if (data.getMeasurementVariable().getTermId() == variableId && data.getValue() != null && !data.getValue().isEmpty()) {
	                    return true;
	                }
	            }
	        }
    	}
        return false;
    }
    
    protected void removeVariablesFromExistingNursery(List<SettingDetail> settingList, String variables) {
        Iterator<SettingDetail> variableList = settingList.iterator();
        while (variableList.hasNext()) {
            if (SettingsUtil.inHideVariableFields(variableList.next().getVariable().getCvTermId(), variables)) {
                variableList.remove();
            }
        }
    }
            
    protected void resetSessionVariablesAfterSave(Workbook workbook, boolean isNursery) throws MiddlewareQueryException {
        
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
        removeDeletedVariablesInMeasurements(userSelection.getDeletedPlotLevelList(), workbook, userSelection);
        removeDeletedVariablesInMeasurements(userSelection.getDeletedBaselineTraitsList(), workbook, userSelection);
        
        //remove deleted variables in the original lists
        //and change add operation to update
        removeDeletedSetUpdate(userSelection.getStudyLevelConditions(), workbook.getConditions());
        removeDeletedSetUpdate(userSelection.getPlotsLevelList(), workbook.getFactors());
        removeDeletedSetUpdate(userSelection.getBaselineTraitsList(), workbook.getVariates());
        removeDeletedSetUpdate(userSelection.getNurseryConditions(), workbook.getConstants());
        removeDeletedSetUpdate(userSelection.getTrialLevelVariableList(), null);
        removeDeletedSetUpdate(userSelection.getSelectionVariates(), null);
        workbook.reset();
        
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
        resetDeletedLists(userSelection);
        
        //add name variables
        if (userSelection.getRemovedConditions() == null) {
            userSelection.setRemovedConditions(new ArrayList<SettingDetail>());
        }
        
        //remove basic details & hidden variables from study level variables
        SettingsUtil.removeBasicDetailsVariables(userSelection.getStudyLevelConditions());
        
        if (isNursery) {
            removeHiddenVariables(userSelection.getStudyLevelConditions(), AppConstants.HIDE_NURSERY_FIELDS.getString());
            removeRemovedVariablesFromSession(userSelection.getStudyLevelConditions(), userSelection.getRemovedConditions());
            removeHiddenVariables(userSelection.getPlotsLevelList(), AppConstants.HIDE_PLOT_FIELDS.getString());
            removeRemovedVariablesFromSession(userSelection.getPlotsLevelList(), userSelection.getRemovedFactors());
            addNameVariables(userSelection.getRemovedConditions(), workbook, AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
            removeCodeVariablesIfNeeded(userSelection.getStudyLevelConditions(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
            //set value of breeding method code back to code after saving
            SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
            //remove selection variates from traits list
            removeSelectionVariatesFromTraits(userSelection.getBaselineTraitsList());
        }                 
    }

    private void removeRemovedVariablesFromSession(List<SettingDetail> variableList, List<SettingDetail> removedVariableList) {
    	if (removedVariableList == null || variableList == null) {
    		return;
    	}
        for (SettingDetail setting : removedVariableList) {
            Iterator<SettingDetail> iter = variableList.iterator();
            while(iter.hasNext()) {
                if (iter.next().getVariable().getCvTermId().equals(setting.getVariable().getCvTermId())) {
                    iter.remove();
                }
            }
        }
    }
    
    private void removeDeletedVariablesInMeasurements(List<SettingDetail> deletedList, Workbook workbook, UserSelection userSelection) {
        if (deletedList != null) {
            for (SettingDetail setting : deletedList) {
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
    private void resetDeletedLists(UserSelection userSelection) {
        userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
        userSelection.setDeletedPlotLevelList(new ArrayList<SettingDetail>());
        userSelection.setDeletedBaselineTraitsList(new ArrayList<SettingDetail>());
        userSelection.setDeletedNurseryConditions(new ArrayList<SettingDetail>());
        userSelection.setDeletedTrialLevelVariables(new ArrayList<SettingDetail>());
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
     * Removes the hidden variables.
     * @param settingList
     * @param hiddenVarList
     */
    private void removeHiddenVariables(List<SettingDetail> settingList, String hiddenVarList) {
        if (settingList != null) {
            
            Iterator<SettingDetail> iter = settingList.iterator();
            while (iter.hasNext()) {
                if (SettingsUtil.inHideVariableFields(iter.next().getVariable().getCvTermId(),
                        hiddenVarList)) {
                    iter.remove();
                }
            }
        }
    }
    
    private void addNameVariables(List<SettingDetail> removedConditions, Workbook workbook, String idCodeNamePairs) throws MiddlewareQueryException {
        Map<String, MeasurementVariable> studyConditionMap = new HashMap<String, MeasurementVariable>();
        Map<String, SettingDetail> removedConditionsMap = new HashMap<String, SettingDetail>();
        if (workbook != null && idCodeNamePairs != null && !("").equalsIgnoreCase(idCodeNamePairs)) {
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
                        method = studyConditionMap.get(idTermId).getValue().isEmpty() ? null : fieldbookMiddlewareService.getMethodById(Double.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());
                    } else if (studyConditionMap.get(codeTermId) != null) {
                        method = studyConditionMap.get(codeTermId).getValue().isEmpty() ? null : fieldbookMiddlewareService.getMethodByCode(studyConditionMap.get(codeTermId).getValue());
                    }
                    
                    //add code to the removed conditions if code is not yet in the list
                    if (studyConditionMap.get(idTermId) != null && studyConditionMap.get(codeTermId) != null && removedConditionsMap.get(codeTermId) == null) {
                        addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, codeTermId, 
                                method == null ? "" : method.getMcode(), this.getCurrentIbdbUserId().toString());
                    }
                    
                    //add name to the removed conditions if name is not yet in the list
                    if (studyConditionMap.get(nameTermId) != null && removedConditionsMap.get(nameTermId) == null) {
                        addSettingDetail(removedConditions, removedConditionsMap, studyConditionMap, nameTermId, 
                                method == null ? "" : method.getMname(), this.getCurrentIbdbUserId().toString());
                    }
                }
            }
        }        
    }
    
    private void addSettingDetail(List<SettingDetail> removedConditions, Map<String, SettingDetail> removedConditionsMap, 
            Map<String, MeasurementVariable> studyConditionMap, String id, String value, String userId) throws MiddlewareQueryException {
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

    protected List<SettingDetail> getCheckVariables(List<SettingDetail> nurseryLevelConditions,
			CreateNurseryForm form) {
    	List<SettingDetail> checkVariables = getSettingDetailsOfSection(nurseryLevelConditions, form,
        		AppConstants.CHECK_VARIABLES.getString());
        //set order by id
        Collections.sort(checkVariables, new  Comparator<SettingDetail>() {
            @Override
            public int compare(SettingDetail o1, SettingDetail o2) {
                    return o1.getVariable().getCvTermId() - o2.getVariable().getCvTermId();
            }
        });
		return checkVariables;
	}

    /**
     * Gets the basic details.
     *
     * @param nurseryLevelConditions the nursery level conditions
     * @return the basic details
     */
    protected List<SettingDetail> getSettingDetailsOfSection(List<SettingDetail> nurseryLevelConditions, CreateNurseryForm form, String variableList) {
        List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();
        
        StringTokenizer token = new StringTokenizer(variableList, ",");
        while(token.hasMoreTokens()){
            Integer termId = Integer.valueOf(token.nextToken());
            boolean isFound = searchAndSetValuesOfSpecialVariables(nurseryLevelConditions, termId, settingDetails, form); 
            if(!isFound){
            	addSettingDetails(settingDetails, termId, form);
            }
        }
        
        return settingDetails;
    }

	private boolean searchAndSetValuesOfSpecialVariables(
			List<SettingDetail> nurseryLevelConditions, Integer termId,
			List<SettingDetail> settingDetails, CreateNurseryForm form) {
		boolean isFound = false;
		for (SettingDetail setting : nurseryLevelConditions) {
            if (termId.equals(setting.getVariable().getCvTermId())) {
                isFound = true;
                setCreatedByAndStudyUpdate(termId, setting, form);
                settingDetails.add(setting);
            }
        } 
		return isFound;
	}

	private void setCreatedByAndStudyUpdate(Integer termId, SettingDetail setting,
			CreateNurseryForm form) {
		if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {
            try {
            	if (setting.getValue() != null && !setting.getValue().isEmpty() && NumberUtils.isNumber(setting.getValue())) {
            		form.setCreatedBy(fieldbookService.getPersonByUserId(
                            Integer.parseInt(setting.getValue())));
            	}
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
        } else if (termId.equals(Integer.valueOf(TermId.STUDY_UPDATE.getId()))) {
            setting.setValue(DateUtil.getCurrentDateAsStringValue());
        }
	}

	private void addSettingDetails(List<SettingDetail> settingDetails, Integer termId,
			CreateNurseryForm form) {
		try {
        	settingDetails.add(createSettingDetail(termId, null));
            if (termId.equals(Integer.valueOf(TermId.STUDY_UID.getId()))) {                    	
        		form.setCreatedBy(fieldbookService.getPersonByUserId(this.getCurrentIbdbUserId()));
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
	}

	protected void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

    protected void addVariableInDeletedList(List<SettingDetail> currentList, int mode,
            int variableId,boolean createNewSettingIfNull) {
        SettingDetail newSetting = null;
        for (SettingDetail setting : currentList) {
            if (setting.getVariable().getCvTermId().equals(Integer.valueOf(variableId))) {
                newSetting = setting;
            }
        }

        if (newSetting == null && createNewSettingIfNull) {
            try {
                newSetting = createSettingDetail(variableId, "");
                newSetting.getVariable().setOperation(Operation.UPDATE);
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
        } else if (newSetting == null) {
            return;
        }

        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            if (userSelection.getDeletedStudyLevelConditions() == null) {
                userSelection.setDeletedStudyLevelConditions(new ArrayList<SettingDetail>());
            }
            userSelection.getDeletedStudyLevelConditions().add(newSetting);
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()
                || mode == AppConstants.SEGMENT_GERMPLASM.getInt()) {
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
        } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            if (userSelection.getDeletedTrialLevelVariables() == null) {
                userSelection.setDeletedTrialLevelVariables(new ArrayList<SettingDetail>());
            }
            userSelection.getDeletedTrialLevelVariables().add(newSetting);
        } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
            if (userSelection.getDeletedTreatmentFactors() == null) {
                userSelection.setDeletedTreatmentFactors(new ArrayList<SettingDetail>());
            }
            userSelection.getDeletedTreatmentFactors().add(newSetting);
        }
    }

}
