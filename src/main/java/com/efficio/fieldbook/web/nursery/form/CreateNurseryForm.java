/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.form;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;


public class CreateNurseryForm {

	private String projectId;
	
	private int selectedSettingId;

	private List<SettingDetail> nurseryLevelVariables;
	
	private List<SettingDetail> plotLevelVariables;
	
	private List<SettingDetail> baselineTraitVariables;

/*	private ImportedGermplasmMainInfo importedGermplasmMainInfo;
	
	private List<ImportedGermplasm> importedGermplasm;

	private List<ImportedGermplasm> paginatedImportedGermplasm;	

	private int currentPage;

	private int totalPages;

	private int resultPerPage = 100;	

	private Integer[] check;
	
	private String chooseSpecifyCheck;
*/	
    private Integer folderId;
    
    private String folderName;
    
    private boolean fieldLayoutRandom = true;


    /**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the selectedSettingId
	 */
	public int getSelectedSettingId() {
		return selectedSettingId;
	}

	/**
	 * @param selectedSettingId the selectedSettingId to set
	 */
	public void setSelectedSettingId(int selectedSettingId) {
		this.selectedSettingId = selectedSettingId;
	}

	/**
	 * @return the nurseryLevelVariables
	 */
	public List<SettingDetail> getNurseryLevelVariables() {
		return nurseryLevelVariables;
	}

	/**
	 * @param nurseryLevelVariables the nurseryLevelVariables to set
	 */
	public void setNurseryLevelVariables(List<SettingDetail> nurseryLevelVariables) {
		this.nurseryLevelVariables = nurseryLevelVariables;
	}

	/**
	 * @return the plotLevelVariables
	 */
	public List<SettingDetail> getPlotLevelVariables() {
		return plotLevelVariables;
	}

	/**
	 * @param plotLevelVariables the plotLevelVariables to set
	 */
	public void setPlotLevelVariables(List<SettingDetail> plotLevelVariables) {
		this.plotLevelVariables = plotLevelVariables;
	}

	/**
	 * @return the baselineTraitVariables
	 */
	public List<SettingDetail> getBaselineTraitVariables() {
		return baselineTraitVariables;
	}

	/**
	 * @param baselineTraitVariables the baselineTraitVariables to set
	 */
	public void setBaselineTraitVariables(List<SettingDetail> baselineTraitVariables) {
		this.baselineTraitVariables = baselineTraitVariables;
	}

	/**
	 * @return the folderId
	 */
	public Integer getFolderId() {
		return folderId;
	}

	/**
	 * @param folderId the folderId to set
	 */
	public void setFolderId(Integer folderId) {
		this.folderId = folderId;
	}

	/**
	 * @return the folderName
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * @param folderName the folderName to set
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	/**
	 * @return the fieldLayoutRandom
	 */
	public boolean isFieldLayoutRandom() {
		return fieldLayoutRandom;
	}

	/**
	 * @param fieldLayoutRandom the fieldLayoutRandom to set
	 */
	public void setFieldLayoutRandom(boolean fieldLayoutRandom) {
		this.fieldLayoutRandom = fieldLayoutRandom;
	}

	
}
