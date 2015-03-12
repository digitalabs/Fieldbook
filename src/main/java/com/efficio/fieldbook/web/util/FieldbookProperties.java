package com.efficio.fieldbook.web.util;

import org.generationcp.commons.util.WorkbenchAppPathResolver;

public class FieldbookProperties {
	private String programLocationsUrl;
	private String programBreedingMethodsUrl;
	private String germplasmImportUrl;
	private String germplasmDetailsUrl;
	
	private String uploadDirectory;

	public String getProgramLocationsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(programLocationsUrl);
	}

	public void setProgramLocationsUrl(String programLocationsUrl) {
		this.programLocationsUrl = programLocationsUrl;
	}

	public String getProgramBreedingMethodsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(programBreedingMethodsUrl);
	}

	public void setProgramBreedingMethodsUrl(String programBreedingMethodsUrl) {
		this.programBreedingMethodsUrl = programBreedingMethodsUrl;
	}

	public String getGermplasmImportUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(germplasmImportUrl);
	}

	public void setGermplasmImportUrl(String germplasmImportUrl) {
		this.germplasmImportUrl = germplasmImportUrl;
	}

	public String getGermplasmDetailsUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(germplasmDetailsUrl);
	}

	public void setGermplasmDetailsUrl(String germplasmDetailsUrl) {
		this.germplasmDetailsUrl = germplasmDetailsUrl;
	}

	public String getUploadDirectory() {
		return uploadDirectory;
	}

	public void setUploadDirectory(String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

}
