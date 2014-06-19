package com.efficio.fieldbook.web.naming.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

@Service
public class AdvancingSourceListFactory {
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	public AdvancingSourceList create(Workbook workbook, AdvancingNursery advanceInfo, Study nursery, Map<Integer, Method> breedingMethodMap)
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

        if (workbook != null) {
            if (workbook.getObservations() != null && !workbook.getObservations().isEmpty()) {
                for (MeasurementRow row : workbook.getObservations()) {
                	
                	ImportedGermplasm germplasm = createGermplasm(row);
                	if (germplasm.getGid() != null && NumberUtils.isNumber(germplasm.getGid())) {
                		gids.add(Integer.valueOf(germplasm.getGid()));
                	}
                    
                    MeasurementRow trialRow = getTrialObservation(workbook, row.getLocationId());
                    season = trialRow.getMeasurementDataValue(TermId.SEASON_VAR_TEXT.getId());
                    if (season == null || "".equals(season.trim())) {
                        MeasurementData seasonData = trialRow.getMeasurementData(TermId.SEASON_VAR.getId());
                        if (seasonData != null) {
                        	season = seasonData.getDisplayValue();
                        }
                    }
                    if (season == null || "".equals(season.trim())) {
                    	DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    	season = dateFormat.format(new Date());
                    }
                    
                    String check = row.getMeasurementDataValue(TermId.CHECK.getId());
                    boolean isCheck = check != null && !"".equals(check);

                    Integer methodId = null;
                    if (advanceInfo.getMethodChoice() == null || "0".equals(advanceInfo.getMethodChoice())) {
                        if (methodVariateId != null) {
                        	methodId = getIntegerValue(row.getMeasurementDataValue(methodVariateId));
                        } 
                    }
                    else {
                    	methodId = getIntegerValue(advanceInfo.getBreedingMethodId());
                    }

                    if (methodId != null) {
                    	Method breedingMethod = breedingMethodMap.get(methodId);
		                Integer plantsSelected = null; 
		                boolean isBulk = breedingMethod.isBulked();
	                	if (isBulk && (advanceInfo.getAllPlotsChoice() == null || "0".equals(advanceInfo.getAllPlotsChoice()))) {
	                    	if (plotVariateId != null) {
		                        plantsSelected = getIntegerValue(row.getMeasurementDataValue(plotVariateId));
	                    	}
	                	}
	                    else {
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
    
    private Boolean isBulk(Integer methodId, Map<Integer, Method> methodMap) throws MiddlewareQueryException {
    	if (methodId != null) {
    		Method method = methodMap.get(methodId);
    		return method != null && method.getGeneq() != null && method.getGeneq().equals(1);
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
}
