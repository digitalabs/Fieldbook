package com.efficio.fieldbook.web.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.settings.Condition;
import org.generationcp.middleware.domain.fieldbook.settings.Dataset;
import org.generationcp.middleware.domain.fieldbook.settings.Factor;
import org.generationcp.middleware.domain.fieldbook.settings.Variate;
import org.pojoxml.core.PojoXml;
import org.pojoxml.core.PojoXmlFactory;

import com.efficio.fieldbook.web.demo.bean.TestJavaBean;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;

public class SettingsUtil {
	public static String generateSettingsXml(Dataset dataset){
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();

        setupPojoXml(pojoXml);
		
        String xml = pojoXml.getXml(dataset);
        //pojoXml.saveXml(dataset,"testdataset.xml");
        //Employee employee = (Employee) pojoXml.getPojoFrormFile(fullPathNamen,Employee.class);
        return xml;
	}
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
	
	private static void setupPojoXml(PojoXml pojoXml){
		pojoXml.addClassAlias(Dataset.class, "dataset");
		pojoXml.addClassAlias(Condition.class, "condition");
		pojoXml.addClassAlias(Variate.class, "variate");
		pojoXml.addClassAlias(Factor.class, "factor");
		
		
		pojoXml.addCollectionClass("condition",Condition.class);
		pojoXml.addCollectionClass("factor",Factor.class);
		pojoXml.addCollectionClass("variate",Variate.class);
	}
	
	public static Dataset convertPojoToXmlDataset(List<SettingDetail> nurseryLevelConditions, List<SettingDetail> plotsLevelList, List<SettingDetail> baselineTraitsList){
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
			conditions.add(condition);
		}
		//iterate for the baseline traits level
		for(SettingDetail settingDetail : baselineTraitsList){
			SettingVariable variable = settingDetail.getVariable();
			Variate variate = new Variate(variable.getName(), variable.getDescription(), variable.getProperty(),
					variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType());
			conditions.add(condition);
		}
		dataset.setConditions(conditions);
		dataset.setFactors(factors);
		dataset.setVariates(variates);
		return dataset;
	}
	
	public static void convertXmlDatasetToPojo(Dataset dataset){
		
	}
	
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
	public static String generateDummySettingsXml(Dataset dataset){
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();

        setupPojoXml(pojoXml);
		
        String xml = pojoXml.getXml(dataset);
        pojoXml.saveXml(dataset,"testdataset.xml");
        //Employee employee = (Employee) pojoXml.getPojoFrormFile(fullPathNamen,Employee.class);
        return xml;
	}
	public static Dataset parseDummyXmlToDatasetPojo(){
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();
		setupPojoXml(pojoXml);
		Dataset dataset  = (Dataset) pojoXml.getPojoFromFile("testdataset.xml",Dataset.class);
		return dataset;
	}
}
