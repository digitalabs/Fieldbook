package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AdvancingSourceListFactory {
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Resource
	private ResourceBundleMessageSource messageSource;
	
	private static final String DEFAULT_TEST_VALUE = "T";

	public AdvancingSourceList create(Workbook workbook, AdvancingNursery advanceInfo, Study nursery, 
			Map<Integer, Method> breedingMethodMap, Map<String, Method> breedingMethodCodeMap)
	throws MiddlewareQueryException {
		
		AdvancingSourceList list = new AdvancingSourceList();
		
		List<AdvancingSource> rows = new ArrayList<AdvancingSource>();
		
    	String locationAbbreviation = advanceInfo.getHarvestLocationAbbreviation();
    	Integer methodVariateId = advanceInfo.getMethodVariateId();
    	Integer lineVariateId = advanceInfo.getLineVariateId();
    	Integer plotVariateId = advanceInfo.getPlotVariateId();
    	List<Name> names = null;
    	
    	String season = null, nurseryName = null;
        if (nursery != null) {
            nurseryName = nursery.getName();
        }
        
        List<Integer> gids = new ArrayList<Integer>();

        if (workbook != null && workbook.getObservations() != null && 
    		!workbook.getObservations().isEmpty()) {
            for (MeasurementRow row : workbook.getObservations()) {
            	
            	ImportedGermplasm germplasm = createGermplasm(row);
            	if (germplasm.getGid() != null && NumberUtils.isNumber(germplasm.getGid())) {
            		gids.add(Integer.valueOf(germplasm.getGid()));
            	}
                
                MeasurementRow trialRow = getTrialObservation(workbook, row.getLocationId());
                season = getSeason(trialRow);
                
                MeasurementData checkData = row.getMeasurementData(TermId.CHECK.getId());
                String check = null;
                if (checkData != null) {
                    check = checkData.getcValueId();
                    if (checkData != null && checkData.getMeasurementVariable() != null 
                    		&& checkData.getMeasurementVariable().getPossibleValues() != null
                    		&& !checkData.getMeasurementVariable().getPossibleValues().isEmpty()
                    		&& check != null 
                    		&& NumberUtils.isNumber(check)) {
                    	
                    	for (ValueReference valref : checkData.getMeasurementVariable().getPossibleValues()) {
                    		if (valref.getId().equals(Double.valueOf(check).intValue())) {
                    			check = valref.getName();
                    			break;
                    		}
                    	}
                    }
                }
                boolean isCheck = check != null && !"".equals(check) && !DEFAULT_TEST_VALUE.equalsIgnoreCase(check);

                Integer methodId = null;
                if (advanceInfo.getMethodChoice() == null || "0".equals(advanceInfo.getMethodChoice())) {
                    if (methodVariateId != null) {
                    	methodId = getBreedingMethodId(methodVariateId, row, breedingMethodCodeMap);
                    } 
                } else {
                	methodId = getIntegerValue(advanceInfo.getBreedingMethodId());
                }

                if (methodId != null) {
                	Method breedingMethod = breedingMethodMap.get(methodId);
	                Integer plantsSelected = null; 
	                Boolean isBulk = breedingMethod.isBulkingMethod();
	                if (isBulk != null) {
	                	if (isBulk && (advanceInfo.getAllPlotsChoice() == null || "0".equals(advanceInfo.getAllPlotsChoice()))) {
	                    	if (plotVariateId != null) {
		                        plantsSelected = getIntegerValue(row.getMeasurementDataValue(plotVariateId));
	                    	}
	                	} else {
	                    	if (lineVariateId != null && (advanceInfo.getLineChoice() == null || "0".equals(advanceInfo.getLineChoice()))) {
	                    		plantsSelected = getIntegerValue(row.getMeasurementDataValue(lineVariateId));
	                    	}
	                    }
		                rows.add(new AdvancingSource(germplasm, names, plantsSelected, breedingMethod, 
		                					isCheck, nurseryName, season, locationAbbreviation));
	                }
                }
            }
        }
        setNamesToGermplasm(rows, gids);
        list.setRows(rows);
        assignSourceGermplasms(list, breedingMethodMap);
        return list;
	}
	
	private void setNamesToGermplasm(List<AdvancingSource> rows, List<Integer> gids) throws MiddlewareQueryException {
		if (rows != null && !rows.isEmpty()) {
			Map<Integer, List<Name>> map = fieldbookMiddlewareService.getNamesByGids(gids);
			for (AdvancingSource row : rows) {
				String gid = row.getGermplasm().getGid();
				if (gid != null && NumberUtils.isNumber(gid)) {
					List<Name> names = map.get(Integer.valueOf(gid));
					if (names != null && !names.isEmpty()) {
						row.setNames(names);
					}
				}
			}
		}
	}

    private MeasurementRow getTrialObservation(Workbook workbook, long geolocationId) {
    	if (workbook.getTrialObservations() != null) {
    		for (MeasurementRow row : workbook.getTrialObservations()) {
    			if (row.getLocationId() == geolocationId) {
    				return row;
    			}
    		}
    	}
    	return null;
    }
    
    private Integer getIntegerValue(String value) {
        Integer integerValue = null;
        
        if (NumberUtils.isNumber(value)) {
            integerValue = Double.valueOf(value).intValue();
        }
        
        return integerValue;
    }
    
    private ImportedGermplasm createGermplasm(MeasurementRow row) {
        ImportedGermplasm germplasm = new ImportedGermplasm();
        germplasm.setCross(row.getMeasurementDataValue(TermId.CROSS.getId()));
        germplasm.setDesig(row.getMeasurementDataValue(TermId.DESIG.getId()));
        germplasm.setEntryCode(row.getMeasurementDataValue(TermId.ENTRY_CODE.getId()));
        germplasm.setEntryId(getIntegerValue(row.getMeasurementDataValue(TermId.ENTRY_NO.getId())));
        germplasm.setGid(row.getMeasurementDataValue(TermId.GID.getId()));
        germplasm.setSource(row.getMeasurementDataValue(TermId.SOURCE.getId()));
        return germplasm;
    }
 
    private void assignSourceGermplasms(AdvancingSourceList list, Map<Integer, Method> breedingMethodMap) throws MiddlewareQueryException {
    	List<Integer> gidList = new ArrayList<Integer>();
    	
        if (list != null && list.getRows() != null && !list.getRows().isEmpty()) {
            for (AdvancingSource source : list.getRows()) {
                if (source.getGermplasm() != null && source.getGermplasm().getGid() != null 
                        && NumberUtils.isNumber(source.getGermplasm().getGid())) {
                	
                	gidList.add(Integer.valueOf(source.getGermplasm().getGid()));
                }
            }
            List<Germplasm> germplasmList = fieldbookMiddlewareService.getGermplasms(gidList);
            Map<String, Germplasm> germplasmMap = new HashMap<String, Germplasm>();
            for(Germplasm germplasm : germplasmList){
            	germplasmMap.put(germplasm.getGid().toString(), germplasm);
            }
            for (AdvancingSource source : list.getRows()) {
                if (source.getGermplasm() != null && source.getGermplasm().getGid() != null 
                        && NumberUtils.isNumber(source.getGermplasm().getGid())) {                	
                    Germplasm germplasm = germplasmMap.get(source.getGermplasm().getGid().toString());
                    
                    checkIfGermplasmIsExisting(germplasm);
                    
                    source.getGermplasm().setGpid1(germplasm.getGpid1());
                    source.getGermplasm().setGpid2(germplasm.getGpid2());
                    source.getGermplasm().setGnpgs(germplasm.getGnpgs());
                    Method sourceMethod = breedingMethodMap.get(germplasm.getMethodId());
                    if (sourceMethod != null) {
                    	source.setSourceMethod(sourceMethod);
                    }
                    source.getGermplasm().setBreedingMethodId(germplasm.getMethodId());
                }
            }
            
        }
    }
    
    protected void checkIfGermplasmIsExisting(Germplasm germplasm) throws MiddlewareQueryException{
    	if(germplasm == null){
        	//we throw exception becuase germplasm is not existing
    		Locale locale = LocaleContextHolder.getLocale();
        	throw new MiddlewareQueryException(messageSource.getMessage("error.advancing.germplasm.not.existing", new String[] {},  locale));
        }
    }
    
    private String getSeason(MeasurementRow trialRow) {
        String season = trialRow.getMeasurementDataValue(TermId.SEASON_MONTH.getId());
        if (season == null || "".equals(season.trim())) {
	        season = trialRow.getMeasurementDataValue(TermId.SEASON_VAR_TEXT.getId());
	        if (season == null || "".equals(season.trim())) {
	            MeasurementData seasonData = trialRow.getMeasurementData(TermId.SEASON_VAR.getId());
	            if (seasonData != null) {
	            	season = seasonData.getDisplayValue();
	            }
	        }
        }
        if (season == null || "".equals(season.trim())) {
        	DateUtil.getCurrentDateAsStringValue("yyyyMM");
        }
        return season;
    }
    
    private Integer getBreedingMethodId(Integer methodVariateId, MeasurementRow row, Map<String, Method> breedingMethodCodeMap) {
    	Integer methodId = null;
    	if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE.getId())) {
    		methodId = getIntegerValue(row.getMeasurementDataValue(methodVariateId));
    	} else if (methodVariateId.equals(TermId.BREEDING_METHOD_VARIATE_TEXT.getId())) {
    		String methodName = row.getMeasurementDataValue(methodVariateId);
    		if (NumberUtils.isNumber(methodName)) {
        		methodId = Double.valueOf(methodName).intValue();
    		} else { 
    			//coming from old fb or other sources
	    		Set<String> keys = breedingMethodCodeMap.keySet();
	    		Iterator<String> iterator = keys.iterator();
	    		while (iterator.hasNext()) {
	    			String code = iterator.next();
	    			Method method = breedingMethodCodeMap.get(code);
	    			if (methodName != null && methodName.equalsIgnoreCase(method.getMname())) {
	    				methodId = method.getMid();
	    				break;
	    			}
	    		}
    		}
    	} else {
    		//on load of study, this has been converted to id and not the code.
    		methodId = getIntegerValue(row.getMeasurementDataValue(methodVariateId));
    	}
    	return methodId;
    }
}
