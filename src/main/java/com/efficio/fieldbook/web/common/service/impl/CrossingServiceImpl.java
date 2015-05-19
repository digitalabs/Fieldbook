package com.efficio.fieldbook.web.common.service.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.common.exception.FileParsingException;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.util.CrossingUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.bean.ImportedCrosses;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrossesList;
import com.efficio.fieldbook.web.common.service.CrossingService;

/**
 * Created by cyrus on 1/23/15.
 */
public class CrossingServiceImpl implements CrossingService {
	
	public static final Integer GERMPLASM_GNPGS = 2;
    public static final Integer GERMPLASM_GRPLCE = 0;
    public static final Integer GERMPLASM_LGID = 0;
    public static final Integer GERMPLASM_MGID = 0;
    public static final Integer GERMPLASM_REFID = 0;
    public static final Integer NAME_REFID = 0;
	public static final String[] USER_DEF_FIELD_CROSS_NAME = {"CROSS NAME", "CROSSING NAME"};

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;

	@Resource
	private CrossingTemplateParser crossingTemplateParser;

	@Override
	public ImportedCrossesList parseFile(MultipartFile file) throws FileParsingException{
		return crossingTemplateParser.parseFile(file);
	}
	
	@Override
	public void applyCrossSetting(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Integer userId) throws MiddlewareQueryException{
		
		CrossNameSetting crossNameSetting = crossSetting.getCrossNameSetting();
		
		applyCrossNameSettingToImportedCrosses(crossNameSetting, importedCrossesList.getImportedCrosses());
		Map<Germplasm, Name> germplasmToBeSaved = generateGermplasmNameMap(crossSetting, importedCrossesList.getImportedCrosses(), userId);
		List<Integer> savedGermplasmIds = saveGermplasm(germplasmToBeSaved);
		
		Iterator<Integer> germplasmIdIterator = savedGermplasmIds.iterator();
		for (ImportedCrosses cross : importedCrossesList.getImportedCrosses()){
			cross.setGid(germplasmIdIterator.next().toString());
		}
		
	}
	
	protected void applyCrossNameSettingToImportedCrosses(CrossNameSetting setting, List<ImportedCrosses> importedCrosses)
			throws MiddlewareQueryException {
		
			Integer nextNumberInSequence = getNextNumberInSequence(setting);
			Integer entryIdCounter = 0;
			
			for (ImportedCrosses cross : importedCrosses){
				entryIdCounter++;
				cross.setEntryId(entryIdCounter);
				cross.setEntryCode(String.valueOf(entryIdCounter));
				cross.setDesig(buildDesignationNameInSequence(nextNumberInSequence++, setting));
				cross.setCross(buildCrossName(cross, setting));
			}
	} 
	
	protected Map<Germplasm, Name> generateGermplasmNameMap(CrossSetting crossSetting, List<ImportedCrosses> importedCrosses, Integer userId) throws MiddlewareQueryException{
		
		Map<Germplasm, Name> germplasmNameMap = new LinkedHashMap<>();
		Integer crossingNameTypeId = getIDForUserDefinedFieldCrossingName();
		AdditionalDetailsSetting additionalDetailsSetting = crossSetting.getAdditionalDetailsSetting();
		
		Integer dateIntValue = 0;
        Integer harvestLocationId = 0;
        
        if(additionalDetailsSetting.getHarvestLocationId() != null){
            harvestLocationId = additionalDetailsSetting.getHarvestLocationId();
        }
        
        if(additionalDetailsSetting.getHarvestDate() != null){
			String dateString = additionalDetailsSetting.getHarvestDate();
			String replacedDateString = dateString.replace("-", "");
            dateIntValue = Integer.parseInt(replacedDateString);
        }
		
		for (ImportedCrosses cross : importedCrosses){
                        
            Germplasm germplasm = new Germplasm();
            Name name = new Name();
            
            updateConstantFields(germplasm, name, userId);
            
            germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
            germplasm.setGpid2(Integer.valueOf(cross.getMaleGid()));
            germplasm.setGdate(dateIntValue);
            germplasm.setLocationId(harvestLocationId); 

            germplasm.setMethodId(0);
            
            Method breedingMethod = germplasmDataManager.getMethodByName(cross.getRawBreedingMethod());
            if (breedingMethod.getMid()!= null && breedingMethod.getMid() != 0){
            	germplasm.setMethodId(breedingMethod.getMid());
            }

            name.setNval(cross.getDesig());
            name.setNdate(dateIntValue);
            name.setTypeId(crossingNameTypeId);
            name.setLocationId(harvestLocationId);
            
            List<Name> names = new ArrayList<>();
            names.add(name);
            cross.setNames(names);
			
			germplasmNameMap.put(germplasm, name);
			
		}	
		
		CrossingUtil.applyBreedingMethodSetting(germplasmDataManager, crossSetting, germplasmNameMap);
		CrossingUtil.applyMethodNameType(germplasmDataManager, germplasmNameMap, crossingNameTypeId);
		return germplasmNameMap;
	}

