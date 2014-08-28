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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
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

	
	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService#generateRealMeasurementRows(com.efficio.fieldbook.web.nursery.bean.UserSelection)
	 */
	@Override
	public List<MeasurementRow> generateRealMeasurementRows(UserSelection userSelection) throws MiddlewareQueryException {
		long start = System.currentTimeMillis();
    	List<MeasurementRow> measurementRows = new ArrayList<MeasurementRow>();
    	//int newGid = fieldbookMiddlewareService.getNextGermplasmId();
    	Map<String, Integer> standardVariableMap = new HashMap<String, Integer>();
    	int entryNo, plotNo;
    	
    	List<ExperimentalDesignInfo> designInfos = getExperimentalDesignInfo(userSelection.getTrialEnvironmentValues());
    	
    	MeasurementData[][] treatmentFactorPermutations = generateTreatmentFactorPermutations(userSelection.getWorkbook().getTreatmentFactors(), standardVariableMap);
    	
    	for (ExperimentalDesignInfo designInfo : designInfos) {
    		
    		int trialNo = designInfo.getTrialNumber();
    		plotNo = 1;
    		
    		for (int repNo = 1; repNo <= designInfo.getNumberOfReps(); repNo++) {
    			
    			entryNo = 1;
    			for (int blockNo = 1; blockNo <= designInfo.getBlocksPerRep(); blockNo++) {
    				
			    	for(ImportedGermplasm germplasm : userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()){
			    		
			    		List<MeasurementRow> measurementRow = createMeasurementRows(userSelection, trialNo, repNo, blockNo, 
			    				germplasm, entryNo++, plotNo++, standardVariableMap, treatmentFactorPermutations);
			    		measurementRows.addAll(measurementRow);
			    	}
    			}
    		}
    	}
    	LOG.info("generateRealMeasurementRows Time duration: "+ ((System.currentTimeMillis() - start)/1000));
    	return measurementRows;
	}
	
	private List<MeasurementRow> createMeasurementRows(UserSelection userSelection, int trialNo, int repNo, int blockNo, 
			ImportedGermplasm germplasm, int entryNo, int plotNo, Map<String, Integer> standardVariableMap,
			MeasurementData[][] treatmentFactorPermutations)
	throws MiddlewareQueryException {
		
		List<MeasurementRow> measurementRows = new ArrayList<MeasurementRow>();
		
		int count = 1;
		if (treatmentFactorPermutations != null && treatmentFactorPermutations.length > 0) {
			count = treatmentFactorPermutations.length;
		}
		
		for (int i = 0; i < count; i++) {
			
			MeasurementRow measurementRow = new MeasurementRow();
			List<MeasurementData> dataList = new ArrayList<MeasurementData>();
			
	    	if (userSelection.isTrial()) {
	    		createTrialInstanceDataList(dataList, userSelection, trialNo);
	    	}
	    	
	    	createFactorDataList(dataList, userSelection, repNo, blockNo, germplasm, entryNo, plotNo, standardVariableMap);
	    	
	    	if (treatmentFactorPermutations != null && treatmentFactorPermutations.length > 0) {
	    		for (MeasurementData treatmentFactor : treatmentFactorPermutations[i]) {
	    			dataList.add(treatmentFactor);
	    		}
	    	}
	    	
	    	createVariateDataList(dataList, userSelection);
    	
	    	measurementRow.setDataList(dataList);
	    	measurementRows.add(measurementRow);
		}
		
		return measurementRows;
	}
	
	private void createTrialInstanceDataList(List<MeasurementData> dataList, UserSelection userSelection, int trialNo) {
		MeasurementVariable trialInstanceVar = 
				WorkbookUtil.getMeasurementVariable(userSelection.getWorkbook().getTrialVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId());
		MeasurementData measurementData = new MeasurementData(trialInstanceVar.getName(), Integer.toString(trialNo), false, 
				trialInstanceVar.getDataType(), trialInstanceVar);
		dataList.add(measurementData);
	}
	
	private void createFactorDataList(List<MeasurementData> dataList, UserSelection userSelection, int repNo, int blockNo, 
			ImportedGermplasm germplasm, int entryNo, int plotNo, Map<String, Integer> standardVariableMap)
	throws MiddlewareQueryException {
		
		for(MeasurementVariable var : userSelection.getWorkbook().getNonTrialFactors()){
			
			//do not include treatment factors
			if (var.getTreatmentLabel() == null || "".equals(var.getTreatmentLabel())) {
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
					else if(termId.intValue() == TermId.SOURCE.getId() || termId.intValue() == TermId.GERMLASM_SOURCE.getId())
						measurementData = new MeasurementData(var.getName(), germplasm.getSource() != null ? germplasm.getSource() : "", false, var.getDataType(), var);
					else if(termId.intValue() == TermId.CROSS.getId())	
						measurementData = new MeasurementData(var.getName(), germplasm.getCross(), false, var.getDataType(), var);
					else if(termId.intValue() == TermId.DESIG.getId())	
						measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), false, var.getDataType(), var);
					else if(termId.intValue() == TermId.GID.getId()){	    					
						measurementData = new MeasurementData(var.getName(), germplasm.getGid(), false, var.getDataType(), var);
					}else if(termId.intValue() == TermId.ENTRY_CODE.getId())	    					
						measurementData = new MeasurementData(var.getName(), germplasm.getEntryCode(), false, var.getDataType(), var);
					else if(termId.intValue() == TermId.PLOT_NO.getId())
						measurementData = new MeasurementData(var.getName(), Integer.toString(plotNo), false, var.getDataType(), var);
					else if(termId.intValue() == TermId.CHECK.getId()){
						measurementData = new MeasurementData(var.getName(), germplasm.getCheckName(), 
				    							false, var.getDataType(), germplasm.getCheckId(), var);
						
					} else if (termId.intValue() == TermId.REP_NO.getId()) {
						measurementData = new MeasurementData(var.getName(), Integer.toString(repNo), false, var.getDataType(), var);
						
					} else if (termId.intValue() == TermId.BLOCK_NO.getId()) {
						measurementData = new MeasurementData(var.getName(), Integer.toString(blockNo), false, var.getDataType(), var);
						
					}else{
						//meaning non factor
	                	measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
	                	var.setFactor(false);
					}
				}
				
				dataList.add(measurementData); 
				//measurementRow.addFactorDataList(measurementData);//for improvement
			}
		}
	}
	
	private void createVariateDataList(List<MeasurementData> dataList, UserSelection userSelection) {
		for(MeasurementVariable var : userSelection.getWorkbook().getVariates()){    			    			
			MeasurementData measurementData =null;
			    			    			
        	measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
        	var.setFactor(false);	                			    						
			
			dataList.add(measurementData);
		}
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
				if (info.getNumberOfReps() == null || info.getNumberOfReps() == 0) {
					info.setNumberOfReps(1);
				}
				if (info.getTrialNumber() == null || info.getTrialNumber() == 0) {
					info.setTrialNumber(1);
				}
				if (info.getBlocksPerRep() == null || info.getBlocksPerRep() == 0) {
					info.setBlocksPerRep(1);
				}
				result.add(info); 
			}
		}
		
		if (result.isEmpty()) {
			result.add(new ExperimentalDesignInfo(1, null, 1, null, 1));
		}
		
		return result;
	}
	
	private MeasurementData[][] generateTreatmentFactorPermutations(List<TreatmentVariable> treatmentVariables,
			Map<String, Integer> standardVariableMap)
			throws MiddlewareQueryException {
		
		MeasurementData[][] output = null;
		if (treatmentVariables != null && !treatmentVariables.isEmpty()) {
			List<List<TreatmentVariable>> lists = rearrangeTreatmentVariables(treatmentVariables); 
			int totalPermutations = getTotalPermutations(lists);
			output = new MeasurementData[totalPermutations][lists.size()*2];
			
			int currentPermutation = 1;
			int listIndex = 0;
			for (List<TreatmentVariable> list : lists) {
				int size = list.size();
				currentPermutation *= size;
				int reps = totalPermutations / currentPermutation;
				
				for (int i = 0; i < currentPermutation; i++) {
					for (int j = 0; j < reps; j++) {
						TreatmentVariable factor = list.get(i % size);
						MeasurementData levelData = createMeasurementData(factor.getLevelVariable(), standardVariableMap);
						MeasurementData valueData = createMeasurementData(factor.getValueVariable(), standardVariableMap);
						output[reps * i + j][listIndex*2] = levelData;
						output[reps * i + j][listIndex*2+1] = valueData;
					}
				}
				listIndex++;
			}
		}
		
		return output;
	}
	
	private List<List<TreatmentVariable>> rearrangeTreatmentVariables(List<TreatmentVariable> treatmentVariables) {
		List<List<TreatmentVariable>> groupedFactors = new ArrayList<List<TreatmentVariable>>();
		Integer levelFactorId = null;
		Map<Integer, List<TreatmentVariable>> map = new LinkedHashMap<Integer, List<TreatmentVariable>>();
		for (TreatmentVariable treatmentFactor : treatmentVariables) {
			levelFactorId = treatmentFactor.getLevelVariable().getTermId();
			List<TreatmentVariable> treatments = map.get(levelFactorId);
			if (treatments == null) {
				treatments = new ArrayList<TreatmentVariable>();
				map.put(levelFactorId, treatments);
			}
			treatments.add(treatmentFactor);
		}
		Set<Integer> keys = map.keySet();
		for (Iterator<Integer> iterator = keys.iterator(); iterator.hasNext(); ) {
			groupedFactors.add(map.get(iterator.next()));
		}
		return groupedFactors;
	}

	private int getTotalPermutations(List<List<TreatmentVariable>> lists) {
		int totalPermutations = 1;
		for (List<TreatmentVariable> list : lists) {
			totalPermutations *= list.size();
		}
		return totalPermutations;
	}
	
	private MeasurementData createMeasurementData(MeasurementVariable variable, Map<String, Integer> standardVariableMap) 
	throws MiddlewareQueryException {
		
		Integer termId = variable.getTermId();
		if (termId == 0) {
			termId = getTermId(variable, standardVariableMap);
		}
		
		return new MeasurementData(variable.getName(), variable.getValue(), false, variable.getDataType(), variable);
	}
	
	private Integer getTermId(MeasurementVariable var, Map<String, Integer> standardVariableMap) throws MiddlewareQueryException {
		
		Integer termId = null;
		String key = var.getProperty() + ":" + var.getScale() + ":" + var.getMethod() + ":" + PhenotypicType.getPhenotypicTypeForLabel(var.getLabel());
		if(standardVariableMap.get(key) == null){
			termId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(), var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
			standardVariableMap.put(key, termId);
		}else{
			termId = (Integer)standardVariableMap.get(key);
					
		}
		
		return termId;    
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
