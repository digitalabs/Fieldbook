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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * The Class MeasurementsGeneratorServiceImpl.
 */
@Service
public class MeasurementsGeneratorServiceImpl implements MeasurementsGeneratorService {
    
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementsGeneratorServiceImpl.class);
    
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
		long start = System.currentTimeMillis();
    	List<MeasurementRow> measurementRows = new ArrayList<MeasurementRow>();
    	//int newGid = fieldbookMiddlewareService.getNextGermplasmId();
    	Map<String, Integer> standardVariableMap = new HashMap<String, Integer>();
    	int entryNo, plotNo;
    	
    	List<ExperimentalDesignInfo> designInfos = getExperimentalDesignInfo(userSelection.getTrialEnvironmentValues());
    	for (ExperimentalDesignInfo designInfo : designInfos) {
    		
    		int trialNo = designInfo.getTrialNumber();
    		plotNo = 1;
    		
    		for (int repNo = 1; repNo <= designInfo.getNumberOfReps(); repNo++) {
    			
    			entryNo = 1;
    			for (int blockNo = 1; blockNo <= designInfo.getBlocksPerRep(); blockNo++) {
    	
			    	for(ImportedGermplasm germplasm : userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()){

			    		MeasurementRow measurementRow = createMeasurementRow(userSelection, trialNo, repNo, blockNo, 
			    				germplasm, entryNo++, plotNo++, standardVariableMap);
			    		measurementRows.add(measurementRow);
			    	}
    			}
    		}
    	}
    	LOG.info("generateRealMeasurementRows Time duration: "+ ((System.currentTimeMillis() - start)/1000));
    	return measurementRows;
	}
	
	private MeasurementRow createMeasurementRow(UserSelection userSelection, int trialNo, int repNo, int blockNo, 
			ImportedGermplasm germplasm, int entryNo, int plotNo, Map<String, Integer> standardVariableMap)
	throws MiddlewareQueryException {
		
		MeasurementRow measurementRow = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		
    	if (userSelection.isTrial()) {
    		MeasurementVariable trialInstanceVar = 
    				WorkbookUtil.getMeasurementVariable(userSelection.getWorkbook().getTrialVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId());
			MeasurementData measurementData = new MeasurementData(trialInstanceVar.getName(), Integer.toString(trialNo), false, 
					trialInstanceVar.getDataType(), trialInstanceVar);
			dataList.add(measurementData);
    	}
    	
		//for(MeasurementVariable var : userSelection.getWorkbook().getMeasurementDatasetVariables()){
		//iterate the non trial factors
		for(MeasurementVariable var : userSelection.getWorkbook().getNonTrialFactors()){    			    			
			MeasurementData measurementData =null;
			
			
			Integer termId = null;//fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(), var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
			String key = var.getProperty() + ":" + var.getScale() + ":" + var.getMethod() + ":" + PhenotypicType.getPhenotypicTypeForLabel(var.getLabel());
			if(standardVariableMap.get(key) == null){
				termId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(), var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
				standardVariableMap.put(key, termId);
			}else{
				termId = (Integer)standardVariableMap.get(key);
						
			}
			
			var.setFactor(true);    
			
			if(termId == null){
				//we default if null, but should not happen
				measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
            	var.setFactor(false);
            	measurementData.setEditable(true);
			}else{
				
    			
    				if(termId.intValue() == TermId.ENTRY_NO.getId())
    					measurementData = new MeasurementData(var.getName(), Integer.toString(entryNo), false, var.getDataType(), var);
    				else if(termId.intValue() == TermId.SOURCE.getId())
    					measurementData = new MeasurementData(var.getName(), "", false, var.getDataType(), var);
    				else if(termId.intValue() == TermId.CROSS.getId())	
    					measurementData = new MeasurementData(var.getName(), germplasm.getCross(), false, var.getDataType(), var);
    				else if(termId.intValue() == TermId.DESIG.getId())	
    					measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), false, var.getDataType(), var);
    					//measurementData = new MeasurementData(var.getName(), " sdasd a", false, var.getDataType());
    				else if(termId.intValue() == TermId.GID.getId()){	    					
    					//we need to check first if the germplasm is existing or not
    					/*
                    	Integer dbGid = fieldbookMiddlewareService.getGermplasmIdByName(germplasm.getDesig());
                    	Integer gidToBeUse = null;
                    	if(dbGid == null){
                    		
                    		gidToBeUse = Integer.valueOf(newGid);
                    		newGid--;
                    	}else{
                    		gidToBeUse = dbGid;
                    	}
                    	
                    	
                    	measurementData = new MeasurementData(var.getName(), gidToBeUse.toString(), false, var.getDataType());
                    	*/
    					measurementData = new MeasurementData(var.getName(), germplasm.getGid(), false, var.getDataType(), var);
    				}else if(termId.intValue() == TermId.ENTRY_CODE.getId())	    					
    					measurementData = new MeasurementData(var.getName(), germplasm.getEntryCode(), false, var.getDataType(), var);
    				else if(termId.intValue() == TermId.PLOT_NO.getId())
    					measurementData = new MeasurementData(var.getName(), Integer.toString(plotNo), false, var.getDataType(), var);
    				else if(termId.intValue() == TermId.CHECK.getId()){
    					
    				/*
    				 * NESTED_PLOT FOR TRIAL ONLY
    				 * BLOCK
						REP
						COL
						ROW
						NESTED_PLOT
    				 */
    				
//	    					measurementData = new MeasurementData(var.getName(), germplasm.getCheck(), false, var.getDataType());
    					measurementData = new MeasurementData(var.getName(), germplasm.getCheckName(), 
				    							false, var.getDataType(), germplasm.getCheckId(), var);
    					
    				} else if (termId.intValue() == TermId.REP_NO.getId()) {
    					measurementData = new MeasurementData(var.getName(), Integer.toString(repNo), false, var.getDataType(), var);
    					
    				} else if (termId.intValue() == TermId.BLOCK_NO.getId()) {
    					measurementData = new MeasurementData(var.getName(), Integer.toString(blockNo), false, var.getDataType(), var);
    					
    				}else{
    					//meaning non factor
                    	measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
                    	//measurementData.setEditable(true);
                    	var.setFactor(false);
                    	//measurementData.set
    					
    				}
    			}
			
			dataList.add(measurementData); 
			//measurementRow.addFactorDataList(measurementData);//for improvement
		}
		//iterate the variates
		for(MeasurementVariable var : userSelection.getWorkbook().getVariates()){    			    			
			MeasurementData measurementData =null;
			    			    			
        	measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
        	//measurementData.setEditable(true);
        	var.setFactor(false);	                			    						
			
			dataList.add(measurementData);
			//measurementRow.addVariateDataList(measurementData);//for improvement
		}
		measurementRow.setDataList(dataList);
		return measurementRow;
	}
	
	private List<ExperimentalDesignInfo> getExperimentalDesignInfo(List<List<ValueReference>> trialInfo) {
		List<ExperimentalDesignInfo> result = new ArrayList<ExperimentalDesignInfo>();
		
		if (trialInfo != null && !trialInfo.isEmpty()) {
			for (List<ValueReference> list : trialInfo) {
				ExperimentalDesignInfo info = new ExperimentalDesignInfo();
				for (ValueReference ref : list) {
					if (ref.getName() != null && NumberUtils.isNumber(ref.getName())) {
						Integer value = Integer.valueOf(Double.valueOf(ref.getName()).intValue());
						if (ref.getId().equals(TermId.TRIAL_INSTANCE_FACTOR.getId())) {
							info.setTrialNumber(value);
						}
						else if (ref.getId().equals(TermId.EXPERIMENT_DESIGN_FACTOR.getId())) {
							info.setDesign(value);
						}
						else if (ref.getId().equals(TermId.NUMBER_OF_REPLICATES.getId()))  {
							info.setNumberOfReps(value);
						}
						else if (ref.getId().equals(TermId.BLOCK_SIZE.getId())) {
							info.setBlockSize(value);
						}
						else if (ref.getId().equals(TermId.BLOCKS_PER_REPLICATE.getId())) {
							info.setBlocksPerRep(value);
						}
					}
				}
				result.add(info);
			}
		}
		
		if (result.isEmpty()) {
			result.add(new ExperimentalDesignInfo(1, null, 1, null, 1));
		}
		
		return result;
	}
	
	class ExperimentalDesignInfo {
		private Integer trialNumber;
		private Integer design;
		private Integer numberOfReps;
		private Integer blockSize;
		private Integer blocksPerRep;
		
		public ExperimentalDesignInfo() {
			
		}
		public ExperimentalDesignInfo(Integer trialNumber, Integer design, Integer numberOfReps, Integer blockSize, Integer blocksPerRep) {
			this.trialNumber = trialNumber;
			this.design = design;
			this.numberOfReps = numberOfReps;
			this.blockSize = blockSize;
			this.blocksPerRep = blocksPerRep;
		}
		
		public Integer getTrialNumber() {
			return trialNumber;
		}
		public void setTrialNumber(Integer trialNumber) {
			this.trialNumber = trialNumber;
		}
		public Integer getDesign() {
			return design;
		}
		public void setDesign(Integer design) {
			this.design = design;
		}
		public Integer getNumberOfReps() {
			return numberOfReps;
		}
		public void setNumberOfReps(Integer numberOfReps) {
			this.numberOfReps = numberOfReps;
		}
		public Integer getBlockSize() {
			return blockSize;
		}
		public void setBlockSize(Integer blockSize) {
			this.blockSize = blockSize;
		}
		public Integer getBlocksPerRep() {
			return blocksPerRep;
		}
		public void setBlocksPerRep(Integer blocksPerRep) {
			this.blocksPerRep = blocksPerRep;
		}
		@Override
		public String toString() {
			return "ExperimentalDesignInfo [trialNumber=" + trialNumber
					+ ", design=" + design + ", numberOfReps=" + numberOfReps
					+ ", blockSize=" + blockSize + ", blocksPerRep="
					+ blocksPerRep + "]";
		}
		
	}

}
