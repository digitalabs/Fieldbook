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
package com.efficio.fieldbook.web.fieldmap.form;

import java.util.List;

import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

// TODO: Auto-generated Javadoc
/**
 * The Class FieldmapForm.
 *
 * Form that would hold the data input for the fieldmap creation.
 */
public class FieldmapForm{
    
    /** The user fieldmap. */
    private UserFieldmap userFieldmap;
    
    /** The fieldmap labels. */
    private List<String> fieldmapLabels;
    
    /** The marked cells. */
    private String markedCells;
    
    /** The field location id all. */
    private String fieldLocationIdAll;
    
    /** The field location id favorite. */
    private String fieldLocationIdFavorite;
    
    /** The save and redirect to create label. */
    private String saveAndRedirectToCreateLabel;
    
    /** The project id. */
    private String projectId;
    
    private String programLocationUrl;
    
    
    
    /**
     * Gets the project id.
     *
     * @return the project id
     */
    public String getProjectId() {
		return projectId;
	}

	/**
	 * Sets the project id.
	 *
	 * @param projectId the new project id
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
     * Gets the save and redirect to create label.
     *
     * @return the save and redirect to create label
     */
    public String getSaveAndRedirectToCreateLabel() {
        return saveAndRedirectToCreateLabel;
    }
    
    /**
     * Sets the save and redirect to create label.
     *
     * @param saveAndRedirectToCreateLabel the new save and redirect to create label
     */
    public void setSaveAndRedirectToCreateLabel(String saveAndRedirectToCreateLabel) {
        this.saveAndRedirectToCreateLabel = saveAndRedirectToCreateLabel;
    }

    /**
     * Gets the field location id all.
     *
     * @return the field location id all
     */
    public String getFieldLocationIdAll() {
        return fieldLocationIdAll;
    }
    
    /**
     * Sets the field location id all.
     *
     * @param fieldLocationIdAll the new field location id all
     */
    public void setFieldLocationIdAll(String fieldLocationIdAll) {
        this.fieldLocationIdAll = fieldLocationIdAll;
    }
    
    /**
     * Gets the field location id favorite.
     *
     * @return the field location id favorite
     */
    public String getFieldLocationIdFavorite() {
        return fieldLocationIdFavorite;
    }
    
    /**
     * Sets the field location id favorite.
     *
     * @param fieldLocationIdFavorite the new field location id favorite
     */
    public void setFieldLocationIdFavorite(String fieldLocationIdFavorite) {
        this.fieldLocationIdFavorite = fieldLocationIdFavorite;
    }

    /**
     * Gets the fieldmap labels.
     *
     * @return the fieldmap labels
     */
    public List<String> getFieldmapLabels() {
        return fieldmapLabels;
    }
    
    /**
     * Sets the fieldmap labels.
     *
     * @param fieldmapLabels the new fieldmap labels
     */
    public void setFieldmapLabels(List<String> fieldmapLabels) {
        this.fieldmapLabels = fieldmapLabels;
    }

    /**
     * Gets the user fieldmap.
     *
     * @return the user fieldmap
     */
    public UserFieldmap getUserFieldmap() {
        return userFieldmap;
    }
    
    /**
     * Sets the user fieldmap.
     *
     * @param userFieldmap the new user fieldmap
     */
    public void setUserFieldmap(UserFieldmap userFieldmap) {
        this.userFieldmap = userFieldmap;
    }

    /**
     * Gets the marked cells.
     *
     * @return the marked cells
     */
    public String getMarkedCells() {
        return markedCells;
    }
    
    /**
     * Sets the marked cells.
     *
     * @param markedCells the new marked cells
     */
    public void setMarkedCells(String markedCells) {
        this.markedCells = markedCells;
    }

	public String getProgramLocationUrl() {
		return programLocationUrl;
	}

	public void setProgramLocationUrl(String programLocationUrl) {
		this.programLocationUrl = programLocationUrl;
	}
    
    
}
