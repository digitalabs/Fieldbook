
package com.efficio.fieldbook.web.util;

import org.generationcp.commons.util.WorkbenchAppPathResolver;

public class FieldbookProperties {

	private String programLocationsUrl;
	private String programBreedingMethodsUrl;
	private String germplasmImportUrl;
	private String germplasmDetailsUrl;

	private String uploadDirectory;

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

	public String getGermplasmImportUrl() {
		return WorkbenchAppPathResolver.getFullWebAddress(this.germplasmImportUrl);
	}

	public void setGermplasmImportUrl(String germplasmImportUrl) {
		this.germplasmImportUrl = germplasmImportUrl;
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

}
