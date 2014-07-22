package com.efficio.fieldbook.web.trial.bean;

import java.io.Serializable;
import java.util.Map;

public class ExpDesignParameterUi implements Serializable {
	private String noOfEnvironments;
	private Integer designType;
	private String replicationsCount;
	private Boolean isResolvable;
	private Boolean useLatenized;
	private String blockSize;
	private String contiguousBlocksToLatenize;
	private String replicationsPerCol;
	private String rowsPerReplications;
	private String colsPerReplications;
	private String contiguousRowsToLatenize;
	private String contiguousColToLatenize;
	private Map treatmentFactors;
	private Map treatmentFactorsData;
	private String totalGermplasmListCount;
	
	public String getNoOfEnvironments() {
		return noOfEnvironments;
	}
	public void setNoOfEnvironments(String noOfEnvironments) {
		this.noOfEnvironments = noOfEnvironments;
	}
	
	public Integer getDesignType() {
		return designType;
	}
	public void setDesignType(Integer designType) {
		this.designType = designType;
	}
	public String getReplicationsCount() {
		return replicationsCount;
	}
	public void setReplicationsCount(String replicationsCount) {
		this.replicationsCount = replicationsCount;
	}
	public Boolean getIsResolvable() {
		return isResolvable;
	}
	public void setIsResolvable(Boolean isResolvable) {
		this.isResolvable = isResolvable;
	}
	public Boolean getUseLatenized() {
		return useLatenized;
	}
	public void setUseLatenized(Boolean useLatenized) {
		this.useLatenized = useLatenized;
	}
	public String getBlockSize() {
		return blockSize;
	}
	public void setBlockSize(String blockSize) {
		this.blockSize = blockSize;
	}
	public String getContiguousBlocksToLatenize() {
		return contiguousBlocksToLatenize;
	}
	public void setContiguousBlocksToLatenize(String contiguousBlocksToLatenize) {
		this.contiguousBlocksToLatenize = contiguousBlocksToLatenize;
	}
	public String getReplicationsPerCol() {
		return replicationsPerCol;
	}
	public void setReplicationsPerCol(String replicationsPerCol) {
		this.replicationsPerCol = replicationsPerCol;
	}
	public String getRowsPerReplications() {
		return rowsPerReplications;
	}
	public void setRowsPerReplications(String rowsPerReplications) {
		this.rowsPerReplications = rowsPerReplications;
	}
	public String getColsPerReplications() {
		return colsPerReplications;
	}
	public void setColsPerReplications(String colsPerReplications) {
		this.colsPerReplications = colsPerReplications;
	}
	public String getContiguousRowsToLatenize() {
		return contiguousRowsToLatenize;
	}
	public void setContiguousRowsToLatenize(String contiguousRowsToLatenize) {
		this.contiguousRowsToLatenize = contiguousRowsToLatenize;
	}
	public String getContiguousColToLatenize() {
		return contiguousColToLatenize;
	}
	public void setContiguousColToLatenize(String contiguousColToLatenize) {
		this.contiguousColToLatenize = contiguousColToLatenize;
	}
	public Map getTreatmentFactors() {
		return treatmentFactors;
	}
	public void setTreatmentFactors(Map treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
	}
	public Map getTreatmentFactorsData() {
		return treatmentFactorsData;
	}
	public void setTreatmentFactorsData(Map treatmentFactorsData) {
		this.treatmentFactorsData = treatmentFactorsData;
	}
	public String getTotalGermplasmListCount() {
		return totalGermplasmListCount;
	}
	public void setTotalGermplasmListCount(String totalGermplasmListCount) {
		this.totalGermplasmListCount = totalGermplasmListCount;
	}
	
	
}
