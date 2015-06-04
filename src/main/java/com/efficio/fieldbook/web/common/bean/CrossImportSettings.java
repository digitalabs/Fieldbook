
package com.efficio.fieldbook.web.common.bean;

import org.generationcp.commons.settings.CrossSetting;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
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
	private Integer locationID;

	public CrossImportSettings() {
	}

	public CrossImportSettings(String name, String crossPrefix, Integer breedingMethodID, Boolean basedOnStatusOfParentalLines,
			String crossSuffix, Integer sequenceNumberDigits, Boolean hasSuffixSpace, Boolean hasPrefixSpace,
			Integer startingSequenceNumber, String parentageDesignationSeparator, String harvestYear, String harvestMonth,
			Integer locationID) {
		this.name = name;
		this.crossPrefix = crossPrefix;
		this.breedingMethodID = breedingMethodID;
		this.basedOnStatusOfParentalLines = basedOnStatusOfParentalLines;
		this.crossSuffix = crossSuffix;
		this.sequenceNumberDigits = sequenceNumberDigits;
		this.hasSuffixSpace = hasSuffixSpace;
		this.hasPrefixSpace = hasPrefixSpace;
		this.startingSequenceNumber = startingSequenceNumber;
		this.parentageDesignationSeparator = parentageDesignationSeparator;
		this.locationID = locationID;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCrossPrefix() {
		return this.crossPrefix;
	}

	public void setCrossPrefix(String crossPrefix) {
		this.crossPrefix = crossPrefix;
	}

	public Integer getBreedingMethodID() {
		return this.breedingMethodID;
	}

	public void setBreedingMethodID(Integer breedingMethodID) {
		this.breedingMethodID = breedingMethodID;
	}

	public String getCrossSuffix() {
		return this.crossSuffix;
	}

	public void setCrossSuffix(String crossSuffix) {
		this.crossSuffix = crossSuffix;
	}

	public Integer getSequenceNumberDigits() {
		return this.sequenceNumberDigits;
	}

	public void setSequenceNumberDigits(Integer sequenceNumberDigits) {
		this.sequenceNumberDigits = sequenceNumberDigits;
	}

	public Boolean getHasSuffixSpace() {
		return this.hasSuffixSpace;
	}

	public void setHasSuffixSpace(Boolean hasSuffixSpace) {
		this.hasSuffixSpace = hasSuffixSpace;
	}

	public Boolean getHasPrefixSpace() {
		return this.hasPrefixSpace;
	}

	public void setHasPrefixSpace(Boolean hasPrefixSpace) {
		this.hasPrefixSpace = hasPrefixSpace;
	}

	public Integer getStartingSequenceNumber() {
		return this.startingSequenceNumber;
	}

	public void setStartingSequenceNumber(Integer startingSequenceNumber) {
		this.startingSequenceNumber = startingSequenceNumber;
	}

	public String getParentageDesignationSeparator() {
		return this.parentageDesignationSeparator;
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
		this.basedOnStatusOfParentalLines = setting.getBreedingMethodSetting().isBasedOnStatusOfParentalLines();
		this.sequenceNumberDigits = setting.getCrossNameSetting().getNumOfDigits();
		this.startingSequenceNumber = setting.getCrossNameSetting().getStartNumber();
		this.parentageDesignationSeparator = setting.getCrossNameSetting().getSeparator();

		this.locationID = setting.getAdditionalDetailsSetting().getHarvestLocationId();
	}

	public Boolean getBasedOnStatusOfParentalLines() {
		return this.basedOnStatusOfParentalLines;
	}

	public void setBasedOnStatusOfParentalLines(Boolean basedOnStatusOfParentalLines) {
		this.basedOnStatusOfParentalLines = basedOnStatusOfParentalLines;
	}

	public Integer getLocationID() {
		return this.locationID;
	}

	public void setLocationID(Integer locationID) {
		this.locationID = locationID;
	}
}
