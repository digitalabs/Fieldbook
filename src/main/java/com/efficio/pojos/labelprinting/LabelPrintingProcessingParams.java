package com.efficio.pojos.labelprinting;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class LabelPrintingProcessingParams {
	private Map<Integer, String> labelHeaders;
	private List<Integer> selectedFieldIDs;
	private Map<Integer, MeasurementVariable> variableMap;
	private MeasurementRow environmentData;
	private FieldMapTrialInstanceInfo instanceInfo;
	private List<MeasurementRow> instanceMeasurements;
	private Map<Integer, String> userSpecifiedLabels;

	public LabelPrintingProcessingParams() {
	}

	public Map<Integer, String> getLabelHeaders() {
		return labelHeaders;
	}

	public void setLabelHeaders(Map<Integer, String> labelHeaders) {
		this.labelHeaders = labelHeaders;
	}

	public List<Integer> getSelectedFieldIDs() {
		return selectedFieldIDs;
	}

	public void setSelectedFieldIDs(List<Integer> selectedFieldIDs) {
		this.selectedFieldIDs = selectedFieldIDs;
	}

	public Map<Integer, MeasurementVariable> getVariableMap() {
		return variableMap;
	}

	public void setVariableMap(Map<Integer, MeasurementVariable> variableMap) {
		this.variableMap = variableMap;
	}

	public MeasurementRow getEnvironmentData() {
		return environmentData;
	}

	public void setEnvironmentData(MeasurementRow environmentData) {
		this.environmentData = environmentData;

	}

	public FieldMapTrialInstanceInfo getInstanceInfo() {
		return instanceInfo;
	}

	public void setInstanceInfo(FieldMapTrialInstanceInfo instanceInfo) {
		this.instanceInfo = instanceInfo;
	}

	public List<MeasurementRow> getInstanceMeasurements() {
		return instanceMeasurements;
	}

	public void setInstanceMeasurements(List<MeasurementRow> instanceMeasurements) {
		this.instanceMeasurements = instanceMeasurements;
	}

	public Map<Integer, String> getUserSpecifiedLabels() {
		return userSpecifiedLabels;
	}

	public void setUserSpecifiedLabels(Map<Integer, String> userSpecifiedLabels) {
		this.userSpecifiedLabels = userSpecifiedLabels;
	}
}
