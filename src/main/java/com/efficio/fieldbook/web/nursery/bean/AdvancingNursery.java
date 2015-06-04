/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.nursery.bean;

import java.io.Serializable;

import org.generationcp.middleware.domain.dms.Study;

/**
 * This bean models the various input that the user builds up over time to perform the actual loading operation.
 */
public class AdvancingNursery implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Study study;

	/** The method choice. */
	private String methodChoice;

	/** The line choice. */
	private String lineChoice;

	/** The line selected. */
	private String lineSelected;

	/** The harvest date. */
	private String harvestDate;

	/** The harvest location id. */
	private String harvestLocationId;

	/** The harvest location abbreviation. */
	private String harvestLocationAbbreviation;

	/** The breeding method id. */
	private String breedingMethodId;

	private String putBrackets;

	private String allPlotsChoice;

	private Integer lineVariateId;

	private Integer methodVariateId;

	private Integer plotVariateId;

	private boolean isCheckAdvanceLinesUnique;

	public Study getStudy() {
		return this.study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	/**
	 * Gets the harvest location abbreviation.
	 *
	 * @return the harvest location abbreviation
	 */
	public String getHarvestLocationAbbreviation() {
		return this.harvestLocationAbbreviation;
	}

	/**
	 * Sets the harvest location abbreviation.
	 *
	 * @param harvestLocationAbbreviation the new harvest location abbreviation
	 */
	public void setHarvestLocationAbbreviation(String harvestLocationAbbreviation) {
		this.harvestLocationAbbreviation = harvestLocationAbbreviation;
	}

	/**
	 * Gets the harvest location id.
	 *
	 * @return the harvest location id
	 */
	public String getHarvestLocationId() {
		return this.harvestLocationId;
	}

	/**
	 * Sets the harvest location id.
	 *
	 * @param harvestLocationId the new harvest location id
	 */
	public void setHarvestLocationId(String harvestLocationId) {
		this.harvestLocationId = harvestLocationId;
	}

	/**
	 * Gets the harvest date.
	 *
	 * @return the harvest date
	 */
	public String getHarvestDate() {
		return this.harvestDate;
	}

	/**
	 * Sets the harvest date.
	 *
	 * @param harvestDate the new harvest date
	 */
	public void setHarvestDate(String harvestDate) {
		this.harvestDate = harvestDate;
	}

	/**
	 * Gets the breeding method id.
	 *
	 * @return the breeding method id
	 */
	public String getBreedingMethodId() {
		return this.breedingMethodId;
	}

	/**
	 * Sets the breeding method id.
	 *
	 * @param breedingMethodId the new breeding method id
	 */
	public void setBreedingMethodId(String breedingMethodId) {
		this.breedingMethodId = breedingMethodId;
	}

	/**
	 * Gets the method choice.
	 *
	 * @return the method choice
	 */
	public String getMethodChoice() {
		return this.methodChoice;
	}

	/**
	 * Sets the method choice.
	 *
	 * @param methodChoice the new method choice
	 */
	public void setMethodChoice(String methodChoice) {
		this.methodChoice = methodChoice;
	}

	/**
	 * Gets the line choice.
	 *
	 * @return the line choice
	 */
	public String getLineChoice() {
		return this.lineChoice;
	}

	/**
	 * Sets the line choice.
	 *
	 * @param lineChoice the new line choice
	 */
	public void setLineChoice(String lineChoice) {
		this.lineChoice = lineChoice;
	}

	/**
	 * Gets the line selected.
	 *
	 * @return the line selected
	 */
	public String getLineSelected() {
		return this.lineSelected;
	}

	public String getPutBrackets() {
		return this.putBrackets;
	}

	public void setPutBrackets(String putBrackets) {
		this.putBrackets = putBrackets;
	}

	/**
	 * @return the allPlotsChoice
	 */
	public String getAllPlotsChoice() {
		return this.allPlotsChoice;
	}

	/**
	 * @param allPlotsChoice the allPlotsChoice to set
	 */
	public void setAllPlotsChoice(String allPlotsChoice) {
		this.allPlotsChoice = allPlotsChoice;
	}

	/**
	 * Sets the line selected.
	 *
	 * @param lineSelected the new line selected
	 */
	public void setLineSelected(String lineSelected) {
		this.lineSelected = lineSelected;
	}

	/**
	 * @return the lineVariateId
	 */
	public Integer getLineVariateId() {
		return this.lineVariateId;
	}

	/**
	 * @param lineVariateId the lineVariateId to set
	 */
	public void setLineVariateId(Integer lineVariateId) {
		this.lineVariateId = lineVariateId;
	}

	/**
	 * @return the methodVariateId
	 */
	public Integer getMethodVariateId() {
		return this.methodVariateId;
	}

	/**
	 * @param methodVariateId the methodVariateId to set
	 */
	public void setMethodVariateId(Integer methodVariateId) {
		this.methodVariateId = methodVariateId;
	}

	/**
	 * @return the plotVariateId
	 */
	public Integer getPlotVariateId() {
		return this.plotVariateId;
	}

	/**
	 * @param plotVariateId the plotVariateId to set
	 */
	public void setPlotVariateId(Integer plotVariateId) {
		this.plotVariateId = plotVariateId;
	}

	public boolean isCheckAdvanceLinesUnique() {
		return this.isCheckAdvanceLinesUnique;
	}

	public void setCheckAdvanceLinesUnique(boolean isCheckAdvanceLinesUnique) {
		this.isCheckAdvanceLinesUnique = isCheckAdvanceLinesUnique;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AdvancingNursery [study=");
		builder.append(this.study);
		builder.append(", methodChoice=");
		builder.append(this.methodChoice);
		builder.append(", lineChoice=");
		builder.append(this.lineChoice);
		builder.append(", lineSelected=");
		builder.append(this.lineSelected);
		builder.append(", harvestDate=");
		builder.append(this.harvestDate);
		builder.append(", harvestLocationId=");
		builder.append(this.harvestLocationId);
		builder.append(", harvestLocationAbbreviation=");
		builder.append(this.harvestLocationAbbreviation);
		builder.append(", breedingMethodId=");
		builder.append(this.breedingMethodId);
		builder.append("]");
		return builder.toString();
	}
}
