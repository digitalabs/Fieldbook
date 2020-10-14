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

package com.efficio.fieldbook.web.common.bean;

import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.springframework.web.util.HtmlUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/**
 * This bean models the various input that the user builds up over time to perform the actual loading operation.
 */
public class UserSelection implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The actual file name. */
	private String actualFileName;

	/** The server file name. */
	private String serverFileName;

	/** The field layout random. */
	private boolean fieldLayoutRandom;

	/** The imported germplasm main info. */
	private ImportedGermplasmMainInfo importedGermplasmMainInfo;

	/** The imported check germplasm main info. */
	private ImportedGermplasmMainInfo importedCheckGermplasmMainInfo;

	/** The is import valid. */
	private boolean isImportValid;

	/** The study details list. */
	private transient List<StudyDetails> studyDetailsList;

	/** The current page germplasm list. */
	private int currentPageGermplasmList;

	/** The current page check germplasm list. */
	private int currentPageCheckGermplasmList;

	/** study level conditions in Manage Settings. */
	private List<SettingDetail> studyLevelConditions = new ArrayList<>();

	/** Plot level factors in Manage Settings. */
	private List<SettingDetail> plotsLevelList;

	/** Baseline Traits in Manage Settings. */
	private List<SettingDetail> baselineTraitsList;

	/** The trial level variable list. */
	private List<SettingDetail> trialLevelVariableList;

	/** The trial environment values. */
	private List<List<ValueReference>> trialEnvironmentValues;

	/** The imported advanced germplasm list. */
	private List<ImportedGermplasm> importedAdvancedGermplasmList;

	/** The trait ref list. */
	private List<TraitClassReference> traitRefList;

	/** The treatment factors. */
	private List<SettingDetail> treatmentFactors;

	/** The selection variates. */
	private List<SettingDetail> selectionVariates;

	/** The basic details. */
	private List<SettingDetail> basicDetails;

	/** The study conditions. */
	private List<SettingDetail> studyConditions;

	/** The deleted study level conditions. */
	private List<SettingDetail> deletedStudyLevelConditions;

	/** The deleted plot level list. */
	private List<SettingDetail> deletedPlotLevelList;

	/** The deleted baseline traits list. */
	private List<SettingDetail> deletedBaselineTraitsList;

	/** The deleted study conditions. */
	private List<SettingDetail> deletedStudyConditions;

	/** The deleted trial level variables. */
	private List<SettingDetail> deletedTrialLevelVariables;

	/** The deleted treatment factors. */
	private List<SettingDetail> deletedTreatmentFactors;

	/** The change details. */
	private List<GermplasmChangeDetail> changeDetails;

	/** The removed factors. */
	private List<SettingDetail> removedFactors;

	/** The removed conditions. */
	private List<SettingDetail> removedConditions;

	/** The new traits. */
	private List<SettingDetail> newTraits;

	/** The new selection variates. */
	private List<SettingDetail> newSelectionVariates;

	/** The workbook. */
	private Workbook workbook;

	/** The temporary workbook. */
	private Workbook temporaryWorkbook;

	/* The data from imported Design file (CSV/Excel) */
	private DesignImportData designImportData;

	/** The current page. */
	private int currentPage;

	/** The measurement row list. */
	private List<MeasurementRow> measurementRowList;
	private List<MeasurementVariable> measurementDatasetVariable;

	private List<MeasurementVariable> constantsWithLabels;

	private ExpDesignParameterUi expDesignParams;
	private List<Integer> expDesignVariables;
	/**
	 * The List of experimental design variables (new, edit, or deleted) since the workbook was retrieved from the db, will be reset upon
	 * save
	 */
	private List<MeasurementVariable> experimentalDesignVariables;
	private ImportedCrossesList importedCrossesList;
	private List<Integer> importedCrossesId;

	private CrossSetting crossSettings;

	private Integer listId;

	List<InventoryDetails> previousInventoryDetails;

	private Integer startingPlotNo;
	private String studyName;
	private String studyDescription;
	private String studyObjective;
	private String studyStartDate;
	private String studyEndDate;
	private String studyUpdate;
	private String studyType;
	private String createdBy;

	/**
	 * Gets the current page germplasm list.
	 *
	 * @return the current page germplasm list
	 */
	public int getCurrentPageGermplasmList() {
		return this.currentPageGermplasmList;
	}

	/**
	 * Sets the current page germplasm list.
	 *
	 * @param currentPageGermplasmList the new current page germplasm list
	 */
	public void setCurrentPageGermplasmList(final int currentPageGermplasmList) {
		this.currentPageGermplasmList = currentPageGermplasmList;
	}

	/**
	 * Gets the current page check germplasm list.
	 *
	 * @return the current page check germplasm list
	 */
	public int getCurrentPageCheckGermplasmList() {
		return this.currentPageCheckGermplasmList;
	}

	/**
	 * Sets the current page check germplasm list.
	 *
	 * @param currentPageCheckGermplasmList the new current page check germplasm list
	 */
	public void setCurrentPageCheckGermplasmList(final int currentPageCheckGermplasmList) {
		this.currentPageCheckGermplasmList = currentPageCheckGermplasmList;
	}

	/**
	 * Checks if is import valid.
	 *
	 * @return true, if is import valid
	 */
	public boolean isImportValid() {
		return this.isImportValid;
	}

	/**
	 * Sets the import valid.
	 *
	 * @param isImportValid the new import valid
	 */
	public void setImportValid(final boolean isImportValid) {
		this.isImportValid = isImportValid;
	}

	/**
	 * Gets the imported germplasm main info.
	 *
	 * @return the imported germplasm main info
	 */
	public ImportedGermplasmMainInfo getImportedGermplasmMainInfo() {
		return this.importedGermplasmMainInfo;
	}

	/**
	 * Sets the imported germplasm main info.
	 *
	 * @param importedGermplasmMainInfo the new imported germplasm main info
	 */
	public void setImportedGermplasmMainInfo(final ImportedGermplasmMainInfo importedGermplasmMainInfo) {
		this.importedGermplasmMainInfo = importedGermplasmMainInfo;
	}

	/**
	 * Gets the actual file name.
	 *
	 * @return the actual file name
	 */
	public String getActualFileName() {
		return this.actualFileName;
	}

	/**
	 * Sets the actual file name.
	 *
	 * @param actualFileName the new actual file name
	 */
	public void setActualFileName(final String actualFileName) {
		this.actualFileName = actualFileName;
	}

	/**
	 * Gets the server file name.
	 *
	 * @return the server file name
	 */
	public String getServerFileName() {
		return this.serverFileName;
	}

	/**
	 * Sets the server file name.
	 *
	 * @param serverFileName the new server file name
	 */
	public void setServerFileName(final String serverFileName) {
		this.serverFileName = serverFileName;
	}

	/**
	 * Gets the workbook.
	 *
	 * @return the workbook
	 */
	public Workbook getWorkbook() {
		return this.workbook;
	}

	/**
	 * Sets the workbook.
	 *
	 * @param workbook the new workbook
	 */
	public void setWorkbook(final Workbook workbook) {
		this.workbook = workbook;
	}

	/**
	 * Gets the field layout random.
	 *
	 * @return the field layout random
	 */
	public Boolean getFieldLayoutRandom() {
		return this.fieldLayoutRandom;
	}

	/**
	 * Sets the field layout random.
	 *
	 * @param fieldLayoutRandom the new field layout random
	 */
	public void setFieldLayoutRandom(final Boolean fieldLayoutRandom) {
		this.fieldLayoutRandom = fieldLayoutRandom;
	}

	/**
	 * Gets the study details list.
	 *
	 * @return the study details list
	 */
	public List<StudyDetails> getStudyDetailsList() {
		return this.studyDetailsList;
	}

	/**
	 * Sets the study details list.
	 *
	 * @param studyDetailsList the new study details list
	 */
	public void setStudyDetailsList(final List<StudyDetails> studyDetailsList) {
		this.studyDetailsList = studyDetailsList;
	}

	/**
	 * Gets the study level conditions.
	 *
	 * @return the study level conditions
	 */
	public List<SettingDetail> getStudyLevelConditions() {
		return this.studyLevelConditions;
	}

	/**
	 * Sets the study level conditions.
	 *
	 * @param studyLevelConditions the new study level conditions
	 */
	public void setStudyLevelConditions(final List<SettingDetail> studyLevelConditions) {
		this.studyLevelConditions = studyLevelConditions;
	}

	/**
	 * Gets the plots level list.
	 *
	 * @return the plotsLevelList
	 */
	public List<SettingDetail> getPlotsLevelList() {
		return this.plotsLevelList;
	}

	/**
	 * Sets the plots level list.
	 *
	 * @param plotsLevelList the plotsLevelList to set
	 */
	public void setPlotsLevelList(final List<SettingDetail> plotsLevelList) {
		this.plotsLevelList = plotsLevelList;
	}

	/**
	 * Gets the baseline traits list.
	 *
	 * @return the baselineTraitsList
	 */
	public List<SettingDetail> getBaselineTraitsList() {
		return this.baselineTraitsList;
	}

	/**
	 * Sets the baseline traits list.
	 *
	 * @param baselineTraitsList the baselineTraitsList to set
	 */
	public void setBaselineTraitsList(final List<SettingDetail> baselineTraitsList) {
		this.baselineTraitsList = baselineTraitsList;
	}

	/**
	 * Gets the imported advanced germplasm list.
	 *
	 * @return the imported advanced germplasm list
	 */
	public List<ImportedGermplasm> getImportedAdvancedGermplasmList() {
		return this.importedAdvancedGermplasmList;
	}

	/**
	 * Sets the imported advanced germplasm list.
	 *
	 * @param importedAdvancedGermplasmList the new imported advanced germplasm list
	 */
	public void setImportedAdvancedGermplasmList(final List<ImportedGermplasm> importedAdvancedGermplasmList) {
		this.importedAdvancedGermplasmList = importedAdvancedGermplasmList;
	}

	/**
	 * Gets the trait ref list.
	 *
	 * @return the trait ref list
	 */
	public List<TraitClassReference> getTraitRefList() {
		return this.traitRefList;
	}

	/**
	 * Sets the trait ref list.
	 *
	 * @param traitRefList the new trait ref list
	 */
	public void setTraitRefList(final List<TraitClassReference> traitRefList) {
		this.traitRefList = traitRefList;
	}

	/**
	 * Gets the trial level variable list.
	 *
	 * @return the trial level variable list
	 */
	public List<SettingDetail> getTrialLevelVariableList() {
		return this.trialLevelVariableList;
	}

	/**
	 * Sets the trial level variable list.
	 *
	 * @param trialLevelVariableList the new trial level variable list
	 */
	public void setTrialLevelVariableList(final List<SettingDetail> trialLevelVariableList) {
		this.trialLevelVariableList = trialLevelVariableList;
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
	public void setTrialEnvironmentValues(final List<List<ValueReference>> trialEnvironmentValues) {
		this.trialEnvironmentValues = trialEnvironmentValues;
	}

	/**
	 * Gets the treatment factors.
	 *
	 * @return the treatment factors
	 */
	public List<SettingDetail> getTreatmentFactors() {
		return this.treatmentFactors;
	}

	/**
	 * Sets the treatment factors.
	 *
	 * @param treatmentFactors the new treatment factors
	 */
	public void setTreatmentFactors(final List<SettingDetail> treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
	}

	/**
	 * Gets the selection variates.
	 *
	 * @return the selectionVariates
	 */
	public List<SettingDetail> getSelectionVariates() {
		return this.selectionVariates;
	}

	/**
	 * Sets the selection variates.
	 *
	 * @param selectionVariates the selectionVariates to set
	 */
	public void setSelectionVariates(final List<SettingDetail> selectionVariates) {
		this.selectionVariates = selectionVariates;
	}

	/**
	 * Gets the basic details.
	 *
	 * @return the basicDetails
	 */
	public List<SettingDetail> getBasicDetails() {
		return (this.basicDetails != null ? this.basicDetails : new ArrayList<SettingDetail>());
	}

	/**
	 * Sets the basic details.
	 *
	 * @param basicDetails the basicDetails to set
	 */
	public void setBasicDetails(final List<SettingDetail> basicDetails) {
		this.basicDetails = basicDetails;
	}

	/**
	 * Gets the study conditions.
	 *
	 * @return the studyConditions
	 */
	public List<SettingDetail> getStudyConditions() {
		return this.studyConditions;
	}

	/**
	 * Sets the study conditions.
	 *
	 * @param studyConditions the studyConditions to set
	 */
	public void setStudyConditions(final List<SettingDetail> studyConditions) {
		this.studyConditions = studyConditions;
	}

	/**
	 * Gets the deleted study level conditions.
	 *
	 * @return the deletedStudyLevelConditions
	 */
	public List<SettingDetail> getDeletedStudyLevelConditions() {
		return this.deletedStudyLevelConditions;
	}

	/**
	 * Sets the deleted study level conditions.
	 *
	 * @param deletedStudyLevelConditions the deletedStudyLevelConditions to set
	 */
	public void setDeletedStudyLevelConditions(final List<SettingDetail> deletedStudyLevelConditions) {
		this.deletedStudyLevelConditions = deletedStudyLevelConditions;
	}

	/**
	 * Gets the deleted baseline traits list.
	 *
	 * @return the deletedBaselineTraitsList
	 */
	public List<SettingDetail> getDeletedBaselineTraitsList() {
		return this.deletedBaselineTraitsList;
	}

	/**
	 * Sets the deleted baseline traits list.
	 *
	 * @param deletedBaselineTraitsList the deletedBaselineTraitsList to set
	 */
	public void setDeletedBaselineTraitsList(final List<SettingDetail> deletedBaselineTraitsList) {
		this.deletedBaselineTraitsList = deletedBaselineTraitsList;
	}

	/**
	 * Gets the deleted study conditions.
	 *
	 * @return the deletedStudyConditions
	 */
	public List<SettingDetail> getDeletedStudyConditions() {
		return this.deletedStudyConditions;
	}

	/**
	 * Sets the deleted study conditions.
	 *
	 * @param deletedStudyConditions the deletedstudyConditions to set
	 */
	public void setDeletedStudyConditions(final List<SettingDetail> deletedStudyConditions) {
		this.deletedStudyConditions = deletedStudyConditions;
	}

	/**
	 * Gets the deleted plot level list.
	 *
	 * @return the deletedPlotLevelList
	 */
	public List<SettingDetail> getDeletedPlotLevelList() {
		return this.deletedPlotLevelList;
	}

	/**
	 * Sets the deleted plot level list.
	 *
	 * @param deletedPlotLevelList the deletedPlotLevelList to set
	 */
	public void setDeletedPlotLevelList(final List<SettingDetail> deletedPlotLevelList) {
		this.deletedPlotLevelList = deletedPlotLevelList;
	}

	/**
	 * Gets the change details.
	 *
	 * @return the changeDetails
	 */
	public List<GermplasmChangeDetail> getChangeDetails() {
		return this.changeDetails;
	}

	/**
	 * Sets the change details.
	 *
	 * @param changeDetails the changeDetails to set
	 */
	public void setChangeDetails(final List<GermplasmChangeDetail> changeDetails) {
		this.changeDetails = changeDetails;
	}

	/**
	 * Gets the removed factors.
	 *
	 * @return the removed factors
	 */
	public List<SettingDetail> getRemovedFactors() {
		return this.removedFactors;
	}

	/**
	 * Sets the removed factors.
	 *
	 * @param removedFactors the new removed factors
	 */
	public void setRemovedFactors(final List<SettingDetail> removedFactors) {
		this.removedFactors = removedFactors;
	}

	/**
	 * Gets the removed conditions.
	 *
	 * @return the removed conditions
	 */
	public List<SettingDetail> getRemovedConditions() {
		return this.removedConditions;
	}

	/**
	 * Sets the removed conditions.
	 *
	 * @param removedConditions the new removed conditions
	 */
	public void setRemovedConditions(final List<SettingDetail> removedConditions) {
		this.removedConditions = removedConditions;
	}

	/**
	 * Gets the new traits.
	 *
	 * @return the newTraits
	 */
	public List<SettingDetail> getNewTraits() {
		return this.newTraits;
	}

	/**
	 * Sets the new traits.
	 *
	 * @param newTraits the newTraits to set
	 */
	public void setNewTraits(final List<SettingDetail> newTraits) {
		this.newTraits = newTraits;
	}

	/**
	 * Gets the new selection variates.
	 *
	 * @return the newSelectionVariates
	 */
	public List<SettingDetail> getNewSelectionVariates() {
		return this.newSelectionVariates;
	}

	/**
	 * Sets the new selection variates.
	 *
	 * @param newSelectionVariates the newSelectionVariates to set
	 */
	public void setNewSelectionVariates(final List<SettingDetail> newSelectionVariates) {
		this.newSelectionVariates = newSelectionVariates;
	}

	/**
	 * Gets the current page.
	 *
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage the currentPage to set
	 */
	public void setCurrentPage(final int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Gets the measurement row list.
	 *
	 * @return the measurementRowList
	 */
	public List<MeasurementRow> getMeasurementRowList() {
		return this.measurementRowList;
	}

	/**
	 * Sets the measurement row list.
	 *
	 * @param measurementRowList the measurementRowList to set
	 */
	public void setMeasurementRowList(final List<MeasurementRow> measurementRowList) {
		this.measurementRowList = measurementRowList;
	}

	/**
	 * Gets the temporary workbook.
	 *
	 * @return the temporary workbook
	 */
	public Workbook getTemporaryWorkbook() {
		return this.temporaryWorkbook;
	}

	/**
	 * Sets the temporary workbook.
	 *
	 * @param temporaryWorkbook the new temporary workbook
	 */
	public void setTemporaryWorkbook(final Workbook temporaryWorkbook) {
		this.temporaryWorkbook = temporaryWorkbook;
	}

	/**
	 * Gets the deleted trial level variables.
	 *
	 * @return the deleted trial level variables
	 */
	public List<SettingDetail> getDeletedTrialLevelVariables() {
		return this.deletedTrialLevelVariables;
	}

	/**
	 * Sets the deleted trial level variables.
	 *
	 * @param deletedTrialLevelVariables the new deleted trial level variables
	 */
	public void setDeletedTrialLevelVariables(final List<SettingDetail> deletedTrialLevelVariables) {
		this.deletedTrialLevelVariables = deletedTrialLevelVariables;
	}

	public List<MeasurementVariable> getMeasurementDatasetVariable() {
		return this.measurementDatasetVariable;
	}

	public void setMeasurementDatasetVariable(final List<MeasurementVariable> measurementDatasetVariable) {
		this.measurementDatasetVariable = measurementDatasetVariable;
	}

	/**
	 * Gets the deleted treatment factors.
	 *
	 * @return the deleted treatment factors
	 */
	public List<SettingDetail> getDeletedTreatmentFactors() {
		return this.deletedTreatmentFactors;
	}

	/**
	 * Sets the deleted treatment factors.
	 *
	 * @param deletedTreatmentFactors the new deleted treatment factors
	 */
	public void setDeletedTreatmentFactors(final List<SettingDetail> deletedTreatmentFactors) {
		this.deletedTreatmentFactors = deletedTreatmentFactors;
	}

	/**
	 * @return the constantsWithLabels
	 */
	public List<MeasurementVariable> getConstantsWithLabels() {
		return this.constantsWithLabels;
	}

	/**
	 * @param constantsWithLabels the constantsWithLabels to set
	 */
	public void setConstantsWithLabels(final List<MeasurementVariable> constantsWithLabels) {
		this.constantsWithLabels = constantsWithLabels;
	}

	/**
	 * @return the expDesignParams
	 */
	public ExpDesignParameterUi getExpDesignParams() {
		return this.expDesignParams;
	}

	/**
	 * @param expDesignParams the expDesignParams to set
	 */
	public void setExpDesignParams(final ExpDesignParameterUi expDesignParams) {
		this.expDesignParams = expDesignParams;
	}

	/**
	 * @return the expDesignVariables
	 */
	public List<Integer> getExpDesignVariables() {
		return this.expDesignVariables;
	}

	/**
	 * @param expDesignVariables the expDesignVariables to set
	 */
	public void setExpDesignVariables(final List<Integer> expDesignVariables) {
		this.expDesignVariables = expDesignVariables;
	}

	/**
	 * @return the experimentalDesignVariables
	 */
	public List<MeasurementVariable> getExperimentalDesignVariables() {
		return this.experimentalDesignVariables;
	}

	/**
	 * @param experimentalDesignVariables the experimentalDesignVariables to set
	 */
	public void setExperimentalDesignVariables(final List<MeasurementVariable> experimentalDesignVariables) {
		this.experimentalDesignVariables = experimentalDesignVariables;
	}

	public ImportedCrossesList getImportedCrossesList() {
		return this.importedCrossesList;
	}

	public void setImportedCrossesList(final ImportedCrossesList importedCrossesList) {
		this.importedCrossesList = importedCrossesList;
	}

	public CrossSetting getCrossSettings() {
		return this.crossSettings;
	}

	public void setCrossSettings(final CrossSetting crossSettings) {
		this.crossSettings = crossSettings;
	}

	public List<Integer> getImportedCrossesId() {
		return this.importedCrossesId;
	}

	public void setImportedCrossesId(final List<Integer> importedCrossesId) {
		this.importedCrossesId = importedCrossesId;
	}

	public void addImportedCrossesId(final Integer crossesId) {
		if (this.importedCrossesId == null) {
			this.importedCrossesId = new ArrayList<Integer>();
		}
		if (crossesId != null) {
			this.importedCrossesId.add(crossesId);
		}
	}

	public DesignImportData getDesignImportData() {
		return this.designImportData;
	}

	public void setDesignImportData(final DesignImportData designImportData) {
		this.designImportData = designImportData;
	}

	public boolean isDesignGenerated() {
		return this.getTemporaryWorkbook() != null;
	}

	public void setListId(final Integer listId) {
		this.listId = listId;
	}

	public Integer getListId() {
		return this.listId;
	}

	public void setInventoryDetails(final List<InventoryDetails> inventoryDetailsList) {
		this.previousInventoryDetails = inventoryDetailsList;
	}

	public List<InventoryDetails> getInventoryDetails() {
		return this.previousInventoryDetails;
	}

	public String getEscapedStudyName() {
		return HtmlUtils.htmlUnescape(this.getWorkbook().getStudyDetails().getStudyName());
	}

	public Integer getStartingPlotNo() {
		return startingPlotNo;
	}

	public void setStartingPlotNo(final Integer startingPlotNo) {
		this.startingPlotNo = startingPlotNo;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public boolean isFieldLayoutRandom() {
		return fieldLayoutRandom;
	}

	public void setFieldLayoutRandom(final boolean fieldLayoutRandom) {
		this.fieldLayoutRandom = fieldLayoutRandom;
	}

	public List<InventoryDetails> getPreviousInventoryDetails() {
		return previousInventoryDetails;
	}

	public void setPreviousInventoryDetails(final List<InventoryDetails> previousInventoryDetails) {
		this.previousInventoryDetails = previousInventoryDetails;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public void setStudyDescription(final String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public String getStudyObjective() {
		return studyObjective;
	}

	public void setStudyObjective(final String studyObjective) {
		this.studyObjective = studyObjective;
	}

	public String getStudyStartDate() {
		return studyStartDate;
	}

	public void setStudyStartDate(final String studyStartDate) {
		this.studyStartDate = studyStartDate;
	}

	public String getStudyEndDate() {
		return studyEndDate;
	}

	public void setStudyEndDate(final String studyEndDate) {
		this.studyEndDate = studyEndDate;
	}

	public String getStudyUpdate() {
		return studyUpdate;
	}

	public void setStudyUpdate(final String studyUpdate) {
		this.studyUpdate = studyUpdate;
	}

	public String getStudyType() {
		return studyType;
	}

	public void setStudyType(final String studyType) {
		this.studyType = studyType;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof UserSelection))
			return false;
		final UserSelection that = (UserSelection) o;
		return isFieldLayoutRandom() == that.isFieldLayoutRandom() && isImportValid() == that.isImportValid()
			&& getCurrentPageGermplasmList() == that.getCurrentPageGermplasmList() && getCurrentPageCheckGermplasmList() == that
			.getCurrentPageCheckGermplasmList() && getCurrentPage() == that.getCurrentPage() && Objects
			.equals(getActualFileName(), that.getActualFileName()) && Objects.equals(getServerFileName(), that.getServerFileName())
			&& Objects.equals(getStudyDetailsList(), that.getStudyDetailsList()) && Objects
			.equals(getStudyLevelConditions(), that.getStudyLevelConditions()) && Objects
			.equals(getPlotsLevelList(), that.getPlotsLevelList()) && Objects.equals(getBaselineTraitsList(), that.getBaselineTraitsList())
			&& Objects.equals(getTrialLevelVariableList(), that.getTrialLevelVariableList()) && Objects
			.equals(getTrialEnvironmentValues(), that.getTrialEnvironmentValues()) && Objects
			.equals(getImportedAdvancedGermplasmList(), that.getImportedAdvancedGermplasmList()) && Objects
			.equals(getTraitRefList(), that.getTraitRefList()) && Objects.equals(getTreatmentFactors(), that.getTreatmentFactors())
			&& Objects.equals(getSelectionVariates(), that.getSelectionVariates()) && Objects
			.equals(getBasicDetails(), that.getBasicDetails()) && Objects.equals(getStudyConditions(), that.getStudyConditions())
			&& Objects.equals(getDeletedStudyLevelConditions(), that.getDeletedStudyLevelConditions()) && Objects
			.equals(getDeletedPlotLevelList(), that.getDeletedPlotLevelList()) && Objects
			.equals(getDeletedBaselineTraitsList(), that.getDeletedBaselineTraitsList()) && Objects
			.equals(getDeletedStudyConditions(), that.getDeletedStudyConditions()) && Objects
			.equals(getDeletedTrialLevelVariables(), that.getDeletedTrialLevelVariables()) && Objects
			.equals(getDeletedTreatmentFactors(), that.getDeletedTreatmentFactors()) && Objects
			.equals(getChangeDetails(), that.getChangeDetails()) && Objects.equals(getRemovedFactors(), that.getRemovedFactors()) && Objects
			.equals(getRemovedConditions(), that.getRemovedConditions()) && Objects.equals(getNewTraits(), that.getNewTraits()) && Objects
			.equals(getNewSelectionVariates(), that.getNewSelectionVariates()) && Objects.equals(getWorkbook(), that.getWorkbook())
			&& Objects.equals(getTemporaryWorkbook(), that.getTemporaryWorkbook()) && Objects
			.equals(getDesignImportData(), that.getDesignImportData())
			&& Objects.equals(getMeasurementRowList(), that.getMeasurementRowList()) && Objects
			.equals(getMeasurementDatasetVariable(), that.getMeasurementDatasetVariable()) && Objects
			.equals(getConstantsWithLabels(), that.getConstantsWithLabels()) && Objects
			.equals(getExpDesignParams(), that.getExpDesignParams()) && Objects
			.equals(getExpDesignVariables(), that.getExpDesignVariables()) && Objects
			.equals(getExperimentalDesignVariables(), that.getExperimentalDesignVariables()) && Objects
			.equals(getImportedCrossesList(), that.getImportedCrossesList()) && Objects
			.equals(getImportedCrossesId(), that.getImportedCrossesId()) && Objects.equals(getCrossSettings(), that.getCrossSettings())
			&& Objects.equals(getListId(), that.getListId()) && Objects
			.equals(getPreviousInventoryDetails(), that.getPreviousInventoryDetails()) && Objects.equals(getStartingPlotNo(), that.getStartingPlotNo())
			&& Objects.equals(getStudyName(), that.getStudyName()) && Objects.equals(getStudyDescription(), that.getStudyDescription())
			&& Objects.equals(getStudyObjective(), that.getStudyObjective()) && Objects
			.equals(getStudyStartDate(), that.getStudyStartDate()) && Objects.equals(getStudyEndDate(), that.getStudyEndDate()) && Objects
			.equals(getStudyUpdate(), that.getStudyUpdate()) && Objects.equals(getStudyType(), that.getStudyType()) && Objects
			.equals(getCreatedBy(), that.getCreatedBy());
	}

	@Override
	public int hashCode() {

		return Objects.hash(getActualFileName(), getServerFileName(), isFieldLayoutRandom(), isImportValid(), getStudyDetailsList(),
			getCurrentPageGermplasmList(), getCurrentPageCheckGermplasmList(), getStudyLevelConditions(), getPlotsLevelList(),
			getBaselineTraitsList(), getTrialLevelVariableList(), getTrialEnvironmentValues(), getImportedAdvancedGermplasmList(),
			getTraitRefList(), getTreatmentFactors(), getSelectionVariates(), getBasicDetails(), getStudyConditions(),
			getDeletedStudyLevelConditions(), getDeletedPlotLevelList(), getDeletedBaselineTraitsList(), getDeletedStudyConditions(),
			getDeletedTrialLevelVariables(), getDeletedTreatmentFactors(), getChangeDetails(), getRemovedFactors(), getRemovedConditions(),
			getNewTraits(),	getNewSelectionVariates(), getWorkbook(), getTemporaryWorkbook(), getDesignImportData(), getCurrentPage(),
			getMeasurementRowList(), getMeasurementDatasetVariable(), getConstantsWithLabels(), getExpDesignParams(),
			getExpDesignVariables(), getExperimentalDesignVariables(), getImportedCrossesList(), getImportedCrossesId(), getCrossSettings(),
			getListId(), getPreviousInventoryDetails(), getStartingPlotNo(), getStudyName(), getStudyDescription(),
			getStudyObjective(), getStudyStartDate(), getStudyEndDate(), getStudyUpdate(), getStudyType(), getCreatedBy());
	}

	@Override
	public String toString() {
		return "UserSelection{" + "actualFileName='" + actualFileName + '\'' + ", serverFileName='" + serverFileName + '\''
			+ ", fieldLayoutRandom=" + fieldLayoutRandom + ", importedGermplasmMainInfo=" + importedGermplasmMainInfo
			+ ", importedCheckGermplasmMainInfo=" + importedCheckGermplasmMainInfo + ", isImportValid=" + isImportValid
			+ ", studyDetailsList=" + studyDetailsList + ", currentPageGermplasmList=" + currentPageGermplasmList
			+ ", currentPageCheckGermplasmList=" + currentPageCheckGermplasmList + ", studyLevelConditions=" + studyLevelConditions
			+ ", plotsLevelList=" + plotsLevelList + ", baselineTraitsList=" + baselineTraitsList + ", trialLevelVariableList="
			+ trialLevelVariableList + ", trialEnvironmentValues=" + trialEnvironmentValues + ", importedAdvancedGermplasmList="
			+ importedAdvancedGermplasmList + ", traitRefList=" + traitRefList + ", treatmentFactors=" + treatmentFactors
			+ ", selectionVariates=" + selectionVariates + ", basicDetails=" + basicDetails + ", studyConditions=" + studyConditions
			+ ", deletedStudyLevelConditions=" + deletedStudyLevelConditions + ", deletedPlotLevelList=" + deletedPlotLevelList
			+ ", deletedBaselineTraitsList=" + deletedBaselineTraitsList + ", deletedstudyConditions=" + deletedStudyConditions
			+ ", deletedTrialLevelVariables=" + deletedTrialLevelVariables + ", deletedTreatmentFactors=" + deletedTreatmentFactors
			+ ", changeDetails=" + changeDetails + ", removedFactors=" + removedFactors + ", removedConditions=" + removedConditions
			+ ", newTraits=" + newTraits + ", newSelectionVariates=" + newSelectionVariates + ", workbook=" + workbook
			+ ", temporaryWorkbook=" + temporaryWorkbook + ", designImportData=" + designImportData + ", currentPage=" + currentPage
			+ ", measurementRowList=" + measurementRowList + ", measurementDatasetVariable="
			+ measurementDatasetVariable + ", constantsWithLabels=" + constantsWithLabels + ", expDesignParams=" + expDesignParams
			+ ", expDesignVariables=" + expDesignVariables + ", experimentalDesignVariables=" + experimentalDesignVariables
			+ ", importedCrossesList=" + importedCrossesList + ", importedCrossesId=" + importedCrossesId + ", crossSettings="
			+ crossSettings + ", listId=" + listId + ", previousInventoryDetails=" + previousInventoryDetails + ", startingPlotNo="
			+ startingPlotNo + ", studyName='" + studyName + '\'' + ", studyDescription='"
			+ studyDescription + '\'' + ", studyObjective='" + studyObjective + '\'' + ", studyStartDate='" + studyStartDate + '\''
			+ ", studyEndDate='" + studyEndDate + '\'' + ", studyUpdate='" + studyUpdate + '\'' + ", studyType='" + studyType + '\''
			+ ", createdBy='" + createdBy + '\'' + '}';
	}
}
