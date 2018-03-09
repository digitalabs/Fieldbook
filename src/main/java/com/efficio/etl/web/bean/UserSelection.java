
package com.efficio.etl.web.bean;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte This bean models the various input that the user builds up over time to perform the
 * actual loading operation
 */
public class UserSelection implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6063231539876569575L;
	private String actualFileName;
	private String serverFileName;

	private Integer selectedSheet;
	private Integer headerRowIndex;
	private Integer contentRowIndex;
	private Integer indexColumnIndex;
	private Integer observationRows;

	private String headerRowDisplayText;
	private String contentRowDisplayText;

	private String studyName;
	private String studyDescription;
	private String studyObjective;
	private String studyStartDate;
	private String studyEndDate;
	private String studyUpdate;
	private String studyType;
	private Integer datasetType;
	private String createdBy;
	private Integer lastSheetRowNum;

	private Boolean initialCategorizationDone = false;

	private Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> giantMap =
			new LinkedHashMap<>();

	private Map<PhenotypicType, List<String>> headerCategorization = new LinkedHashMap<>();

	// add selected study and datasets if available
	private Integer studyId;
	private Integer trialDatasetId;
	private Integer measurementDatasetId;
	private Integer meansDatasetId;

	public void setHeadersForCategory(final List<String> headers, final PhenotypicType type) {
		this.headerCategorization.put(type, headers);
	}

	public MeasurementVariable getCurrentVariableData(final String name, final PhenotypicType category) {

		final Map<String, MeasurementVariable> typeMap = this.giantMap.get(category);

		return typeMap.get(name);
	}

	public List<String> getHeadersForCategory(final PhenotypicType category) {
		return this.headerCategorization.get(category);
	}

	public String getActualFileName() {
		return this.actualFileName;
	}

	public void setActualFileName(final String actualFileName) {
		this.actualFileName = actualFileName;
	}

	public String getServerFileName() {
		return this.serverFileName;
	}

	public void setServerFileName(final String serverFileName) {
		this.serverFileName = serverFileName;
	}

	public Integer getSelectedSheet() {
		return this.selectedSheet;
	}

	public void setSelectedSheet(final Integer selectedSheet) {
		this.selectedSheet = selectedSheet;
	}

	public Integer getHeaderRowIndex() {
		return this.headerRowIndex;
	}

	public void setHeaderRowIndex(final Integer headerRowIndex) {
		this.headerRowIndex = headerRowIndex;
	}

	public Integer getContentRowIndex() {
		return this.contentRowIndex;
	}

	public void setContentRowIndex(final Integer contentRowIndex) {
		this.contentRowIndex = contentRowIndex;
	}

	public Integer getIndexColumnIndex() {
		return this.indexColumnIndex;
	}

	public void setIndexColumnIndex(final Integer indexColumnIndex) {
		this.indexColumnIndex = indexColumnIndex;
	}

	public Integer getObservationRows() {
		return this.observationRows;
	}

	public void setObservationRows(final Integer observationRows) {
		this.observationRows = observationRows;
	}

	public String getStudyName() {
		return this.studyName;
	}

	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	public String getStudyDescription() {
		return this.studyDescription;
	}

	public void setStudyDescription(final String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public String getStudyObjective() {
		return this.studyObjective;
	}

	public void setStudyObjective(final String studyObjective) {
		this.studyObjective = studyObjective;
	}

	public String getStudyStartDate() {
		return this.studyStartDate;
	}

	public void setStudyStartDate(final String studyStartDate) {
		this.studyStartDate = studyStartDate;
	}

	public String getStudyEndDate() {
		return this.studyEndDate;
	}

	public void setStudyEndDate(final String studyEndDate) {
		this.studyEndDate = studyEndDate;
	}

	public String getStudyType() {
		return this.studyType;
}

	public void setStudyType(final String studyType) {
		this.studyType = studyType;
	}

	public void transferTo(final UserSelection userSelection) {
		BeanUtils.copyProperties(this, userSelection);
		userSelection.setHeaderCategorization(this.headerCategorization);
	}

	public void setMeasurementVariablesByPhenotypic(final PhenotypicType phenotypickey, final LinkedHashMap<String, MeasurementVariable> item) {
		this.giantMap.put(phenotypickey, item);
	}

	public void clearMeasurementVariables() {
		if (this.giantMap != null) {
			this.giantMap.clear();
		}
	}

	public Map<String, MeasurementVariable> getMeasurementVariablesByPhenotypic(final PhenotypicType phenotypickey) {

		return this.giantMap.get(phenotypickey);
	}

	public Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> getPhenotypicMap() {
		return this.giantMap;
	}

	public Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> getGiantMap() {
		return this.giantMap;
	}

	public void setGiantMap(final Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> giantMap) {
		this.giantMap = giantMap;
	}

	public Map<PhenotypicType, List<String>> getHeaderCategorization() {
		return this.headerCategorization;
	}

	public void setHeaderCategorization(final Map<PhenotypicType, List<String>> headerCategorization) {
		this.headerCategorization = headerCategorization;
	}

	public String getHeaderRowDisplayText() {
		return this.headerRowDisplayText;
	}

	public void setHeaderRowDisplayText(final String headerRowDisplayText) {
		this.headerRowDisplayText = headerRowDisplayText;
	}

	public String getContentRowDisplayText() {
		return this.contentRowDisplayText;
	}

	public void setContentRowDisplayText(final String contentRowDisplayText) {
		this.contentRowDisplayText = contentRowDisplayText;
	}

	public Integer getLastSheetRowNum() {
		return this.lastSheetRowNum;
	}

	public void setLastSheetRowNum(final Integer lastSheetRowNum) {
		this.lastSheetRowNum = lastSheetRowNum;
	}

	public Boolean getInitialCategorizationDone() {
		return this.initialCategorizationDone;
	}

	public void setInitialCategorizationDone(final Boolean initialCategorizationDone) {
		this.initialCategorizationDone = initialCategorizationDone;
	}

	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
	}

	public Integer getTrialDatasetId() {
		return this.trialDatasetId;
	}

	public void setTrialDatasetId(final Integer trialDatasetId) {
		this.trialDatasetId = trialDatasetId;
	}

	public Integer getMeasurementDatasetId() {
		return this.measurementDatasetId;
	}

	public void setMeasurementDatasetId(final Integer measurementDatasetId) {
		this.measurementDatasetId = measurementDatasetId;
	}

	public Integer getMeansDatasetId() {
		return this.meansDatasetId;
	}

	public void setMeansDatasetId(final Integer meansDatasetId) {
		this.meansDatasetId = meansDatasetId;
	}

	public Integer getDatasetType() {
		return this.datasetType;
	}

	public void setDatasetType(final Integer datasetType) {
		this.datasetType = datasetType;
	}

	public String getStudyUpdate() {
		return studyUpdate;
	}

	public void setStudyUpdate(final String studyUpdate) {
		this.studyUpdate = studyUpdate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}
}
