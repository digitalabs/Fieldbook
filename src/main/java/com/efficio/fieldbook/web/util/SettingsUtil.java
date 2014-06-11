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
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Constant;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.ParentDataset;
import org.generationcp.middleware.pojos.workbench.settings.TreatmentFactor;
import org.generationcp.middleware.pojos.workbench.settings.TrialDataset;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;
import com.efficio.fieldbook.web.nursery.bean.NurseryDetails;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;

/**
 * The Class SettingsUtil.
 */
public class SettingsUtil {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SettingsUtil.class);

	public static String cleanSheetAndFileName(String name){
		if(name == null)
			return null;
    	name = name.replaceAll("[^a-zA-Z0-9-_.=^&'@{}$!-#()%.+~_\\[\\]]", "_");
    	name = name.replaceAll("\"", "_");
    	return name;
    }
	

	/**
	 * Get standard variable.
	 *
	 * @param id the id
	 * @param userSelection the user selection
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @return the standard variable
	 */
    private static StandardVariable getStandardVariable(int id, UserSelection userSelection, 
    		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
    	
    	StandardVariable variable = userSelection.getCacheStandardVariable(id);
    	if (variable == null) {
    		try {
				variable = fieldbookMiddlewareService.getStandardVariable(id);
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
    		if (variable != null) {
    			userSelection.putStandardVariableInCache(variable);
    		}
    	}
    	
    	return variable;
    }
	
	/**
	 * Convert pojo to xml dataset.
	 *
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param name the name
	 * @param nurseryLevelConditions the nursery level conditions
	 * @param plotsLevelList the plots level list
	 * @param baselineTraitsList the baseline traits list
	 * @param userSelection the user selection
	 * @return the dataset
	 */
    public static ParentDataset convertPojoToXmlDataset(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, String name, List<SettingDetail> nurseryLevelConditions, 
            List<SettingDetail> plotsLevelList, List<SettingDetail> baselineTraitsList, UserSelection userSelection, List<SettingDetail> nurseryConditions){
    	return convertPojoToXmlDataset(fieldbookMiddlewareService, name, nurseryLevelConditions,  plotsLevelList,baselineTraitsList,  userSelection, null, null, null, nurseryConditions);
    }

	/**
	 * Convert pojo to xml dataset.
	 *
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param name the name
	 * @param nurseryLevelConditions the nursery level conditions
	 * @param plotsLevelList the plots level list
	 * @param baselineTraitsList the baseline traits list
	 * @param userSelection the user selection
	 * @param trialLevelVariablesList the trial level variables list
	 * @return the parent dataset
	 */
	public static ParentDataset convertPojoToXmlDataset(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, 
			String name, List<SettingDetail> nurseryLevelConditions, List<SettingDetail> plotsLevelList, List<SettingDetail> baselineTraitsList, 
			UserSelection userSelection, List<SettingDetail> trialLevelVariablesList, List<SettingDetail> treatmentFactorList, 
			List<TreatmentFactorDetail> treatmentDetailList, List<SettingDetail> nurseryConditions){
		
		List<Condition> conditions = new ArrayList<Condition>();
		List<Factor> factors = new ArrayList<Factor>();
		List<Variate> variates = new ArrayList<Variate>();
		List<Factor> trialLevelVariables = new ArrayList<Factor>();
		List<TreatmentFactor> treatmentFactors = new ArrayList<TreatmentFactor>();
		List<Constant> constants = new ArrayList<Constant>();
		//iterate for the nursery level
		int index = 0;
		for(SettingDetail settingDetail : nurseryLevelConditions){
			SettingVariable variable = settingDetail.getVariable();
			if(userSelection != null){
				StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
				
				variable.setPSMRFromStandardVariable(standardVariable);
				//need to get the name from the session
				variable.setName(userSelection.getStudyLevelConditions().get(index).getVariable().getName());
				
			
        			
        			Condition condition = new Condition(variable.getName(), variable.getDescription(), variable.getProperty(),
        					variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
        					HtmlUtils.htmlEscape(settingDetail.getValue()), variable.getDataTypeId(), variable.getMinRange(), variable.getMaxRange());
        			condition.setOperation(userSelection.getStudyLevelConditions().get(index++).getVariable().getOperation());
        			condition.setStoredIn(standardVariable.getStoredIn().getId());
        			condition.setId(variable.getCvTermId());
        			conditions.add(condition);
			}
		}
		//iterate for the plot level
		index = 0;
		if(plotsLevelList != null && !plotsLevelList.isEmpty()){
			for(SettingDetail settingDetail : plotsLevelList){
				SettingVariable variable = settingDetail.getVariable();
				if(userSelection != null){
					StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
					variable.setPSMRFromStandardVariable(standardVariable);
					//need to get the name from the session
					variable.setName(userSelection.getPlotsLevelList().get(index).getVariable().getName());
				
        				Factor factor = new Factor(variable.getName(), variable.getDescription(), variable.getProperty(),
        						variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(), variable.getCvTermId());
        				factor.setOperation(userSelection.getPlotsLevelList().get(index++).getVariable().getOperation());
        				factor.setStoredIn(standardVariable.getStoredIn().getId());
        				factor.setId(standardVariable.getId());
        				factors.add(factor);
				}
			}
		}
		//iterate for the baseline traits level
		index = 0;
		if(baselineTraitsList != null && !baselineTraitsList.isEmpty()){
			for(SettingDetail settingDetail : baselineTraitsList){
				SettingVariable variable = settingDetail.getVariable();
				if(userSelection != null){
					StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
					variable.setPSMRFromStandardVariable(standardVariable);
					//need to get the name from the session
					variable.setName(userSelection.getBaselineTraitsList().get(index).getVariable().getName());
					
				
        				Variate variate = new Variate(variable.getName(), variable.getDescription(), variable.getProperty(),
        						variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(), variable.getDataTypeId(),
        						settingDetail.getPossibleValues(), variable.getMinRange(), variable.getMaxRange());
        				variate.setOperation(userSelection.getBaselineTraitsList().get(index++).getVariable().getOperation());
        				variate.setStoredIn(standardVariable.getStoredIn().getId());
        				variate.setId(variable.getCvTermId());
        				variates.add(variate);
				}
			}
		}
		
		//iterate for the nursery conditions/constants
		if (nurseryConditions != null && !nurseryConditions.isEmpty()) {
                index = 0;
                for(SettingDetail settingDetail : nurseryConditions){
                        SettingVariable variable = settingDetail.getVariable();
                        if(userSelection != null){
                                StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                                
                                variable.setPSMRFromStandardVariable(standardVariable);
                                //need to get the name from the session
                                variable.setName(userSelection.getNurseryConditions().get(index).getVariable().getName()); 
                        
                        
                                Constant constant= new Constant(variable.getName(), variable.getDescription(), variable.getProperty(),
                                                variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
                                                HtmlUtils.htmlEscape(settingDetail.getValue()), variable.getDataTypeId(), variable.getMinRange(), variable.getMaxRange());
                                constant.setOperation(userSelection.getNurseryConditions().get(index++).getVariable().getOperation());
                                constant.setStoredIn(standardVariable.getStoredIn().getId());
                                constant.setId(variable.getCvTermId());
                                constants.add(constant);
                        }
                }
		}
		
		//iterate for treatment factor details
		if (treatmentDetailList != null && !treatmentDetailList.isEmpty()) {
			List<Integer> addedTreatmentFactors = new ArrayList<Integer>();
			for (TreatmentFactorDetail detail : treatmentDetailList) {
				TreatmentFactor treatmentFactor = convertTreatmentFactorDetailToTreatmentFactor(detail, userSelection, fieldbookMiddlewareService);
				treatmentFactors.add(treatmentFactor);
				if (!addedTreatmentFactors.contains(treatmentFactor.getLevelFactor().getTermId())) {
					factors.add(treatmentFactor.getLevelFactor());
					factors.add(treatmentFactor.getValueFactor());
					addedTreatmentFactors.add(treatmentFactor.getLevelFactor().getTermId());
				}
			}
		}
		else if (treatmentFactorList != null && !treatmentFactorList.isEmpty()) {
			int currentGroup = -1;
			TreatmentFactor treatmentFactor;
			Factor levelFactor = null, valueFactor = null;
			
			for (int i = 0; i < treatmentFactorList.size(); i++) {
				currentGroup = getTreatmentGroup(userSelection, treatmentFactorList, i);
				levelFactor = createFactor(treatmentFactorList.get(i), userSelection, fieldbookMiddlewareService, i);
				levelFactor.setTreatmentLabel(treatmentFactorList.get(i).getVariable().getName());

				int j;
				for (j = i + 1; j < treatmentFactorList.size(); j++) {
					int groupNumber = getTreatmentGroup(userSelection, treatmentFactorList, j);
					if (groupNumber != currentGroup) {
						j--;
						break;
					}
					valueFactor = createFactor(treatmentFactorList.get(j), userSelection, fieldbookMiddlewareService, j);
					valueFactor.setTreatmentLabel(treatmentFactorList.get(i).getVariable().getName());
				}
				i = j;
				treatmentFactor = new TreatmentFactor(levelFactor, valueFactor);
				treatmentFactors.add(treatmentFactor);
			}
		}
		
		ParentDataset realDataset = null;
		if(trialLevelVariablesList != null){
			
			index = 0;
			if(trialLevelVariablesList != null && !trialLevelVariablesList.isEmpty()){
				for(SettingDetail settingDetail : trialLevelVariablesList){
					SettingVariable variable = settingDetail.getVariable();
					if(userSelection != null){
						StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
						variable.setPSMRFromStandardVariable(standardVariable);
						//need to get the name from the session
						variable.setName(userSelection.getTrialLevelVariableList().get(index++).getVariable().getName());
						
					}
					Factor factor = new Factor(variable.getName(), variable.getDescription(), variable.getProperty(),
							variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(), variable.getCvTermId());
					trialLevelVariables.add(factor);
				}
			}
			
			
			//this is a trial dataset
			TrialDataset dataset = new TrialDataset(trialLevelVariables);
			dataset.setConditions(conditions);
			dataset.setFactors(factors);
			dataset.setVariates(variates);
			dataset.setName(name);
			dataset.setTrialLevelFactor(trialLevelVariables);
			dataset.setTreatmentFactors(treatmentFactors);
			realDataset = dataset;
		}else{
			Dataset dataset = new Dataset();
			dataset.setConditions(conditions);
			dataset.setFactors(factors);
			dataset.setVariates(variates);
			dataset.setConstants(constants);
			dataset.setName(name);
			realDataset = dataset;
		}
		
	
		
		return realDataset;
	}
	
	
	/**
	 * Gets the field possible vales.
	 *
	 * @param fieldbookService the fieldbook service
	 * @param standardVariableId the standard variable id
	 * @return the field possible vales
	 */
	public static List<ValueReference> getFieldPossibleVales(FieldbookService fieldbookService, Integer standardVariableId){
		List<ValueReference> possibleValueList = new ArrayList<ValueReference>();
		
		try {
		
			//possibleValueList = fieldbookService.getAllPossibleValuesByPSMR(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variable.getRole()));
			possibleValueList = fieldbookService.getAllPossibleValues(standardVariableId);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return possibleValueList;
	}
	
	/**
	 * Gets the field possible values favorite.
	 *
	 * @param fieldbookService the fieldbook service
	 * @param standardVariableId the standard variable id
	 * @param projectId the project id
	 * @return the field possible values favorite
	 */
	private static List<ValueReference> getFieldPossibleValuesFavorite(FieldbookService fieldbookService, Integer standardVariableId, String projectId) {
	    List<ValueReference> possibleValueList = new ArrayList<ValueReference>();
            
            try {
            
                    //possibleValueList = fieldbookService.getAllPossibleValuesByPSMR(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variable.getRole()));
                    possibleValueList = fieldbookService.getAllPossibleValuesFavorite(standardVariableId, projectId);
            } catch (MiddlewareQueryException e) {
    			LOG.error(e.getMessage(), e);
            }
            return possibleValueList;
	}
	
	/**
	 * Checks if is setting variable deletable.
	 *
	 * @param standardVariableId the standard variable id
	 * @param requiredFields the required fields
	 * @return true, if is setting variable deletable
	 */
	public static boolean isSettingVariableDeletable(Integer standardVariableId, String requiredFields){
		//need to add the checking here if the specific PSM-R is deletable, for the nursery level details
	        StringTokenizer token = new StringTokenizer(requiredFields, ",");
	        while(token.hasMoreTokens()){ 
	            if (standardVariableId.equals(Integer.parseInt(token.nextToken()))) {
	                return false;
	            }
	        }		
		return true;
	}
	
	/**
	 * Convert xml dataset to pojo.
	 *
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param fieldbookService the fieldbook service
	 * @param dataset the dataset
	 * @param userSelection the user selection
	 * @param projectId the project id
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	public static void convertXmlDatasetToPojo(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, com.efficio.fieldbook.service.api.FieldbookService fieldbookService, ParentDataset dataset, UserSelection userSelection, String projectId) throws MiddlewareQueryException{
		if(dataset instanceof Dataset)
			 convertXmlNurseryDatasetToPojo( fieldbookMiddlewareService, fieldbookService, (Dataset) dataset,  userSelection, projectId);
		else if(dataset instanceof TrialDataset)
			convertXmlTrialDatasetToPojo( fieldbookMiddlewareService, fieldbookService, (TrialDataset) dataset,  userSelection, projectId);
	}
	
	/**
	 * Convert xml nursery dataset to pojo.
	 *
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param fieldbookService the fieldbook service
	 * @param dataset the dataset
	 * @param userSelection the user selection
	 * @param projectId the project id
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	private static void convertXmlNurseryDatasetToPojo(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, com.efficio.fieldbook.service.api.FieldbookService fieldbookService, Dataset dataset, UserSelection userSelection, String projectId) throws MiddlewareQueryException{
		if(dataset != null && userSelection != null){
			//we copy it to User session object
			//nursery level
   		    List<SettingDetail> studyLevelConditions = new ArrayList<SettingDetail>();
		    List<SettingDetail> plotsLevelList  = new ArrayList<SettingDetail>();
		    List<SettingDetail> baselineTraitsList  = new ArrayList<SettingDetail>();
		    List<SettingDetail> nurseryConditions = new ArrayList<SettingDetail>();
		    List<SettingDetail> selectionVariates = new ArrayList<SettingDetail>();
		    List<SettingDetail> removedFactors = new ArrayList<SettingDetail>();
		    List<SettingDetail> removedConditions = new ArrayList<SettingDetail>();
		    if(dataset.getConditions() != null){
				for(Condition condition : dataset.getConditions()){					
					SettingVariable variable = new SettingVariable(condition.getName(), condition.getDescription(), condition.getProperty(),
							condition.getScale(), condition.getMethod(), condition.getRole(), condition.getDatatype(), condition.getDataTypeId(),
							condition.getMinRange(), condition.getMaxRange());
					variable.setOperation(Operation.UPDATE);
					Integer stdVar = null;
					if (condition.getId() != 0) {
						stdVar = condition.getId();
					}
					else {
						stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), 
							HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
					}
					
                    variable.setCvTermId(stdVar);                                        
                    List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                    SettingDetail settingDetail = new SettingDetail(variable,
                            possibleValues, HtmlUtils.htmlUnescape(condition.getValue()), isSettingVariableDeletable(stdVar, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()));
                    
                    settingDetail.setPossibleValuesToJson(possibleValues);
                    List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, projectId);
                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                    
                    if(userSelection != null){
                        StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                        variable.setPSMRFromStandardVariable(standardVariable);                        
                        Enumeration enumerationByDescription = standardVariable.getEnumerationByDescription(condition.getValue());
                        
                        if (!inHideVariableFields(stdVar, AppConstants.HIDE_NURSERY_FIELDS.getString())) {
                            if(enumerationByDescription != null) {
                                settingDetail.setValue(enumerationByDescription.getName());
                            }
                            studyLevelConditions.add(settingDetail);
                        } else {
                            if(enumerationByDescription != null) {
                                settingDetail.setValue(enumerationByDescription.getId().toString());
                            }
                            removedConditions.add(settingDetail);
                        }
                    }
                    
				}
		    }
			//plot level
			//always allowed to be deleted
			if(dataset.getFactors() != null){
				for(Factor factor : dataset.getFactors()){
					SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
							factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
					variable.setOperation(Operation.UPDATE);
					Integer stdVar = null;
					if (factor.getTermId() != null) {
						stdVar = factor.getTermId();
					} else {
						stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
					}
					
					variable.setCvTermId(stdVar);
					SettingDetail settingDetail = new SettingDetail(variable,
							null, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()));
					
					if (factor.getRole() != null && !factor.getRole().equals(PhenotypicType.TRIAL_ENVIRONMENT.name())) {
						if (!inHideVariableFields(stdVar, AppConstants.HIDE_PLOT_FIELDS.getString())) {
							plotsLevelList.add(settingDetail);
						} else {
							removedFactors.add(settingDetail);
						}
					} else {
						removedFactors.add(settingDetail);
					}
				}
			}
			//baseline traits
			//always allowed to be deleted
			if(dataset.getVariates() != null){
				for(Variate variate : dataset.getVariates()){
					
					SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
							variate.getScale(), variate.getMethod(), variate.getRole(), variate.getDatatype());
					variable.setOperation(Operation.UPDATE);
					Integer  stdVar = null;
					if (variate.getId() != 0) {
						stdVar = variate.getId();
					}
					else {
						stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
					}
					variable.setCvTermId(stdVar);
					StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
					
					SettingDetail settingDetail = new SettingDetail(variable,
                                                null, null, true);
					if (inPropertyList(standardVariable.getProperty().getId())) {
					    selectionVariates.add(settingDetail);
					} else {
					    baselineTraitsList.add(settingDetail);
					}
					/*
					if(userSelection != null){
						StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);						
						variable.setPSMRFromStandardVariable(standardVariable);						
					}
					*/
				}
			}
			
			//nursery conditions/constants
			if(dataset.getConstants() != null){
                            for(Constant constant : dataset.getConstants()){
                                    SettingVariable variable = new SettingVariable(constant.getName(), constant.getDescription(), constant.getProperty(),
                                                    constant.getScale(), constant.getMethod(), constant.getRole(), constant.getDatatype(), constant.getDataTypeId(),
                                                    constant.getMinRange(), constant.getMaxRange());
                                    variable.setOperation(Operation.UPDATE);
                                    Integer  stdVar = null;
                                    if (constant.getId() != 0) {
                                    	stdVar = constant.getId();
                                    }
                                    else {
                                    	stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), 
                                                    HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.VARIATE);
                                    }
                                    
                                    variable.setCvTermId(stdVar);      
                                    
                                    List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                                    SettingDetail settingDetail = new SettingDetail(variable,
                                                    possibleValues, HtmlUtils.htmlUnescape(constant.getValue()), true);
                                    
                                    settingDetail.setPossibleValuesToJson(possibleValues);
                                    List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, projectId);
                                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                                    nurseryConditions.add(settingDetail);
                                    if(userSelection != null){
                                            StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);                                             
                                            variable.setPSMRFromStandardVariable(standardVariable); 
                                            Enumeration enumerationByDescription = standardVariable.getEnumerationByDescription(constant.getValue());
                                            if(enumerationByDescription != null) {
                                            	settingDetail.setValue(enumerationByDescription.getName());
                                            }
                                    }
                            }
                        }
			
			userSelection.setStudyLevelConditions(studyLevelConditions);
			userSelection.setPlotsLevelList(plotsLevelList);			
			userSelection.setBaselineTraitsList(baselineTraitsList);
			userSelection.setNurseryConditions(nurseryConditions);
			userSelection.setSelectionVariates(selectionVariates);
			userSelection.setRemovedFactors(removedFactors);
			userSelection.setRemovedConditions(removedConditions);
		}
	}
	
	public static boolean inPropertyList(int propertyId) {
	    StringTokenizer token = new StringTokenizer(AppConstants.SELECTION_VARIATES_PROPERTIES.getString(), ",");
            while(token.hasMoreTokens()){
                int propId = Integer.parseInt(token.nextToken());
                
                if (propId == propertyId) {
                    return true;
                }
            }
            return false;
	}
	
	/**
	 * Convert xml trial dataset to pojo.
	 *
	 * @param fieldbookMiddlewareService the fieldbook middleware service
	 * @param fieldbookService the fieldbook service
	 * @param dataset the dataset
	 * @param userSelection the user selection
	 * @param projectId the project id
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	private static void convertXmlTrialDatasetToPojo(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, com.efficio.fieldbook.service.api.FieldbookService fieldbookService, TrialDataset dataset, UserSelection userSelection, String projectId) throws MiddlewareQueryException{
		if(dataset != null && userSelection != null){
			//we copy it to User session object
			//nursery level
   		    List<SettingDetail> studyLevelConditions = new ArrayList<SettingDetail>();
		    List<SettingDetail> plotsLevelList  = new ArrayList<SettingDetail>();
		    List<SettingDetail> baselineTraitsList  = new ArrayList<SettingDetail>();
		    List<SettingDetail> trialLevelVariableList  = new ArrayList<SettingDetail>();
		    List<SettingDetail> treatmentFactors = new ArrayList<SettingDetail>();
		    if(dataset.getConditions() != null){
				for(Condition condition : dataset.getConditions()){
					
					SettingVariable variable = new SettingVariable(condition.getName(), condition.getDescription(), condition.getProperty(),
							condition.getScale(), condition.getMethod(), condition.getRole(), condition.getDatatype(), condition.getDataTypeId(),
							condition.getMinRange(), condition.getMaxRange());
					//Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.valueOf(variable.getRole()));
					Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
					
					if (!inHideVariableFields(stdVar, AppConstants.HIDE_NURSERY_FIELDS.getString())) {
        					variable.setCvTermId(stdVar);										
        					List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
        					SettingDetail settingDetail = new SettingDetail(variable,
        							possibleValues, HtmlUtils.htmlUnescape(condition.getValue()), isSettingVariableDeletable(stdVar, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()));
        					
        					settingDetail.setPossibleValuesToJson(possibleValues);
        					List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, projectId);
        					settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
        					studyLevelConditions.add(settingDetail);
        					if(userSelection != null){
        						StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);						
        						variable.setPSMRFromStandardVariable(standardVariable);						
        					}
    					}
				}
		    }
			//plot level
			//always allowed to be deleted
			if(dataset.getFactors() != null){
				for(Factor factor : dataset.getFactors()){
					
					if (factor.getTreatmentLabel() == null || "".equals(factor.getTreatmentLabel()) 
							&& factor.getRole() != null && !factor.getRole().equals(PhenotypicType.TRIAL_ENVIRONMENT.name())) {
					
						SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
								factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
						//Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.valueOf(variable.getRole()));
						Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
						if (!inHideVariableFields(stdVar, AppConstants.HIDE_PLOT_FIELDS.getString())) {
	        					variable.setCvTermId(stdVar);
	        					SettingDetail settingDetail = new SettingDetail(variable,
	        							null, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString()));
	        					plotsLevelList.add(settingDetail);
						}
						/*
						if(userSelection != null){
							StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);						
							variable.setPSMRFromStandardVariable(standardVariable);						
						}
						*/
					}
				}
			}
			//baseline traits
			//always allowed to be deleted
			if(dataset.getVariates() != null){
				for(Variate variate : dataset.getVariates()){
					
					SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
							variate.getScale(), variate.getMethod(), variate.getRole(), variate.getDatatype());
					//Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.valueOf(variable.getRole()));
					Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
					variable.setCvTermId(stdVar);
					SettingDetail settingDetail = new SettingDetail(variable,
							null, null, true);
					baselineTraitsList.add(settingDetail);
					/*
					if(userSelection != null){
						StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);						
						variable.setPSMRFromStandardVariable(standardVariable);						
					}
					*/
				}
			}
				
			if(dataset.getTrialLevelFactor() != null){
				for(Factor factor : dataset.getTrialLevelFactor()){
					String variableName = factor.getName();
					/*
					String tempName = AppConstants.getString(variableName + AppConstants.LABEL.getString());
					if(tempName != null)
						variableName = tempName;
					*/
					SettingVariable variable = new SettingVariable(variableName, factor.getDescription(), factor.getProperty(),
							factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
					//Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.valueOf(variable.getRole()));
					Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
					if (!inHideVariableFields(stdVar, AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString())) {
	    					variable.setCvTermId(stdVar);
	    					
	    					List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                                                SettingDetail settingDetail = new SettingDetail(variable,
                                                                possibleValues, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()));
                                                
                                                settingDetail.setPossibleValuesToJson(possibleValues);
                                                List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, projectId);
                                                settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                                                
                                                if (TermId.TRIAL_INSTANCE_FACTOR.getId() == variable.getCvTermId()) {
                                                    settingDetail.setDeletable(false);
                                                }
                                                
	    					trialLevelVariableList.add(settingDetail);
	    					
	    					if(userSelection != null){
                                                    StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);                                             
                                                    variable.setPSMRFromStandardVariable(standardVariable);                                         
                                                }
					}
				}
		    }
			
			if (dataset.getTreatmentFactors() != null && !dataset.getTreatmentFactors().isEmpty()) {
				int group = 1;
				for (TreatmentFactor treatmentFactor : dataset.getTreatmentFactors()) {
           
					treatmentFactors.add(createTreatmentFactor(treatmentFactor.getLevelFactor(), fieldbookMiddlewareService, fieldbookService, group, userSelection));
					treatmentFactors.add(createTreatmentFactor(treatmentFactor.getValueFactor(), fieldbookMiddlewareService, fieldbookService, group, userSelection));
					
					group++;
				}
			}
			
			userSelection.setStudyLevelConditions(studyLevelConditions);
			userSelection.setPlotsLevelList(plotsLevelList);			
			userSelection.setBaselineTraitsList(baselineTraitsList);
			userSelection.setTrialLevelVariableList(trialLevelVariableList);
			userSelection.setTreatmentFactors(treatmentFactors);
		}
	}
	
	/**
	 * In hide variable fields.
	 *
	 * @param stdVarId the std var id
	 * @param variableList the variable list
	 * @return true, if successful
	 */
	public static boolean inHideVariableFields(Integer stdVarId, String variableList) {
	    StringTokenizer token = new StringTokenizer(variableList, ",");
	    boolean inList = false;
	    while(token.hasMoreTokens()){
	        if (stdVarId.equals(Integer.parseInt(token.nextToken()))) {
	            inList = true;
	            break;
	        }
	    }
	    return inList;
	}
		
	/**
	 * Generate dummy condition.
	 *
	 * @param limit the limit
	 * @return the list
	 */
	public static List<Condition> generateDummyCondition(int limit){
		List<Condition> conditions = new ArrayList<Condition>();
		for(int i = 0 ; i < limit ; i++){
			Condition condition = new Condition();
			
			condition.setName(i + "name");
			condition.setDescription(i + " description");
			condition.setProperty(i + " property");
			condition.setScale(i + " scale");
			condition.setMethod(i + " method");
			condition.setRole(i + " role");
			condition.setDatatype(i + "Test Data Type");
			condition.setValue(i + " value");
			conditions.add(condition);
		}
		return conditions;
	}
	
	/**
	 * Generate dummy factor.
	 *
	 * @param limit the limit
	 * @return the list
	 */
	public static List<Factor> generateDummyFactor(int limit){
		List<Factor> factors = new ArrayList<Factor>();
		for(int i = 0 ; i < limit ; i++){
			Factor factor = new Factor();
			
			factor.setName(i + "name");
			factor.setDescription(i + " description");
			factor.setProperty(i + " property");
			factor.setScale(i + " scale");
			factor.setMethod(i + " method");
			factor.setRole(i + " role");
			factor.setDatatype(i + "Test Data Type");
			factors.add(factor);
		}
		return factors;
	}
	
	/**
	 * Generate dummy variate.
	 *
	 * @param limit the limit
	 * @return the list
	 */
	public static List<Variate> generateDummyVariate(int limit){
		List<Variate> variates = new ArrayList<Variate>();
		for(int i = 0 ; i < limit ; i++){
			Variate variate = new Variate();
			
			variate.setName(i + "name");
			variate.setDescription(i + " description");
			variate.setProperty(i + " property");
			variate.setScale(i + " scale");
			variate.setMethod(i + " method");
			variate.setRole(i + " role");
			variate.setDatatype(i + "Test Data Type");
			variates.add(variate);
		}
		return variates;
	}	
	
	
	/**
	 * Convert xml dataset to workbook.
	 *
	 * @param dataset the dataset
	 * @return the workbook
	 */
	public static Workbook convertXmlDatasetToWorkbook(ParentDataset dataset) {
		Workbook workbook = new Workbook();
		
		if (dataset instanceof Dataset) {
			Dataset nurseryDataset = (Dataset) dataset;
			workbook.setConditions(convertConditionsToMeasurementVariables(nurseryDataset.getConditions()));
			workbook.setFactors(convertFactorsToMeasurementVariables(nurseryDataset.getFactors()));
			workbook.setVariates(convertVariatesToMeasurementVariables(nurseryDataset.getVariates()));
			workbook.setConstants(convertConstantsToMeasurementVariables(nurseryDataset.getConstants()));
		}
		else {
			TrialDataset trialDataset = (TrialDataset) dataset;
			workbook.setConditions(convertConditionsToMeasurementVariables(trialDataset.getConditions()));
			workbook.setFactors(convertFactorsToMeasurementVariables(trialDataset.getFactors()));
			workbook.setVariates(convertVariatesToMeasurementVariables(trialDataset.getVariates()));
			workbook.getConditions().addAll(convertFactorsToMeasurementVariables(trialDataset.getTrialLevelFactor()));
			if (workbook.getTreatmentFactors() == null) {
				workbook.setTreatmentFactors(new ArrayList<TreatmentVariable>());
			}
			workbook.getTreatmentFactors().addAll(convertTreatmentFactorsToTreatmentVariables(trialDataset.getTreatmentFactors()));
		}
		
		return workbook;
	}
	
	/**
	 * Convert workbook to xml dataset.
	 *
	 * @param workbook the workbook
	 * @return the dataset
	 */
	public static ParentDataset convertWorkbookToXmlDataset(Workbook workbook) {
		return convertWorkbookToXmlDataset(workbook, true);
	}
	public static ParentDataset convertWorkbookToXmlDataset(Workbook workbook, boolean isNursery) {
		ParentDataset dataset = null;
		
		if(isNursery){
			Dataset nurseryDataset = new Dataset();
			List<Condition> conditions = convertMeasurementVariablesToConditions(workbook.getConditions());
			List<Factor> factors = convertMeasurementVariablesToFactors(workbook.getFactors());
			List<Variate> variates = convertMeasurementVariablesToVariates(workbook.getVariates());
			List<Constant> constants = convertMeasurementVariablesToConstants(workbook.getConstants());

			nurseryDataset.setConditions(conditions);
			nurseryDataset.setFactors(factors);
			nurseryDataset.setVariates(variates);
			nurseryDataset.setConstants(constants);
			dataset = nurseryDataset;
		}else{
			TrialDataset trialDataset = new TrialDataset();
			
			List<Condition> conditions = convertMeasurementVariablesToConditions(workbook.getStudyConditions());
			List<Factor> factors = convertMeasurementVariablesToFactors(workbook.getFactors());
			List<Variate> variates = convertMeasurementVariablesToVariates(workbook.getVariates());
			List<TreatmentFactor> treatmentFactors = convertTreatmentVariablesToTreatmentFactors(workbook.getTreatmentFactors());
			
			trialDataset.setConditions(conditions);
			trialDataset.setFactors(factors);
			trialDataset.setVariates(variates);
			trialDataset.setTrialLevelFactor(convertMeasurementVariablesToFactors(workbook.getTrialConditions()));
			trialDataset.setTreatmentFactors(treatmentFactors);
			
			dataset = trialDataset;
		}
		return dataset;
	}
	
	/**
	 * Convert measurement variables to conditions.
	 *
	 * @param mlist the mlist
	 * @return the list
	 */
	private static List<Condition> convertMeasurementVariablesToConditions(List<MeasurementVariable> mlist) {
		List<Condition> conditions = new ArrayList<Condition>();
		
		if (mlist != null && !mlist.isEmpty()) {
			for (MeasurementVariable mvar : mlist) {
				Condition condition = new Condition(
						mvar.getName(), 
						mvar.getDescription(), 
						mvar.getProperty(), 
						mvar.getScale(), 
						mvar.getMethod(), 
						PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), 
						mvar.getDataType(), 
						mvar.getValue(), null, null, null);
				condition.setId(mvar.getTermId());
				conditions.add(condition);
			}
		}
		
		return conditions;
	}
	
	private static List<Constant> convertMeasurementVariablesToConstants(List<MeasurementVariable> mlist) {
            List<Constant> constants = new ArrayList<Constant>();
            
            if (mlist != null && !mlist.isEmpty()) {
                    
                    for (MeasurementVariable mvar : mlist) {
                        Constant constant = new Constant(
                                            mvar.getName(), 
                                            mvar.getDescription(), 
                                            mvar.getProperty(), 
                                            mvar.getScale(), 
                                            mvar.getMethod(), 
                                            PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), 
                                            mvar.getDataType(), 
                                            mvar.getValue(), null, null, null);
                        constant.setId(mvar.getTermId());
                        constants.add(constant);
                    }
            }
            
            return constants;
        }
	
	/**
	 * Convert measurement variables to factors.
	 *
	 * @param mlist the mlist
	 * @return the list
	 */
	private static List<Factor> convertMeasurementVariablesToFactors(List<MeasurementVariable> mlist) {
		List<Factor> factors = new ArrayList<Factor>();
		
		if (mlist != null && !mlist.isEmpty()) {
			for (MeasurementVariable mvar : mlist) {
				Factor factor = new Factor(
						mvar.getName(), 
						mvar.getDescription(), 
						mvar.getProperty(), 
						mvar.getScale(), 
						mvar.getMethod(), 
						PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), 
						mvar.getDataType(), mvar.getTermId());
				factor.setTreatmentLabel(mvar.getTreatmentLabel());
				factor.setId(mvar.getTermId());
				factors.add(factor);
			}
		}
		
		return factors;
	}
	
	private static List<TreatmentFactor> convertTreatmentVariablesToTreatmentFactors(List<TreatmentVariable> mlist) {
		List<TreatmentFactor> factors = new ArrayList<TreatmentFactor>();
		
		if (mlist != null && !mlist.isEmpty()) {
			Factor levelFactor, valueFactor;
			for (TreatmentVariable var : mlist) {
				MeasurementVariable mvar = var.getLevelVariable();
				MeasurementVariable vvar = var.getValueVariable();
				levelFactor = new Factor(
						mvar.getName(), 
						mvar.getDescription(), 
						mvar.getProperty(), 
						mvar.getScale(), 
						mvar.getMethod(), 
						PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), 
						mvar.getDataType(), mvar.getTermId());
				valueFactor = new Factor(
						vvar.getName(), 
						vvar.getDescription(), 
						vvar.getProperty(), 
						vvar.getScale(), 
						vvar.getMethod(), 
						PhenotypicType.getPhenotypicTypeForLabel(vvar.getLabel()).toString(), 
						vvar.getDataType(), vvar.getTermId());
				factors.add(new TreatmentFactor(levelFactor, valueFactor));
			}
		}
		
		return factors;
	}
	
	/**
	 * Convert measurement variables to variates.
	 *
	 * @param mlist the mlist
	 * @return the list
	 */
	private static List<Variate> convertMeasurementVariablesToVariates(List<MeasurementVariable> mlist) {
		List<Variate> variates = new ArrayList<Variate>();
		
		if (mlist != null && !mlist.isEmpty()) {
			for (MeasurementVariable mvar : mlist) {
				Variate variate = new Variate(
						mvar.getName(), 
						mvar.getDescription(), 
						mvar.getProperty(), 
						mvar.getScale(), 
						mvar.getMethod(), 
						PhenotypicType.VARIATE.toString(), 
						mvar.getDataType(),
						mvar.getDataTypeId(),
						mvar.getPossibleValues(),
						mvar.getMinRange(),
						mvar.getMaxRange());
				variate.setId(mvar.getTermId());
				variates.add(variate);
			}
		}
		
		return variates;
	}
	
	/**
	 * Convert conditions to measurement variables.
	 *
	 * @param conditions the conditions
	 * @return the list
	 */
	private static List<MeasurementVariable> convertConditionsToMeasurementVariables(List<Condition> conditions) {
		List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
		if (conditions != null && !conditions.isEmpty()) {
			for (Condition condition : conditions) {
				list.add(convertConditionToMeasurementVariable(condition));
			}
		}
		return list;
	}
	
	private static List<MeasurementVariable> convertConstantsToMeasurementVariables(List<Constant> constants) {
    	        List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
                if (constants != null && !constants.isEmpty()) {
                        for (Constant constant : constants) {
                                list.add(convertConstantToMeasurementVariable(constant));
                        }
                }
                return list;
	}
	
	/**
	 * Convert condition to measurement variable.
	 *
	 * @param condition the condition
	 * @return the measurement variable
	 */
	private static MeasurementVariable convertConditionToMeasurementVariable(Condition condition) {
		String label = null;
//		if (condition.getRole() == null) {
//			label = "STUDY";
//		}
//		else {
			label = PhenotypicType.valueOf(condition.getRole()).getLabelList().get(0);
//		}
		MeasurementVariable mvar = new MeasurementVariable(
				condition.getName(), condition.getDescription(), condition.getScale(), condition.getMethod(), condition.getProperty(), condition.getDatatype(), 
				condition.getValue(), /*PhenotypicType.valueOf(condition.getRole()).getLabelList().get(0)*/ label, 
				condition.getMinRange(), condition.getMaxRange());
		mvar.setOperation(condition.getOperation());
		mvar.setTermId(condition.getId());
		mvar.setStoredIn(condition.getStoredIn());
		mvar.setFactor(true);
		mvar.setDataTypeId(condition.getDataTypeId());
		return mvar;
	}
	
	private static MeasurementVariable convertConstantToMeasurementVariable(Constant constant) {
	    String label = null;

            label = PhenotypicType.valueOf(constant.getRole()).getLabelList().get(0);
            
            MeasurementVariable mvar = new MeasurementVariable(
                    constant.getName(), constant.getDescription(), constant.getScale(), constant.getMethod(), constant.getProperty(), constant.getDatatype(), 
                    constant.getValue(), /*PhenotypicType.valueOf(condition.getRole()).getLabelList().get(0)*/ label, 
                    constant.getMinRange(), constant.getMaxRange());
            
            mvar.setOperation(constant.getOperation());
            mvar.setTermId(constant.getId());
            mvar.setStoredIn(constant.getStoredIn());
            mvar.setFactor(false);
            mvar.setDataTypeId(constant.getDataTypeId());
            return mvar;
	}

	/**
	 * Convert factors to measurement variables.
	 *
	 * @param factors the factors
	 * @return the list
	 */
	private static List<MeasurementVariable> convertFactorsToMeasurementVariables(List<Factor> factors) {
		List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
		if (factors != null && !factors.isEmpty()) {
			for (Factor factor : factors) {
				list.add(convertFactorToMeasurementVariable(factor));
			}
		}
		return list;
	}
	
	/**
	 * Convert factor to measurement variable.
	 *
	 * @param factor the factor
	 * @return the measurement variable
	 */
	private static MeasurementVariable convertFactorToMeasurementVariable(Factor factor) {
		MeasurementVariable mvar = new MeasurementVariable(
				factor.getName(), factor.getDescription(), factor.getScale(), factor.getMethod(), factor.getProperty(), factor.getDatatype(), null, 
				PhenotypicType.valueOf(factor.getRole()).getLabelList().get(0));
		mvar.setFactor(true);
		mvar.setOperation(factor.getOperation());
		mvar.setStoredIn(factor.getStoredIn());
		mvar.setTermId(factor.getId());
		mvar.setTreatmentLabel(factor.getTreatmentLabel());
		return mvar;
	}

	private static List<TreatmentVariable> convertTreatmentFactorsToTreatmentVariables(List<TreatmentFactor> factors) {
		List<TreatmentVariable> list = new ArrayList<TreatmentVariable>();
		if (factors != null && !factors.isEmpty()) {
			for (TreatmentFactor factor : factors) {
				list.add(convertTreatmentFactorToTreatmentVariable(factor));
			}
		}
		return list;
	}

	private static TreatmentVariable convertTreatmentFactorToTreatmentVariable(TreatmentFactor factor) {
		TreatmentVariable mvar = new TreatmentVariable();
		MeasurementVariable levelVariable = convertFactorToMeasurementVariable(factor.getLevelFactor());
		MeasurementVariable valueVariable = convertFactorToMeasurementVariable(factor.getValueFactor());
		levelVariable.setValue(factor.getLevelNumber() != null ? factor.getLevelNumber().toString() : null);
		levelVariable.setTreatmentLabel(factor.getLevelFactor().getName());
		valueVariable.setValue(factor.getValue());
		valueVariable.setTreatmentLabel(factor.getLevelFactor().getName());
		mvar.setLevelVariable(levelVariable);
		mvar.setValueVariable(valueVariable);
		return mvar;
	}

	/**
	 * Convert variates to measurement variables.
	 *
	 * @param variates the variates
	 * @return the list
	 */
	private static List<MeasurementVariable> convertVariatesToMeasurementVariables(List<Variate> variates) {
		List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
		if (variates != null && !variates.isEmpty()) {
			for (Variate variate : variates) {
				list.add(convertVariateToMeasurementVariable(variate));
			}
		}
		return list;
	}
	
	/**
	 * Convert variate to measurement variable.
	 *
	 * @param variate the variate
	 * @return the measurement variable
	 */
	private static MeasurementVariable convertVariateToMeasurementVariable(Variate variate) {
		MeasurementVariable mvar = new MeasurementVariable(
				variate.getName(), variate.getDescription(), variate.getScale(), variate.getMethod(), variate.getProperty(), variate.getDatatype(), null, 
				PhenotypicType.TRIAL_DESIGN.getLabelList().get(0), variate.getMinRange(), variate.getMaxRange()); //because variates are mostly PLOT variables
		mvar.setOperation(variate.getOperation());
		mvar.setTermId(variate.getId());
		mvar.setStoredIn(variate.getStoredIn());
		mvar.setFactor(false);
		mvar.setDataTypeId(variate.getDataTypeId());
		mvar.setPossibleValues(variate.getPossibleValues());
		return mvar;
	}
	
	private static SettingDetail createTreatmentFactor(Factor factor, org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, 
			FieldbookService fieldbookService, int group, UserSelection userSelection) throws MiddlewareQueryException {
		
		SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(),
				factor.getProperty(), factor.getScale(), factor.getMethod(), factor.getRole(),
				factor.getDatatype());
		StandardVariable standardVariable = getStandardVariable(factor.getTermId(), userSelection, fieldbookMiddlewareService);
                variable.setPSMRFromStandardVariable(standardVariable);
		variable.setCvTermId(standardVariable.getId());
		List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, standardVariable.getId());
		SettingDetail settingDetail = new SettingDetail(variable, possibleValues, null, true);
		settingDetail.setPossibleValuesToJson(possibleValues);
		settingDetail.setGroup(group);
		settingDetail.setDeletable(true);
		
		return settingDetail;
	}
	
	private static Factor createFactor(SettingDetail settingDetail, UserSelection userSelection, 
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, int index) {
		
		SettingVariable variable = settingDetail.getVariable();
		if (userSelection != null) {
			StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
			variable.setPSMRFromStandardVariable(standardVariable);
			//need to get the name from the session
			variable.setName(userSelection.getTreatmentFactors().get(index).getVariable().getName());
		}
		Factor factor = new Factor(variable.getName(), variable.getDescription(), variable.getProperty(),
				variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(), variable.getCvTermId());
		return factor;
	}
	
	private static int getTreatmentGroup(UserSelection userSelection, List<SettingDetail> treatmentFactorList, int index) {
		Integer currentGroup;
		if (userSelection != null) {
			currentGroup = userSelection.getTreatmentFactors().get(index).getGroup();
		}
		else {
			currentGroup = treatmentFactorList.get(index).getGroup();
		}
		return currentGroup != null ? currentGroup : -1;
	}
	
	private static TreatmentFactor convertTreatmentFactorDetailToTreatmentFactor(TreatmentFactorDetail detail, UserSelection userSelection,
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		
		Factor levelFactor = createFactor(detail.getLevelId(), detail.getLevelName(), userSelection, fieldbookMiddlewareService);
		Factor valueFactor = createFactor(detail.getAmountId(), detail.getAmountName(), userSelection, fieldbookMiddlewareService);
		levelFactor.setTreatmentLabel(detail.getLevelName());
		valueFactor.setTreatmentLabel(detail.getLevelName());
		
		return new TreatmentFactor(levelFactor, valueFactor, Integer.valueOf(detail.getLevelValue()), detail.getAmountValue());
	}

	private static Factor createFactor(int id, String name, UserSelection userSelection, 
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		
		StandardVariable variable = null;
		if (userSelection != null) {
			variable = getStandardVariable(id, userSelection, fieldbookMiddlewareService);
		}
		if (variable != null) {
			return  new Factor(name, variable.getDescription(), variable.getProperty().getName(),
					variable.getScale().getName(), variable.getMethod().getName(), 
					variable.getPhenotypicType().name(), variable.getDataType().getName(), id);
		}
		return null;
	}
	
	public static NurseryDetails convertWorkbookToNurseryDetails(Workbook workbook, org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
			FieldbookService fieldbookService, UserSelection userSelection) 
	throws MiddlewareQueryException {
		
		NurseryDetails nurseryDetails = convertWorkbookStudyLevelVariablesToNurserDetails(workbook, 
				fieldbookMiddlewareService, fieldbookService, userSelection, workbook.getStudyId().toString());
		
		nurseryDetails.setFactorDetails(convertWorkbookFactorsToSettingDetails(workbook.getNonTrialFactors(), fieldbookMiddlewareService));
		List<SettingDetail> traits = new ArrayList<SettingDetail>();
		List<SettingDetail> selectionVariateDetails = new ArrayList<SettingDetail>();
		convertWorkbookVariatesToSettingDetails(workbook.getVariates(), fieldbookMiddlewareService, fieldbookService, traits, selectionVariateDetails);
		nurseryDetails.setVariateDetails(traits);
		nurseryDetails.setSelectionVariateDetails(selectionVariateDetails);
		
		return nurseryDetails;
	}
	
	private static NurseryDetails convertWorkbookStudyLevelVariablesToNurserDetails(Workbook workbook, 
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
			FieldbookService fieldbookService, UserSelection userSelection, String projectId) 
	throws MiddlewareQueryException {
		
		NurseryDetails details = new NurseryDetails();
		details.setId(workbook.getStudyId());
		List<MeasurementVariable> conditions = workbook.getConditions();
		List<MeasurementVariable> constants = workbook.getConstants();
		
		List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
		List<SettingDetail> managementDetails = new ArrayList<SettingDetail>();
		List<SettingDetail> nurseryConditionDetails = new ArrayList<SettingDetail>();
		
		List<String> basicFields = Arrays.asList(AppConstants.NURSERY_BASIC_REQUIRED_FIELDS.getString().split(","));
		
	    if(conditions != null){
	    	MeasurementVariable studyName = WorkbookUtil.getMeasurementVariable(conditions, TermId.STUDY_NAME.getId());
	    	if (studyName != null) {
	    		details.setName(studyName.getValue());
	    	}
	    	basicDetails = convertWorkbookToSettingDetails(basicFields, conditions, fieldbookMiddlewareService, fieldbookService, userSelection, workbook);
	    	managementDetails = convertWorkbookOtherStudyVariablesToSettingDetails(conditions, managementDetails.size(), userSelection, fieldbookMiddlewareService, fieldbookService);
	    	nurseryConditionDetails = convertWorkbookOtherStudyVariablesToSettingDetails(constants, 1, userSelection, fieldbookMiddlewareService, fieldbookService, true);
	    }
		
		details.setBasicStudyDetails(basicDetails);
		details.setManagementDetails(managementDetails);
		details.setNurseryConditionDetails(nurseryConditionDetails);
		return details;
	}
	
	private static List<SettingDetail> convertWorkbookOtherStudyVariablesToSettingDetails(List<MeasurementVariable> conditions, int index, 
			UserSelection userSelection,  
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, FieldbookService fieldbookService) 
	throws MiddlewareQueryException {
		return convertWorkbookOtherStudyVariablesToSettingDetails(conditions, index, userSelection, fieldbookMiddlewareService, fieldbookService, false);
	}
	
	private static List<SettingDetail> convertWorkbookOtherStudyVariablesToSettingDetails(List<MeasurementVariable> conditions, int index, 
			UserSelection userSelection,  
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, FieldbookService fieldbookService, boolean isVariate) 
	throws MiddlewareQueryException {
		
		List<SettingDetail> details = new ArrayList<SettingDetail>();
		List<String> basicFields = Arrays.asList(AppConstants.NURSERY_BASIC_REQUIRED_FIELDS.getString().split(","));
		List<String> hiddenFields = Arrays.asList(AppConstants.HIDDEN_FIELDS.getString().split(","));
		
		if (conditions != null) {
			for (MeasurementVariable condition : conditions) {
				String id = String.valueOf(condition.getTermId());
				String role = (isVariate) ? PhenotypicType.VARIATE.toString() : PhenotypicType.getPhenotypicTypeForLabel(condition.getLabel()).toString();
				if (!basicFields.contains(id) && !hiddenFields.contains(id)) {
					SettingVariable variable = getSettingVariable(condition.getName(), condition.getDescription(), condition.getProperty(),
							condition.getScale(), condition.getMethod(), role, 
							condition.getDataType(), condition.getDataTypeId(), condition.getMinRange(), condition.getMaxRange(), userSelection, fieldbookMiddlewareService);
					variable.setCvTermId(condition.getTermId());
					String value = fieldbookService.getValue(variable.getCvTermId(), HtmlUtils.htmlUnescape(condition.getValue()), 
							condition.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId());
					SettingDetail settingDetail = new SettingDetail(variable, null,	HtmlUtils.htmlUnescape(value), false);
					index = addToList(details, settingDetail, index, null, null);
				}
			}
		}
		return details;
	}
	
	private static List<SettingDetail> convertWorkbookToSettingDetails(List<String> fields, List<MeasurementVariable> conditions,
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, FieldbookService fieldbookService,
			UserSelection userSelection, Workbook workbook) 
	throws MiddlewareQueryException {
		
		List<SettingDetail> details = new ArrayList<SettingDetail>();
		int index = fields != null ? fields.size() : 0;
    	MeasurementVariable studyNameVar = WorkbookUtil.getMeasurementVariable(workbook.getConditions(), TermId.STUDY_NAME.getId());
    	String studyName = studyNameVar != null ? studyNameVar.getValue() : "";
		int datasetId = fieldbookMiddlewareService.getMeasurementDatasetId(workbook.getStudyId(), studyName);
    	for (String strFieldId : fields) {
    		if (strFieldId != null && !"".equals(strFieldId)) {
	    		boolean found = false;
				String label = AppConstants.getString(strFieldId + "_LABEL");
				if (conditions != null) {
		    		for (MeasurementVariable condition : conditions) {
		    			if (NumberUtils.isNumber(strFieldId) ) {
			    			if (condition.getTermId() == Integer.valueOf(strFieldId)) {
								if (label == null || "".equals(label.trim())) {
									label = condition.getName();
								}
								if (NumberUtils.isNumber(strFieldId) ) {
									SettingVariable variable = getSettingVariable(label, condition.getDescription(), condition.getProperty(),
											condition.getScale(), condition.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(condition.getLabel()).toString(), 
											condition.getDataType(), condition.getDataTypeId(), condition.getMinRange(), condition.getMaxRange(), userSelection, fieldbookMiddlewareService);
									variable.setCvTermId(Integer.valueOf(strFieldId));						
									String value = fieldbookService.getValue(variable.getCvTermId(), HtmlUtils.htmlUnescape(condition.getValue()), 
											condition.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId());
									SettingDetail settingDetail = new SettingDetail(variable, null,
											HtmlUtils.htmlUnescape(value), false);
				    				index = addToList(details, settingDetail, index, fields, strFieldId);
				    			}
								found = true;
								break;
			    			}
		    			}
		    			else { //special field
		        			SettingVariable variable = new SettingVariable(label, null, null, null, null, null, null, null, null, null);
		        			String value = getSpecialFieldValue(strFieldId, datasetId, fieldbookMiddlewareService);
		        			SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
		        			index = addToList(details, settingDetail, index, fields, strFieldId);
		        			found = true;
		        			break;
		    			}
		    		}
				}
	    		if (!found) { //required field but has no value
	    			SettingVariable variable = new SettingVariable(label, null, null, null, null, null, null, null, null, null);
	    			SettingDetail settingDetail = new SettingDetail(variable, null, "", false);
	    			index = addToList(details, settingDetail, index, fields, strFieldId);
	    		}
    		}
    	}
    	return details;
	}
	
	private static String getSpecialFieldValue(String specialFieldLabel, int datasetId,
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) 
	throws MiddlewareQueryException {
		
		if (AppConstants.SPFLD_ENTRIES.getString().equals(specialFieldLabel)) {
			long count = fieldbookMiddlewareService.countStocks(datasetId);
			return String.valueOf(count);
		}
		else if (AppConstants.SPFLD_HAS_FIELDMAP.getString().equals(specialFieldLabel)) {
			return fieldbookMiddlewareService.hasFieldMap(datasetId) ? "Yes" : "No"; 
		}
		else if (AppConstants.SPFLD_HAS_MEASUREMENTS.getString().equals(specialFieldLabel)) {
			long count = fieldbookMiddlewareService.countObservations(datasetId);
			return count > 0 ? "Yes" : "No";
		}
		return "";
	}
	
	private static List<SettingDetail> convertWorkbookFactorsToSettingDetails(List<MeasurementVariable> factors,
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) 
	throws MiddlewareQueryException {
		
		List<SettingDetail> plotsLevelList = new ArrayList<SettingDetail>();
		if (factors != null) {
			for (MeasurementVariable factor : factors){
				SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
						factor.getScale(), factor.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(factor.getLabel()).toString(), 
						factor.getDataType());
				Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
						HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), 
						HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
				
				if (!inHideVariableFields(stdVar, AppConstants.HIDE_PLOT_FIELDS.getString())) {
    					variable.setCvTermId(stdVar);
    					SettingDetail settingDetail = new SettingDetail(variable,
    							null, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()));
    					plotsLevelList.add(settingDetail);
				}
			}
		}
		return plotsLevelList;
	}
	
	public static void convertWorkbookVariatesToSettingDetails(List<MeasurementVariable> variates,
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
			FieldbookService fieldbookService, List<SettingDetail> traits, List<SettingDetail> selectedVariates) 
	throws MiddlewareQueryException {

		List<String> svProperties = getSelectedVariatesPropertyNames(fieldbookService);
		
		if (variates != null) {
			for (MeasurementVariable variate : variates){
				SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
						variate.getScale(), variate.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variate.getLabel()).toString(), 
						variate.getDataType());
				Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), 
						HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), 
						PhenotypicType.VARIATE);
				variable.setCvTermId(stdVar);
				SettingDetail settingDetail = new SettingDetail(variable, null, null, true);
				if (svProperties.contains(variate.getProperty())) {
					selectedVariates.add(settingDetail);
				}
				else {
					traits.add(settingDetail);
				}
			}
		}
	}
	
	private static List<String> getSelectedVariatesPropertyNames(FieldbookService fieldbookService) throws MiddlewareQueryException {
		List<String> names = new ArrayList<String>();
		List<String> ids = Arrays.asList(AppConstants.SELECTION_VARIATES_PROPERTIES.getString().split(","));
		for (String id : ids) {
			Term term = fieldbookService.getTermById(Integer.valueOf(id));
			if (term != null) {
				names.add(term.getName());
			}
		}
		return names;
	}
	
	private static SettingVariable getSettingVariable(String name, String description, String property, String scale, String method, String role, String dataType, 
			Integer dataTypeId, Double minRange, Double maxRange, UserSelection userSelection, 
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) 
	throws MiddlewareQueryException {
		
		SettingVariable variable = new SettingVariable(name, description, property, scale, method, role, dataType, dataTypeId, minRange, maxRange);
		
		Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
				HtmlUtils.htmlUnescape(variable.getProperty()), 
				HtmlUtils.htmlUnescape(variable.getScale()), 
				HtmlUtils.htmlUnescape(variable.getMethod()), 
				PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
		variable.setCvTermId(stdVar);
		if (userSelection != null) {
			StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);						
			variable.setPSMRFromStandardVariable(standardVariable);
			stdVar = standardVariable.getId();
		}
		
		return variable;
	}
	
	private static int addToList(List<SettingDetail> list, SettingDetail settingDetail, int index, List<String> fields, String idString) {
		int order = -1;
		if (fields != null) {
			order = fields.indexOf(idString);
		}
		settingDetail.setOrder(order > -1 ? order : index++);
		list.add(settingDetail);
		
		return index;
	}
}
