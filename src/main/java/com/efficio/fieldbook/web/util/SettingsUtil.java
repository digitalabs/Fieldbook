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

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.pojoxml.core.PojoXml;
import org.pojoxml.core.PojoXmlFactory;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.demo.bean.TestJavaBean;
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
	 * Convert pojo to xml dataset.
	 *
	 * @param name the name
	 * @param nurseryLevelConditions the nursery level conditions
	 * @param plotsLevelList the plots level list
	 * @param baselineTraitsList the baseline traits list
	 * @return the dataset
	 */
	public static Dataset convertPojoToXmlDataset(String name, List<SettingDetail> nurseryLevelConditions, List<SettingDetail> plotsLevelList, List<SettingDetail> baselineTraitsList){
		Dataset dataset = new Dataset();
		List<Condition> conditions = new ArrayList<Condition>();
		List<Factor> factors = new ArrayList<Factor>();
		List<Variate> variates = new ArrayList<Variate>();
		//iterate for the nursery level
		for(SettingDetail settingDetail : nurseryLevelConditions){
			SettingVariable variable = settingDetail.getVariable();
			Condition condition = new Condition(variable.getName(), variable.getDescription(), variable.getProperty(),
					variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
					settingDetail.getValue());
			conditions.add(condition);
		}
		//iterate for the plot level
		for(SettingDetail settingDetail : plotsLevelList){
			SettingVariable variable = settingDetail.getVariable();
			Factor factor = new Factor(variable.getName(), variable.getDescription(), variable.getProperty(),
					variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType());
			factors.add(factor);
		}
		//iterate for the baseline traits level
		for(SettingDetail settingDetail : baselineTraitsList){
			SettingVariable variable = settingDetail.getVariable();
			Variate variate = new Variate(variable.getName(), variable.getDescription(), variable.getProperty(),
					variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType());
			variates.add(variate);
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
	public static List<ValueReference> getFieldPossibleVales(FieldbookService fieldbookService, SettingVariable variable){
		List<ValueReference> possibleValueList = new ArrayList<ValueReference>();
		
		try {
		
			possibleValueList = fieldbookService.getAllPossibleValuesByPSMR(variable.getProperty(), variable.getScale(), variable.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variable.getRole()));
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
	public static boolean isSettingVariableDeletable(SettingVariable variable){
		//need to add the checking here if the specific PSM-R is deletable, for the nursery level details
		return true;
	}
	
	/**
	 * Convert xml dataset to pojo.
	 *
	 * @param fieldbookService the fieldbook service
	 * @param dataset the dataset
	 * @param userSelection the user selection
	 */
	public static void convertXmlDatasetToPojo(com.efficio.fieldbook.service.api.FieldbookService fieldbookService, Dataset dataset, UserSelection userSelection){
		if(dataset != null && userSelection != null){
			//we copy it to User session object
			//nursery level
   		    List<SettingDetail> nurseryLevelConditions = new ArrayList<SettingDetail>();
		    List<SettingDetail> plotsLevelList  = new ArrayList<SettingDetail>();
		    List<SettingDetail> baselineTraitsList  = new ArrayList<SettingDetail>();
			for(Condition condition : dataset.getConditions()){
				
				SettingVariable variable = new SettingVariable(condition.getName(), condition.getDescription(), condition.getProperty(),
						condition.getScale(), condition.getMethod(), condition.getRole(), condition.getDatatype());
				
				SettingDetail settingDetail = new SettingDetail(variable,
						getFieldPossibleVales(fieldbookService, variable), condition.getValue(), isSettingVariableDeletable(variable));
				nurseryLevelConditions.add(settingDetail);
			}
			//plot level
			//always allowed to be deleted
			for(Factor factor : dataset.getFactors()){
				
				SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
						factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
				
				SettingDetail settingDetail = new SettingDetail(variable,
						null, null, true);
				plotsLevelList.add(settingDetail);
			}
			//baseline traits
			//always allowed to be deleted
			for(Variate variate : dataset.getVariates()){
				
				SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
						variate.getScale(), variate.getMethod(), variate.getRole(), variate.getDatatype());
				
				SettingDetail settingDetail = new SettingDetail(variable,
						null, null, true);
				baselineTraitsList.add(settingDetail);
			}
			
			userSelection.setNurseryLevelConditions(nurseryLevelConditions);
			userSelection.setPlotsLevelList(plotsLevelList);			
			userSelection.setBaselineTraitsList(baselineTraitsList);
		}
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
}
