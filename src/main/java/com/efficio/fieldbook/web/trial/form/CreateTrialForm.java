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
package com.efficio.fieldbook.web.trial.form;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;
import com.efficio.fieldbook.web.util.SettingsUtil;

// TODO: Auto-generated Javadoc

/**
 * The Class CreateNurseryForm.
 */
public class CreateTrialForm {

    /**
     * The nursery level variables.
     */
    private List<SettingDetail> studyLevelVariables;

    /**
     * The plot level variables.
     */
    private List<SettingDetail> plotLevelVariables;

    /**
     * The baseline trait variables.
     *
     * Dan V - maps to items added in the measurements tab
     */
    private List<SettingDetail> baselineTraitVariables;

    /**
     * The trial level variables.
     */
    private List<SettingDetail> trialLevelVariables;

    /**
     * The treatment factors.
     */
    private List<TreatmentFactorDetail> treatmentFactors;

    /**
     * The folder id.
     */
    private Integer folderId;

    /**
     * The folder name.
     */
    private String folderName;

    /**
     * The folder name label.
     */
    private String folderNameLabel;


    /**
     * The required fields.
     */
    private String requiredFields;


    /**
     * The trial instances.
     */
    private int trialInstances;

    /**
     * The trial environment values.
     */
    private List<List<ValueReference>> trialEnvironmentValues;
    
    private List<MeasurementVariable> measurementVariables;
    private boolean isMeasurementDataExisting;
    private Integer studyId;
    /** The name type. */
    private int nameType;
    
    /** The import date. */
    private String importDate;
    
    /** The import method id. */
    private int importMethodId;
    
    /** The import location id. */
    private int importLocationId;
    private MultipartFile file;




    /**
     * Gets the study level variables.
     *
     * @return the study level variables
     */
    public List<SettingDetail> getStudyLevelVariables() {
        return studyLevelVariables;
    }

    /**
     * Sets the study level variables.
     *
     * @param studyLevelVariables the new study level variables
     */
    public void setStudyLevelVariables(List<SettingDetail> studyLevelVariables) {
        this.studyLevelVariables = studyLevelVariables;
    }

    /**
     * Gets the plot level variables.
     *
     * @return the plotLevelVariables
     */
    public List<SettingDetail> getPlotLevelVariables() {
        return plotLevelVariables;
    }

    /**
     * Sets the plot level variables.
     *
     * @param plotLevelVariables the plotLevelVariables to set
     */
    public void setPlotLevelVariables(List<SettingDetail> plotLevelVariables) {
        this.plotLevelVariables = plotLevelVariables;
    }

    /**
     * Gets the baseline trait variables.
     *
     * @return the baselineTraitVariables
     */
    public List<SettingDetail> getBaselineTraitVariables() {
        return baselineTraitVariables;
    }

    /**
     * Sets the baseline trait variables.
     *
     * @param baselineTraitVariables the baselineTraitVariables to set
     */
    public void setBaselineTraitVariables(List<SettingDetail> baselineTraitVariables) {
        this.baselineTraitVariables = baselineTraitVariables;
    }

    /**
     * Gets the folder id.
     *
     * @return the folderId
     */
    public Integer getFolderId() {
        return folderId;
    }

    /**
     * Sets the folder id.
     *
     * @param folderId the folderId to set
     */
    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    /**
     * Gets the folder name.
     *
     * @return the folderName
     */
    public String getFolderName() {
        return folderName;
    }

    /**
     * Sets the folder name.
     *
     * @param folderName the folderName to set
     */
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    /**
     * Gets the required fields.
     *
     * @return the required fields
     */
    public String getRequiredFields() {
        return requiredFields;
    }

    /**
     * Sets the required fields.
     *
     * @param requiredFields the new required fields
     */
    public void setRequiredFields(String requiredFields) {
        this.requiredFields = requiredFields;
    }

    /**
     * Gets the trial instances.
     *
     * @return the trial instances
     */
    public int getTrialInstances() {
        return this.trialInstances;
    }

    /**
     * Sets the trial instances.
     *
     * @param trialInstances the new trial instances
     */
    public void setTrialInstances(int trialInstances) {
        this.trialInstances = trialInstances;
    }

    /**
     * Gets the trial level variables.
     *
     * @return the trial level variables
     */
    public List<SettingDetail> getTrialLevelVariables() {
        return trialLevelVariables;
    }

    /**
     * Sets the trial level variables.
     *
     * @param trialLevelVariables the new trial level variables
     */
    public void setTrialLevelVariables(List<SettingDetail> trialLevelVariables) {
        this.trialLevelVariables = trialLevelVariables;
    }

    /**
     * Gets the trial environment values.
     *
     * @return the trial environment values
     */
    public List<List<ValueReference>> getTrialEnvironmentValues() {
        return this.trialEnvironmentValues;
    }

    /**
     * Sets the trial environment values.
     *
     * @param trialEnvironmentValues the new trial environment values
     */
    public void setTrialEnvironmentValues(List<List<ValueReference>> trialEnvironmentValues) {
        this.trialEnvironmentValues = trialEnvironmentValues;
    }

    /**
     * Gets the treatment factors.
     *
     * @return the treatmentFactors
     */
    public List<TreatmentFactorDetail> getTreatmentFactors() {
        return treatmentFactors;
    }

    /**
     * Sets the treatment factors.
     *
     * @param treatmentFactors the treatmentFactors to set
     */
    public void setTreatmentFactors(List<TreatmentFactorDetail> treatmentFactors) {
        this.treatmentFactors = treatmentFactors;
    }

    public String getFolderNameLabel() {
        return folderNameLabel;
    }

    public void setFolderNameLabel(String folderNameLabel) {
        this.folderNameLabel = folderNameLabel;
    }

	public List<MeasurementVariable> getMeasurementVariables() {
		return measurementVariables;
	}

	public void setMeasurementVariables(
			List<MeasurementVariable> measurementVariables) {
		this.measurementVariables = measurementVariables;
	}

	public boolean isMeasurementDataExisting() {
		return isMeasurementDataExisting;
	}

	public void setMeasurementDataExisting(boolean isMeasurementDataExisting) {
		this.isMeasurementDataExisting = isMeasurementDataExisting;
	}

	public List<MeasurementVariable> getArrangeMeasurementVariables(){
		return SettingsUtil.getArrangedMeasurementVariable(getMeasurementVariables());
	}

	public Integer getStudyId() {
		return studyId;
	}

	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}

	public int getNameType() {
		return nameType;
	}

	public void setNameType(int nameType) {
		this.nameType = nameType;
	}

	public String getImportDate() {
		return importDate;
	}

	public void setImportDate(String importDate) {
		this.importDate = importDate;
	}

	public int getImportMethodId() {
		return importMethodId;
	}

	public void setImportMethodId(int importMethodId) {
		this.importMethodId = importMethodId;
	}

	public int getImportLocationId() {
		return importLocationId;
	}

	public void setImportLocationId(int importLocationId) {
		this.importLocationId = importLocationId;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}
}
