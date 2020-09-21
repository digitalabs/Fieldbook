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

package com.efficio.fieldbook.web.trial.bean;

import java.io.Serializable;
import java.util.Set;

import com.efficio.fieldbook.web.trial.bean.AdvanceType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.generationcp.middleware.domain.dms.Study;

/**
 * This bean models the various input that the user builds up over time to perform the actual loading operation.
 */
public class AdvancingStudy implements Serializable {

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

	private String allPlotsChoice;

	private Integer lineVariateId;

	private Integer methodVariateId;

	private Integer plotVariateId;

	private boolean isCheckAdvanceLinesUnique;

	private Set<String> selectedTrialInstances;

    private Set<String> selectedReplications;

	private AdvanceType advanceType;

	public AdvancingStudy() {

	}

	public AdvancingStudy(Study study, String methodChoice, String lineChoice, String lineSelected, String harvestDate, String harvestLocationId, String harvestLocationAbbreviation, String breedingMethodId, String allPlotsChoice, Integer lineVariateId, Integer methodVariateId, Integer plotVariateId, boolean isCheckAdvanceLinesUnique, Set<String> selectedTrialInstances, Set<String> selectedReplications, AdvanceType advanceType) {
		this.study = study;
		this.methodChoice = methodChoice;
		this.lineChoice = lineChoice;
		this.lineSelected = lineSelected;
		this.harvestDate = harvestDate;
		this.harvestLocationId = harvestLocationId;
		this.harvestLocationAbbreviation = harvestLocationAbbreviation;
		this.breedingMethodId = breedingMethodId;
		this.allPlotsChoice = allPlotsChoice;
		this.lineVariateId = lineVariateId;
		this.methodVariateId = methodVariateId;
		this.plotVariateId = plotVariateId;
		this.isCheckAdvanceLinesUnique = isCheckAdvanceLinesUnique;
		this.selectedTrialInstances = selectedTrialInstances;
		this.selectedReplications = selectedReplications;
		this.advanceType = advanceType;
	}

	public Set<String> getSelectedTrialInstances() {
		return this.selectedTrialInstances;
	}

	public void setSelectedTrialInstances(Set<String> selectedTrialInstances) {
		this.selectedTrialInstances = selectedTrialInstances;
	}

	public Set<String> getSelectedReplications() {
		return this.selectedReplications;
	}
	
	public void setSelectedReplications(Set<String> selectedReplications) {
		this.selectedReplications = selectedReplications;
	}

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

	public AdvanceType getAdvanceType() {
		return advanceType;
	}

	public void setAdvanceType(AdvanceType advanceType) {
		this.advanceType = advanceType;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
