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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.pojoxml.core.PojoXml;
import org.pojoxml.core.PojoXmlFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;


/**
 * The Class SettingsUtil.
 */
public class SettingsUtil {
	
	/**
	 * Generate settings xml.
	 *
	 * @param dataset the dataset
	 * @return the string
	 */
	public static String generateSettingsXml(Dataset dataset){
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();
		
        setupPojoXml(pojoXml);
		
        String xml = pojoXml.getXml(dataset);
        //pojoXml.saveXml(dataset,"testdataset.xml");
        //Employee employee = (Employee) pojoXml.getPojoFrormFile(fullPathNamen,Employee.class);
        return xml;
	}
	
	/**
	 * Parses the xml to dataset pojo.
	 *
	 * @param xml the xml
	 * @return the dataset
	 */
	public static Dataset parseXmlToDatasetPojo(String xml){
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();
		setupPojoXml(pojoXml);
		String filename = System.currentTimeMillis() + ".tmp";
		File file = new File(filename);
		 
		// if file doesnt exists, then create it
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
				
	
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(xml);
			bw.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Dataset dataset  = (Dataset) pojoXml.getPojoFromFile(file.getAbsolutePath(), Dataset.class);
		
		if(file.exists()){
			file.delete();
		}
		return dataset;
	}
	
	/**
	 * Sets the up pojo xml.
	 *
	 * @param pojoXml the new up pojo xml
	 */
	private static void setupPojoXml(PojoXml pojoXml){
		pojoXml.addClassAlias(Dataset.class, "dataset");
		pojoXml.addClassAlias(Condition.class, "condition");
		pojoXml.addClassAlias(Variate.class, "variate");
		pojoXml.addClassAlias(Factor.class, "factor");
		
		
		pojoXml.addCollectionClass("condition",Condition.class);
		pojoXml.addCollectionClass("factor",Factor.class);
		pojoXml.addCollectionClass("variate",Variate.class);
	}
	
