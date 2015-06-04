
package com.efficio.fieldbook.web.trial.bean;

import java.io.Serializable;
import java.util.Map;

public class ExpDesignParameterUi implements Serializable, TabInfoBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3081300491744812262L;
	private String noOfEnvironments;
	private String noOfEnvironmentsToAdd;
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
	private boolean hasMeasurementData;
	/*
	 * 1 - single col 2 - single row 3 - adjacent
	 */
	private Integer replicationsArrangement;

	public String getNoOfEnvironments() {
		return this.noOfEnvironments;
	}

	public void setNoOfEnvironments(String noOfEnvironments) {
		this.noOfEnvironments = noOfEnvironments;
	}

	public Integer getDesignType() {
		return this.designType;
	}

	public void setDesignType(Integer designType) {
		this.designType = designType;
	}

	public String getReplicationsCount() {
		return this.replicationsCount;
	}

	public void setReplicationsCount(String replicationsCount) {
		this.replicationsCount = replicationsCount;
	}

	public Boolean getIsResolvable() {
		return this.isResolvable;
	}

	public void setIsResolvable(Boolean isResolvable) {
		this.isResolvable = isResolvable;
	}

	public Boolean getUseLatenized() {
		return this.useLatenized;
	}

	public void setUseLatenized(Boolean useLatenized) {
		this.useLatenized = useLatenized;
	}

	public String getBlockSize() {
		return this.blockSize;
	}

	public void setBlockSize(String blockSize) {
		this.blockSize = blockSize;
	}

	public String getRowsPerReplications() {
		return this.rowsPerReplications;
	}

	public void setRowsPerReplications(String rowsPerReplications) {
		this.rowsPerReplications = rowsPerReplications;
	}

	public String getColsPerReplications() {
		return this.colsPerReplications;
	}

	public void setColsPerReplications(String colsPerReplications) {
		this.colsPerReplications = colsPerReplications;
	}

	public Map getTreatmentFactors() {
		return this.treatmentFactors;
	}

	public void setTreatmentFactors(Map treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
	}

	public Map getTreatmentFactorsData() {
		return this.treatmentFactorsData;
	}

	public void setTreatmentFactorsData(Map treatmentFactorsData) {
		this.treatmentFactorsData = treatmentFactorsData;
	}

	public String getTotalGermplasmListCount() {
		return this.totalGermplasmListCount;
	}

	public void setTotalGermplasmListCount(String totalGermplasmListCount) {
		this.totalGermplasmListCount = totalGermplasmListCount;
	}

	public String getNclatin() {
		return this.nclatin;
	}

	public void setNclatin(String nclatin) {
		this.nclatin = nclatin;
	}

	public String getNrlatin() {
		return this.nrlatin;
	}

	public void setNrlatin(String nrlatin) {
		this.nrlatin = nrlatin;
	}

	public String getNblatin() {
		return this.nblatin;
	}

	public void setNblatin(String nblatin) {
		this.nblatin = nblatin;
	}

	public String getReplatinGroups() {
		return this.replatinGroups;
	}

	public void setReplatinGroups(String replatinGroups) {
		this.replatinGroups = replatinGroups;
	}

	public Integer getReplicationsArrangement() {
		return this.replicationsArrangement;
	}

	public void setReplicationsArrangement(Integer replicationsArrangement) {
		this.replicationsArrangement = replicationsArrangement;
	}

	public String getNoOfEnvironmentsToAdd() {
		return this.noOfEnvironmentsToAdd;
	}

	public void setNoOfEnvironmentsToAdd(String noOfEnvironmentsToAdd) {
		this.noOfEnvironmentsToAdd = noOfEnvironmentsToAdd;
	}

	public boolean isHasMeasurementData() {
		return this.hasMeasurementData;
	}

	public void setHasMeasurementData(boolean hasMeasurementData) {
		this.hasMeasurementData = hasMeasurementData;
	}

}
