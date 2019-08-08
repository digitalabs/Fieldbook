
package com.efficio.fieldbook.web.util;

import org.generationcp.commons.util.WorkbenchAppPathResolver;

public class FieldbookProperties {

	private String programLocationsUrl;
	private String programBreedingMethodsUrl;
	private String germplasmDetailsUrl;

	private String bvDesignPath;
	private Integer bvDesignRunnerTimeout;
	
	private String uploadDirectory;
	private Integer maxNumOfSubObsSetsPerStudy;
	private Integer maxNumOfSubObsPerParentUnit;


	public String getProgramLocationsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(this.programLocationsUrl);
	}

	public void setProgramLocationsUrl(String programLocationsUrl) {
		this.programLocationsUrl = programLocationsUrl;
	}

	public String getProgramBreedingMethodsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(this.programBreedingMethodsUrl);
	}

	public void setProgramBreedingMethodsUrl(String programBreedingMethodsUrl) {
		this.programBreedingMethodsUrl = programBreedingMethodsUrl;
	}

	public String getGermplasmDetailsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(this.germplasmDetailsUrl);
	}

	public void setGermplasmDetailsUrl(String germplasmDetailsUrl) {
		this.germplasmDetailsUrl = germplasmDetailsUrl;
	}

	public String getUploadDirectory() {
		return this.uploadDirectory;
	}

	public void setUploadDirectory(String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

	public String getBvDesignPath() {
		return bvDesignPath;
	}

	public void setBvDesignPath(final String bvDesignPath) {
		this.bvDesignPath = bvDesignPath;
	}

	public Integer getMaxNumOfSubObsSetsPerStudy() {
		return maxNumOfSubObsSetsPerStudy;
	}

	public void setMaxNumOfSubObsSetsPerStudy(Integer maxNumOfSubObsSetsPerStudy) {
		this.maxNumOfSubObsSetsPerStudy = maxNumOfSubObsSetsPerStudy;
	}

	public Integer getMaxNumOfSubObsPerParentUnit() {
		return maxNumOfSubObsPerParentUnit;
	}

	public void setMaxNumOfSubObsPerParentUnit(Integer maxNumOfSubObsPerParentUnit) {
		this.maxNumOfSubObsPerParentUnit = maxNumOfSubObsPerParentUnit;
	}

	public Integer getBvDesignRunnerTimeout() {
		return bvDesignRunnerTimeout;
	}

	public void setBvDesignRunnerTimeout(final Integer bvDesignRunnerTimeout) {
		this.bvDesignRunnerTimeout = bvDesignRunnerTimeout;
	}
}
