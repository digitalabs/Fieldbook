package com.efficio.fieldbook.web.util;

public class FieldbookProperties {

	private String programLocationsUrl;
	private String programBreedingMethodsUrl;
	private String germplasmImportUrl;
	private String germplasmDetailsUrl;
	
	private String uploadDirectory;

	public String getProgramLocationsUrl() {
		return programLocationsUrl;
	}

	public void setProgramLocationsUrl(String programLocationsUrl) {
		this.programLocationsUrl = programLocationsUrl;
	}

	public String getProgramBreedingMethodsUrl() {
		return programBreedingMethodsUrl;
	}

	public void setProgramBreedingMethodsUrl(String programBreedingMethodsUrl) {
		this.programBreedingMethodsUrl = programBreedingMethodsUrl;
	}

	public String getGermplasmImportUrl() {
		return germplasmImportUrl;
	}

	public void setGermplasmImportUrl(String germplasmImportUrl) {
		this.germplasmImportUrl = germplasmImportUrl;
	}

	public String getGermplasmDetailsUrl() {
		return germplasmDetailsUrl;
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
