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
package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;

// TODO: Auto-generated Javadoc
/**
 * The Class MeasurementsGeneratorServiceImpl.
 */
@Service
public class MeasurementsGeneratorServiceImpl implements MeasurementsGeneratorService {

	/** The fieldbook middleware service. */
	@Resource
    private FieldbookService fieldbookMiddlewareService;
	
	//TODO: currently used for generating test data.. 
	//but in the future can be used to call a Middleware service that will 
	//generate the measurements row
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService#generateMeasurementRows(com.efficio.fieldbook.web.nursery.bean.UserSelection)
	 */
	public List<MeasurementRow> generateMeasurementRows(UserSelection userSelection) {
		Workbook workbook = userSelection.getWorkbook();
		List<MeasurementRow> rows = new ArrayList<MeasurementRow>();
		List<ImportedGermplasm> germplasms = null;
		if (userSelection.getImportedGermplasmMainInfo() != null
				&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList() != null
				&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null 
				&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().size() > 0) {
			germplasms = userSelection.getImportedGermplasmMainInfo()
			        .getImportedGermplasmList().getImportedGermplasms();
		}
				
		int count = germplasms != null ? germplasms.size() : 20;
		for (int i = 0; i < count; i++) {
			MeasurementRow row = new MeasurementRow();
			List<MeasurementData> rowCells = new ArrayList<MeasurementData>();
			row.setDataList(rowCells);
			
			if (workbook.getFactors() != null && workbook.getFactors().size() > 0) {
				for (MeasurementVariable factor : workbook.getFactors()) {
					if (germplasms != null) {
						if (factor.getName().equals("DESIG")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getDesig()));
						} else if (factor.getName().equals("GID")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getGid()));
						} else if (factor.getName().equals("SOURCE")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getSource()));
						} else if (factor.getName().equals("CROSS")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getCross()));
						} else if (factor.getName().startsWith("ENTRY")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getEntryId().toString()));
						} else if (factor.getName().startsWith("CHECK")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getEntryCode()));
						} else {
							rowCells.add(new MeasurementData(factor.getName(), String.valueOf(i)));
						}
					} else {
						rowCells.add(new MeasurementData(factor.getName(), String.valueOf(i)));
					}
				}
			}
			
			if (workbook.getVariates() != null && workbook.getVariates().size() > 0) {
				for (MeasurementVariable variate : workbook.getVariates()) {
					rowCells.add(new MeasurementData(variate.getName(), String.valueOf(i)));
				}
			}
			rows.add(row);
		}
		
		return rows;
	}
	
	
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService#generateRealMeasurementRows(com.efficio.fieldbook.web.nursery.bean.UserSelection)
	 */
	public List<MeasurementRow> generateRealMeasurementRows(UserSelection userSelection) throws MiddlewareQueryException {
		int index = 0;
    	List<MeasurementRow> measurementRows = new ArrayList();
    	int newGid = fieldbookMiddlewareService.getNextGermplasmId();
    	for(ImportedGermplasm germplasm : userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()){
    		MeasurementRow measurementRow = new MeasurementRow();
    		List<MeasurementData> dataList = new ArrayList();
    		index++;
    		
    		for(MeasurementVariable var : userSelection.getWorkbook().getMeasurementDatasetVariables()){
    			MeasurementData measurementData =null;
    			
    			
    			Integer termId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(), var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
    			
    			
    			var.setFactor(true);    
    			
    			if(termId == null){
    				//we default if null, but should not happen
    				measurementData = new MeasurementData(var.getName(), "", true, var.getDataType());
                	var.setFactor(false);
                	measurementData.setEditable(true);
    			}else{
    				
	    			
	    				if(termId.intValue() == TermId.ENTRY_NO.getId())
	    					measurementData = new MeasurementData(var.getName(), Integer.toString(index), false, var.getDataType());
	    				else if(termId.intValue() == TermId.SOURCE.getId())
	    					measurementData = new MeasurementData(var.getName(), "", false, var.getDataType());
	    				else if(termId.intValue() == TermId.CROSS.getId())	
	    					measurementData = new MeasurementData(var.getName(), germplasm.getCross(), false, var.getDataType());
	    				else if(termId.intValue() == TermId.DESIG.getId())	
	    					measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), false, var.getDataType());
	    					//measurementData = new MeasurementData(var.getName(), " sdasd a", false, var.getDataType());
	    				else if(termId.intValue() == TermId.GID.getId()){	    					
	    					//we need to check first if the germplasm is existing or not
	                    	Integer dbGid = fieldbookMiddlewareService.getGermplasmIdByName(germplasm.getDesig());
	                    	Integer gidToBeUse = null;
	                    	if(dbGid == null){
	                    		newGid--;
	                    		gidToBeUse = Integer.valueOf(newGid);
	                    	}else{
	                    		gidToBeUse = dbGid;
	                    	}
	                    	
	                    	
	                    	measurementData = new MeasurementData(var.getName(), gidToBeUse.toString(), false, var.getDataType());
	                    	
	    				}else if(termId.intValue() == TermId.ENTRY_CODE.getId())	    					
	    					measurementData = new MeasurementData(var.getName(), germplasm.getEntryCode(), false, var.getDataType());
	    				else if(termId.intValue() == TermId.PLOT_NO.getId())
	    					measurementData = new MeasurementData(var.getName(), Integer.toString(index), false, var.getDataType());
	    				else if(termId.intValue() == TermId.CHECK.getId()){
	    					
	    				/*
	    				 * NESTED_PLOT FOR TRIAL ONLY
	    				 * BLOCK
							REP
							COL
							ROW
							NESTED_PLOT
	    				 */
	    				
	    					measurementData = new MeasurementData(var.getName(), germplasm.getCheck(), false, var.getDataType());
	    				}else{
	    					//meaning non factor
	                    	measurementData = new MeasurementData(var.getName(), "", true, var.getDataType());
	                    	//measurementData.setEditable(true);
	                    	var.setFactor(false);
	                    	//measurementData.set
	    					
	    				}
	    			}
    			
    						
    			
    			dataList.add(measurementData);    			
    		}
    		measurementRow.setDataList(dataList);
    		measurementRows.add(measurementRow);
    	}
    	return measurementRows;
	}
	

}
