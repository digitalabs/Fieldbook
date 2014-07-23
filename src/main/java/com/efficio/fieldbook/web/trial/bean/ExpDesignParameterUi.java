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
	private String rowsPerReplications;
	private String colsPerReplications;
	private Map treatmentFactors;
	private Map treatmentFactorsData;
	private String totalGermplasmListCount;
	private String nclatin;
	private String nrlatin;
	private String nblatin;
	private String replatinGroups;
/*
	1 - single col
	2 - single row
	3 - adjacent
 */
	private Integer replicationsArrangement;	   
		
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
	public String getNclatin() {
		return nclatin;
	}
	public void setNclatin(String nclatin) {
		this.nclatin = nclatin;
	}
	public String getNrlatin() {
		return nrlatin;
	}
	public void setNrlatin(String nrlatin) {
		this.nrlatin = nrlatin;
	}
	public String getNblatin() {
		return nblatin;
	}
	public void setNblatin(String nblatin) {
		this.nblatin = nblatin;
	}
	public String getReplatinGroups() {
		return replatinGroups;
	}
	public void setReplatinGroups(String replatinGroups) {
		this.replatinGroups = replatinGroups;
	}
	public Integer getReplicationsArrangement() {
		return replicationsArrangement;
	}
	public void setReplicationsArrangement(Integer replicationsArrangement) {
		this.replicationsArrangement = replicationsArrangement;
	}
	
	
	
}
