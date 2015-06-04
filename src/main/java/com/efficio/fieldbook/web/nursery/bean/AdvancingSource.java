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

import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;

import com.efficio.fieldbook.web.common.bean.AdvanceGermplasmChangeDetail;

/**
 *
 * The POJO containing information needed for Advancing.
 *
 */
public class AdvancingSource {

	private ImportedGermplasm germplasm;
	private List<Name> names;
	private Integer plantsSelected;
	private Method breedingMethod;
	private boolean isCheck;
	private boolean isBulk;
	private String nurseryName;
	private String season;
	private String locationAbbreviation;
	private String rootName;
	private Method sourceMethod;
	private int currentMaxSequence;
	private AdvanceGermplasmChangeDetail changeDetail;
	private String prefix;
	private String suffix;
	private Integer rootNameType;

	private boolean isForceUniqueNameGeneration;

	public AdvancingSource(ImportedGermplasm germplasm, List<Name> names, Integer plantsSelected, Method breedingMethod, boolean isCheck,
			String nurseryName, String season, String locationAbbreviation) {
		super();
		this.germplasm = germplasm;
		this.names = names;
		this.plantsSelected = plantsSelected;
		this.breedingMethod = breedingMethod;
		this.isCheck = isCheck;
		this.nurseryName = nurseryName;
		this.season = season;
		this.locationAbbreviation = locationAbbreviation;
	}

	public AdvancingSource() {
		super();
	}

	/**
	 * @return the germplasm
	 */
	public ImportedGermplasm getGermplasm() {
		return this.germplasm;
	}

	/**
	 * @param germplasm the germplasm to set
	 */
	public void setGermplasm(ImportedGermplasm germplasm) {
		this.germplasm = germplasm;
	}

	/**
	 * @return the plantsSelected
	 */
	public Integer getPlantsSelected() {
		return this.plantsSelected;
	}

	/**
	 * @param plantsSelected the plantsSelected to set
	 */
	public void setPlantsSelected(Integer plantsSelected) {
		this.plantsSelected = plantsSelected;
	}

	/**
	 * @return the isCheck
	 */
	public boolean isCheck() {
		return this.isCheck;
	}

	/**
	 * @param isCheck the isCheck to set
	 */
	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	 /**
	  * @return the isBulk
	  */
	 public boolean isBulk() {
		 Boolean isBulk = this.getBreedingMethod().isBulkingMethod();
		 return this.getBreedingMethod() != null && isBulk != null ? isBulk : false;
	 }

	 /**
	  * @param isBulk the isBulk to set
	  */
	 public void setBulk(boolean isBulk) {
		 this.isBulk = isBulk;
	 }

	 /**
	  * @return the names
	  */
	 public List<Name> getNames() {
		 return this.names;
	 }

	 /**
	  * @param names the names to set
	  */
	 public void setNames(List<Name> names) {
		 this.names = names;
	 }

	 /**
	  * @return the breedingMethod
	  */
	 public Method getBreedingMethod() {
		 return this.breedingMethod;
	 }

	 /**
	  * @param breedingMethod the breedingMethod to set
	  */
	 public void setBreedingMethod(Method breedingMethod) {
		 this.breedingMethod = breedingMethod;
	 }

	 /**
	  * @return the nurseryName
	  */
	 public String getNurseryName() {
		 return this.nurseryName;
	 }

	 /**
	  * @param nurseryName the nurseryName to set
	  */
	 public void setNurseryName(String nurseryName) {
		 this.nurseryName = nurseryName;
	 }

	 /**
	  * @return the season
	  */
	 public String getSeason() {
		 return this.season;
	 }

	 /**
	  * @param season the season to set
	  */
	 public void setSeason(String season) {
		 this.season = season;
	 }

	 /**
	  * @return the locationAbbreviation
	  */
	 public String getLocationAbbreviation() {
		 return this.locationAbbreviation;
	 }

	 /**
	  * @param locationAbbreviation the locationAbbreviation to set
	  */
	 public void setLocationAbbreviation(String locationAbbreviation) {
		 this.locationAbbreviation = locationAbbreviation;
	 }

	 /**
	  * @return the rootName
	  */
	 public String getRootName() {
		 return this.rootName;
	 }

	 /**
	  * @param rootName the rootName to set
	  */
	 public void setRootName(String rootName) {
		 this.rootName = rootName;
	 }

	 /**
	  * @return the sourceMethod
	  */
	 public Method getSourceMethod() {
		 return this.sourceMethod;
	 }

	 /**
	  * @param sourceMethod the sourceMethod to set
	  */
	 public void setSourceMethod(Method sourceMethod) {
		 this.sourceMethod = sourceMethod;
	 }

	 /**
	  * @return the currentMaxSequence
	  */
	 public int getCurrentMaxSequence() {
		 return this.currentMaxSequence;
	 }

	 /**
	  * @param currentMaxSequence the currentMaxSequence to set
	  */
	 public void setCurrentMaxSequence(int currentMaxSequence) {
		 this.currentMaxSequence = currentMaxSequence;
	 }

	 /**
	  * @return the changeDetail
	  */
	 public AdvanceGermplasmChangeDetail getChangeDetail() {
		 return this.changeDetail;
	 }

	 /**
	  * @param changeDetail the changeDetail to set
	  */
	 public void setChangeDetail(AdvanceGermplasmChangeDetail changeDetail) {
		 this.changeDetail = changeDetail;
	 }

	 /**
	  * @return the prefix
	  */
	 public String getPrefix() {
		 return this.prefix;
	 }

	 /**
	  * @param prefix the prefix to set
	  */
	 public void setPrefix(String prefix) {
		 this.prefix = prefix;
	 }

	 /**
	  * @return the suffix
	  */
	 public String getSuffix() {
		 return this.suffix;
	 }

	 /**
	  * @param suffix the suffix to set
	  */
	 public void setSuffix(String suffix) {
		 this.suffix = suffix;
	 }

	 public boolean isForceUniqueNameGeneration() {
		 return this.isForceUniqueNameGeneration;
	 }

	 public void setForceUniqueNameGeneration(boolean isForceUniqueNameGeneration) {
		 this.isForceUniqueNameGeneration = isForceUniqueNameGeneration;
	 }

	 public Integer getRootNameType() {
		 return this.rootNameType;
	 }

	 public void setRootNameType(Integer rootNameType) {
		 this.rootNameType = rootNameType;
	 }

	 @Override
	 public String toString() {
		 return "AdvancingSource [germplasm=" + this.germplasm + ", names=" + this.names + ", plantsSelected=" + this.plantsSelected
				+ ", breedingMethod=" + this.breedingMethod + ", isCheck=" + this.isCheck + ", isBulk=" + this.isBulk + ", nurseryName="
				+ this.nurseryName + ", season=" + this.season + ", locationAbbreviation=" + this.locationAbbreviation + ", rootName="
				+ this.rootName + ", sourceMethod=" + this.sourceMethod + ", currentMaxSequence=" + this.currentMaxSequence
				+ ", changeDetail=" + this.changeDetail + ", prefix=" + this.prefix + ", suffix=" + this.suffix + "]";
	 }

}
