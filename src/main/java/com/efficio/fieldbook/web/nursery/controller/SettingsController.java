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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.service.api.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;

// TODO: Auto-generated Javadoc
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
		
        return null;
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
		
        return null;
    }
    
    /**
     * Gets the settings list.
     *
     * @return the settings list
     */
    @ModelAttribute("nurseryList")
    public List<StudyDetails> getNurseryList() {
        try {
            List<StudyDetails> nurseries = fieldbookMiddlewareService.getAllLocalNurseryDetails();
            return nurseries;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
                
        return null;
    }
    
    /**
     * Gets the trial list.
     *
     * @return the trial list
     */
    @ModelAttribute("trialList")
    public List<StudyDetails> getTrialList() {
        try {
            List<StudyDetails> nurseries = fieldbookMiddlewareService.getAllLocalTrialStudyDetails();
            return nurseries;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
                
        return null;
    }
    
    /**
     * Builds the required factors.
     *
     * @param requiredFields the required fields
     * @return the list
     */
    protected List<Integer> buildRequiredVariables(String requiredFields) {
        List<Integer> requiredVariables = new ArrayList<Integer>();
        StringTokenizer token = new StringTokenizer(requiredFields, ",");
        while(token.hasMoreTokens()){
            requiredVariables.add(Integer.valueOf(token.nextToken()));
        }        
        return requiredVariables;
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
        HashMap<String, String> idCodeNameMap = new HashMap<String, String>();
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
        HashMap<String, SettingDetail> variablesMap = new HashMap<String, SettingDetail>();
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
                        DateFormat dateFormat = new SimpleDateFormat(DateUtil.DB_DATE_FORMAT);
                        Date date = new Date();
                        settingDetail.setValue(dateFormat.format(date));
                    }
                    settingDetail.setPossibleValuesToJson(possibleValues);
                    List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProjectId());
                    settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                    return settingDetail;
            } else {
                SettingVariable svar = new SettingVariable();
                svar.setCvTermId(stdVar.getId());
                SettingDetail settingDetail = new SettingDetail(svar, null, null, false);
                return settingDetail;
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
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
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
	        studyDetails.setTitle(getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
	        studyDetails.setObjective(getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
	        studyDetails.setStudyName(getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
	        studyDetails.setStartDate(getSettingDetailValue(conditions, TermId.START_DATE.getId()));
	        studyDetails.setEndDate(getSettingDetailValue(conditions, TermId.END_DATE.getId()));
	        studyDetails.setStudyType(StudyType.N);
	        
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
    
    protected void removeVariablesFromExistingNursery(List<SettingDetail> settingList, String variables) {
        Iterator<SettingDetail> variableList = settingList.iterator();
        while (variableList.hasNext()) {
            if (SettingsUtil.inHideVariableFields(variableList.next().getVariable().getCvTermId(), variables)) {
                variableList.remove();
            }
        }
    }
}
