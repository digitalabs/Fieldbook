package com.efficio.fieldbook.web.common.bean;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class CrossImportSettings {
	private String crossPrefix;
	private Integer breedingMethodID;
	private String crossSuffix;
	private Integer sequenceNumberDigits;
	private Boolean hasSuffixSpace;
	private Boolean hasPrefixSpace;
	private Integer startingSequenceNumber;
	private String parentageDesignationSeparator;

	public CrossImportSettings() {
	}

	public CrossImportSettings(String crossPrefix, Integer breedingMethodID,
			String crossSuffix, Integer sequenceNumberDigits, Boolean hasSuffixSpace,
			Boolean hasPrefixSpace, Integer startingSequenceNumber,
			String parentageDesignationSeparator) {
		this.crossPrefix = crossPrefix;
		this.breedingMethodID = breedingMethodID;
		this.crossSuffix = crossSuffix;
		this.sequenceNumberDigits = sequenceNumberDigits;
		this.hasSuffixSpace = hasSuffixSpace;
		this.hasPrefixSpace = hasPrefixSpace;
		this.startingSequenceNumber = startingSequenceNumber;
		this.parentageDesignationSeparator = parentageDesignationSeparator;
	}

	public String getCrossPrefix() {
		return crossPrefix;
	}

	public void setCrossPrefix(String crossPrefix) {
		this.crossPrefix = crossPrefix;
	}

	public Integer getBreedingMethodID() {
		return breedingMethodID;
	}

	public void setBreedingMethodID(Integer breedingMethodID) {
		this.breedingMethodID = breedingMethodID;
	}

	public String getCrossSuffix() {
		return crossSuffix;
	}

	public void setCrossSuffix(String crossSuffix) {
		this.crossSuffix = crossSuffix;
	}

	public Integer getSequenceNumberDigits() {
		return sequenceNumberDigits;
	}

	public void setSequenceNumberDigits(Integer sequenceNumberDigits) {
		this.sequenceNumberDigits = sequenceNumberDigits;
	}

	public Boolean getHasSuffixSpace() {
		return hasSuffixSpace;
	}

	public void setHasSuffixSpace(Boolean hasSuffixSpace) {
		this.hasSuffixSpace = hasSuffixSpace;
	}

	public Boolean getHasPrefixSpace() {
		return hasPrefixSpace;
	}

	public void setHasPrefixSpace(Boolean hasPrefixSpace) {
		this.hasPrefixSpace = hasPrefixSpace;
	}

	public Integer getStartingSequenceNumber() {
		return startingSequenceNumber;
	}

	public void setStartingSequenceNumber(Integer startingSequenceNumber) {
		this.startingSequenceNumber = startingSequenceNumber;
	}

	public String getParentageDesignationSeparator() {
		return parentageDesignationSeparator;
	}

	public void setParentageDesignationSeparator(String parentageDesignationSeparator) {
		this.parentageDesignationSeparator = parentageDesignationSeparator;
	}
}