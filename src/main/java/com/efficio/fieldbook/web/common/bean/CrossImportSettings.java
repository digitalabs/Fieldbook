package com.efficio.fieldbook.web.common.bean;

import org.generationcp.commons.settings.CrossSetting;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */


public class CrossImportSettings {


	private String name;
	private String crossPrefix;
	private Integer breedingMethodID;
	private Boolean basedOnStatusOfParentalLines;
	private String crossSuffix;
	private Integer sequenceNumberDigits;
	private Boolean hasSuffixSpace;
	private Boolean hasPrefixSpace;
	private Integer startingSequenceNumber;
	private String parentageDesignationSeparator;

	public CrossImportSettings() {
	}

	public CrossImportSettings(String name, String crossPrefix, Integer breedingMethodID,
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public void populate(CrossSetting setting) {
		this.name = setting.getName();
		this.breedingMethodID = setting.getBreedingMethodSetting().getMethodId();
		this.crossPrefix = setting.getCrossNameSetting().getPrefix();
		this.crossSuffix = setting.getCrossNameSetting().getSuffix();
		this.hasPrefixSpace = setting.getCrossNameSetting().isAddSpaceBetweenPrefixAndCode();
		this.hasSuffixSpace = setting.getCrossNameSetting().isAddSpaceBetweenSuffixAndCode();
		this.basedOnStatusOfParentalLines = setting.getBreedingMethodSetting()
				.isBasedOnStatusOfParentalLines();
		this.sequenceNumberDigits = setting.getCrossNameSetting().getNumOfDigits();
		this.startingSequenceNumber = setting.getCrossNameSetting().getStartNumber();
		this.parentageDesignationSeparator = setting.getCrossNameSetting().getSeparator();
	}

	public Boolean getBasedOnStatusOfParentalLines() {
		return basedOnStatusOfParentalLines;
	}

	public void setBasedOnStatusOfParentalLines(Boolean basedOnStatusOfParentalLines) {
		this.basedOnStatusOfParentalLines = basedOnStatusOfParentalLines;
	}
}