	protected void updateConstantFields(Germplasm germplasm, Name name, Integer userId){
		germplasm.setGnpgs(GERMPLASM_GNPGS);
		germplasm.setGrplce(GERMPLASM_GRPLCE);
		germplasm.setLgid(GERMPLASM_LGID);
		germplasm.setMgid(GERMPLASM_MGID);
		germplasm.setUserId(userId);
		germplasm.setReferenceId(GERMPLASM_REFID);
        
        name.setReferenceId(NAME_REFID);
        name.setUserId(userId);
	}
	
	protected List<Integer> saveGermplasm(Map<Germplasm, Name> germplasmNameMap) throws MiddlewareQueryException{
		return germplasmDataManager.addGermplasm(germplasmNameMap);
	}
	
	protected Integer getNextNumberInSequence(CrossNameSetting setting) throws MiddlewareQueryException{

        String lastPrefixUsed = buildPrefixString(setting);
        int nextNumberInSequence = 1;
        
        Integer startNumber = setting.getStartNumber();
        if (startNumber != null && startNumber > 0){
        	nextNumberInSequence = startNumber;
        } else {
        	String nextSequenceNumberString = this.germplasmDataManager.getNextSequenceNumberForCrossName(lastPrefixUsed.toUpperCase().trim());
        	nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
        }
        
        return nextNumberInSequence;
        
	}
	
	protected String buildDesignationNameInSequence(Integer number, CrossNameSetting setting) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildPrefixString(setting));
        sb.append(getNumberWithLeadingZeroesAsString(number, setting));
        if (!StringUtils.isEmpty(setting.getSuffix())){
            sb.append(buildSuffixString(setting));
        }
        return sb.toString();
    }
	
	protected String buildCrossName(ImportedCrosses importedCrosses, CrossNameSetting setting) {
		return importedCrosses.getFemaleDesig() + setting.getSeparator() + importedCrosses.getMaleDesig();
	}
	
	
	protected String buildPrefixString(CrossNameSetting setting){
		String prefix = setting.getPrefix().trim();
        if(setting.isAddSpaceBetweenPrefixAndCode()){
            return prefix + " ";
        }
        return prefix;
    }
	
	protected String buildSuffixString(CrossNameSetting setting){
		String suffix = setting.getSuffix().trim();
        if(setting.isAddSpaceBetweenSuffixAndCode()){
            return " " + suffix ;
        }
        return suffix;
	}
    
	protected String getNumberWithLeadingZeroesAsString(Integer number,CrossNameSetting setting){
        StringBuilder sb = new StringBuilder();
        String numberString = number.toString();
        Integer numOfDigits = setting.getNumOfDigits();
        
        if (numOfDigits != null && numOfDigits > 0){
        	int numOfZerosNeeded = numOfDigits - numberString.length();
        	if(numOfZerosNeeded > 0){
        		for (int i = 0; i < numOfZerosNeeded; i++){
        			sb.append("0");
        		}
        	}
        	
        }
        sb.append(number);
        return sb.toString();
    }
    
	public Integer getIDForUserDefinedFieldCrossingName() throws MiddlewareQueryException  {
        
        List<UserDefinedField> nameTypes = germplasmListManager.getGermplasmNameTypes();
        for (UserDefinedField type : nameTypes){
            for (String crossNameValue : USER_DEF_FIELD_CROSS_NAME){
                if (crossNameValue.equalsIgnoreCase(type.getFcode()) ||
                        crossNameValue.equalsIgnoreCase(type.getFname())){
                    return type.getFldno();
                }
            }
        }
        
        return null;
    }
	
	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
		
	}

}