	/**
     * Get standard variable.
     * @param id
     * @return
     * @throws MiddlewareQueryException
     */
    private static StandardVariable getStandardVariable(int id, UserSelection userSelection, org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
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
	 * @param name the name
	 * @param nurseryLevelConditions the nursery level conditions
	 * @param plotsLevelList the plots level list
	 * @param baselineTraitsList the baseline traits list
	 * @return the dataset
	 */
	public static Dataset convertPojoToXmlDataset(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, String name, List<SettingDetail> nurseryLevelConditions, List<SettingDetail> plotsLevelList, List<SettingDetail> baselineTraitsList, UserSelection userSelection){
		Dataset dataset = new Dataset();
		List<Condition> conditions = new ArrayList<Condition>();
		List<Factor> factors = new ArrayList<Factor>();
		List<Variate> variates = new ArrayList<Variate>();
		//iterate for the nursery level
		int index = 0;
		for(SettingDetail settingDetail : nurseryLevelConditions){
			SettingVariable variable = settingDetail.getVariable();
			if(userSelection != null){
				StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
				
				variable.setPSMRFromStandardVariable(standardVariable);
				//need to get the name from the session
				variable.setName(userSelection.getNurseryLevelConditions().get(index++).getVariable().getName());
			}
			
			Condition condition = new Condition(variable.getName(), variable.getDescription(), variable.getProperty(),
					variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
					settingDetail.getValue(), variable.getDataTypeId(), variable.getMinRange(), variable.getMaxRange());
			conditions.add(condition);
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
					variable.setName(userSelection.getPlotsLevelList().get(index++).getVariable().getName());
				}
				Factor factor = new Factor(variable.getName(), variable.getDescription(), variable.getProperty(),
						variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType());
				factors.add(factor);
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
					variable.setName(userSelection.getBaselineTraitsList().get(index++).getVariable().getName());
					
				}
				Variate variate = new Variate(variable.getName(), variable.getDescription(), variable.getProperty(),
						variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType());
				variates.add(variate);
			}
		}
		dataset.setConditions(conditions);
		dataset.setFactors(factors);
		dataset.setVariates(variates);
		dataset.setName(name);
		return dataset;
	}
	
	/**
	 * Gets the field possible vales.
	 *
	 * @param fieldbookService the fieldbook service
	 * @param variable the variable
	 * @return the field possible vales
	 */
	public static List<ValueReference> getFieldPossibleVales(FieldbookService fieldbookService, Integer standardVariableId){
		List<ValueReference> possibleValueList = new ArrayList<ValueReference>();
		
		try {
		
			//possibleValueList = fieldbookService.getAllPossibleValuesByPSMR(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variable.getRole()));
			possibleValueList = fieldbookService.getAllPossibleValues(standardVariableId);
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return possibleValueList;
	}
	
	private static List<ValueReference> getFieldPossibleValuesFavorite(FieldbookService fieldbookService, Integer standardVariableId, String projectId) {
	    List<ValueReference> possibleValueList = new ArrayList<ValueReference>();
            
            try {
            
                    //possibleValueList = fieldbookService.getAllPossibleValuesByPSMR(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variable.getRole()));
                    possibleValueList = fieldbookService.getAllPossibleValuesFavorite(standardVariableId, projectId);
            } catch (MiddlewareQueryException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            return possibleValueList;
	}
	
	/**
	 * Checks if is setting variable deletable.
	 *
	 * @param variable the variable
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
	 * @param fieldbookService the fieldbook service
	 * @param dataset the dataset
	 * @param userSelection the user selection
	 * @throws MiddlewareQueryException 
	 */
	public static void convertXmlDatasetToPojo(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, com.efficio.fieldbook.service.api.FieldbookService fieldbookService, Dataset dataset, UserSelection userSelection, String projectId) throws MiddlewareQueryException{
		if(dataset != null && userSelection != null){
			//we copy it to User session object
			//nursery level
   		    List<SettingDetail> nurseryLevelConditions = new ArrayList<SettingDetail>();
		    List<SettingDetail> plotsLevelList  = new ArrayList<SettingDetail>();
		    List<SettingDetail> baselineTraitsList  = new ArrayList<SettingDetail>();
		    if(dataset.getConditions() != null){
				for(Condition condition : dataset.getConditions()){
					
					SettingVariable variable = new SettingVariable(condition.getName(), condition.getDescription(), condition.getProperty(),
							condition.getScale(), condition.getMethod(), condition.getRole(), condition.getDatatype(), condition.getDataTypeId(),
							condition.getMinRange(), condition.getMaxRange());
					Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.valueOf(variable.getRole()));
					
					if (!inHideNurseryFields(stdVar)) {
					        System.out.println(stdVar);
					        System.out.println(condition.getName());
        					variable.setCvTermId(stdVar);										
        					List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
        					SettingDetail settingDetail = new SettingDetail(variable,
        							possibleValues, condition.getValue(), isSettingVariableDeletable(stdVar, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()));
        					
        					settingDetail.setPossibleValuesToJson(possibleValues);
        					List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, projectId);
        					settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
        					nurseryLevelConditions.add(settingDetail);
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
					
					SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
							factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
					Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.valueOf(variable.getRole()));
					variable.setCvTermId(stdVar);
					SettingDetail settingDetail = new SettingDetail(variable,
							null, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()));
					plotsLevelList.add(settingDetail);
					/*
					if(userSelection != null){
						StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);						
						variable.setPSMRFromStandardVariable(standardVariable);						
					}
					*/
				}
			}
			//baseline traits
			//always allowed to be deleted
			if(dataset.getVariates() != null){
				for(Variate variate : dataset.getVariates()){
					
					SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
							variate.getScale(), variate.getMethod(), variate.getRole(), variate.getDatatype());
					Integer  stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.valueOf(variable.getRole()));
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
			
			userSelection.setNurseryLevelConditions(nurseryLevelConditions);
			userSelection.setPlotsLevelList(plotsLevelList);			
			userSelection.setBaselineTraitsList(baselineTraitsList);
		}
	}
	
	private static boolean inHideNurseryFields(Integer stdVarId) {
	    StringTokenizer token = new StringTokenizer(AppConstants.HIDE_NURSERY_FIELDS.getString(), ",");
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
		List<Condition> conditions = new ArrayList();
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
		List<Factor> factors = new ArrayList();
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
		List<Variate> variates = new ArrayList();
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
	 * Generate dummy settings xml.
	 *
	 * @param dataset the dataset
	 * @return the string
	 */
	public static String generateDummySettingsXml(Dataset dataset){
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();

        setupPojoXml(pojoXml);
		
        String xml = pojoXml.getXml(dataset);
        pojoXml.saveXml(dataset,"testdataset.xml");
        //Employee employee = (Employee) pojoXml.getPojoFrormFile(fullPathNamen,Employee.class);
        return xml;
	}
	
	/**
	 * Parses the dummy xml to dataset pojo.
	 *
	 * @return the dataset
	 */
	public static Dataset parseDummyXmlToDatasetPojo(){
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();
		setupPojoXml(pojoXml);
		Dataset dataset  = (Dataset) pojoXml.getPojoFromFile("testdataset.xml",Dataset.class);
		return dataset;
	}
	
	public static Workbook convertXmlDatasetToWorkbook(Dataset dataset) {
		Workbook workbook = new Workbook();
		
		workbook.setConditions(convertConditionsToMeasurementVariables(dataset.getConditions()));
		workbook.setFactors(convertFactorsToMeasurementVariables(dataset.getFactors()));
		workbook.setVariates(convertVariatesToMeasurementVariables(dataset.getVariates()));
		
		return workbook;
	}
	
	public static Dataset convertWorkbookToXmlDataset(Workbook workbook) {
		Dataset dataset = new Dataset();
		
		dataset.setConditions(convertMeasurementVariablesToConditions(workbook.getConditions()));
		dataset.setFactors(convertMeasurementVariablesToFactors(workbook.getFactors()));
		dataset.setVariates(convertMeasurementVariablesToVariates(workbook.getVariates()));
		
		return dataset;
	}
	
	private static List<Condition> convertMeasurementVariablesToConditions(List<MeasurementVariable> mlist) {
		List<Condition> conditions = new ArrayList<Condition>();
		
		if (mlist != null && !mlist.isEmpty()) {
			for (MeasurementVariable mvar : mlist) {
				conditions.add(new Condition(
						mvar.getName(), 
						mvar.getDescription(), 
						mvar.getProperty(), 
						mvar.getScale(), 
						mvar.getMethod(), 
						PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), 
						mvar.getDataType(), 
						mvar.getValue(), null, null, null));
			}
		}
		
		return conditions;
	}
	private static List<Factor> convertMeasurementVariablesToFactors(List<MeasurementVariable> mlist) {
		List<Factor> factors = new ArrayList<Factor>();
		
		if (mlist != null && !mlist.isEmpty()) {
			for (MeasurementVariable mvar : mlist) {
				factors.add(new Factor(
						mvar.getName(), 
						mvar.getDescription(), 
						mvar.getProperty(), 
						mvar.getScale(), 
						mvar.getMethod(), 
						PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(), 
						mvar.getDataType()));
			}
		}
		
		return factors;
	}
	private static List<Variate> convertMeasurementVariablesToVariates(List<MeasurementVariable> mlist) {
		List<Variate> variates = new ArrayList<Variate>();
		
		if (mlist != null && !mlist.isEmpty()) {
			for (MeasurementVariable mvar : mlist) {
				variates.add(new Variate(
						mvar.getName(), 
						mvar.getDescription(), 
						mvar.getProperty(), 
						mvar.getScale(), 
						mvar.getMethod(), 
						PhenotypicType.VARIATE.toString(), 
						mvar.getDataType()));
			}
		}
		
		return variates;
	}
	
	private static List<MeasurementVariable> convertConditionsToMeasurementVariables(List<Condition> conditions) {
		List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
		if (conditions != null && !conditions.isEmpty()) {
			for (Condition condition : conditions) {
				list.add(convertConditionToMeasurementVariable(condition));
			}
		}
		return list;
	}
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
				condition.getValue(), /*PhenotypicType.valueOf(condition.getRole()).getLabelList().get(0)*/ label);
		mvar.setFactor(true);
		return mvar;
	}

	private static List<MeasurementVariable> convertFactorsToMeasurementVariables(List<Factor> factors) {
		List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
		if (factors != null && !factors.isEmpty()) {
			for (Factor factor : factors) {
				list.add(convertFactorToMeasurementVariable(factor));
			}
		}
		return list;
	}
	private static MeasurementVariable convertFactorToMeasurementVariable(Factor factor) {
		MeasurementVariable mvar = new MeasurementVariable(
				factor.getName(), factor.getDescription(), factor.getScale(), factor.getMethod(), factor.getProperty(), factor.getDatatype(), null, 
				PhenotypicType.valueOf(factor.getRole()).getLabelList().get(0));
		mvar.setFactor(true);
		return mvar;
	}

	private static List<MeasurementVariable> convertVariatesToMeasurementVariables(List<Variate> variates) {
		List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
		if (variates != null && !variates.isEmpty()) {
			for (Variate variate : variates) {
				list.add(convertVariateToMeasurementVariable(variate));
			}
		}
		return list;
	}
	private static MeasurementVariable convertVariateToMeasurementVariable(Variate variate) {
		MeasurementVariable mvar = new MeasurementVariable(
				variate.getName(), variate.getDescription(), variate.getScale(), variate.getMethod(), variate.getProperty(), variate.getDatatype(), null, 
				PhenotypicType.TRIAL_DESIGN.getLabelList().get(0)); //because variates are mostly PLOT variables
		mvar.setFactor(false);
		return mvar;
	}
	
}
