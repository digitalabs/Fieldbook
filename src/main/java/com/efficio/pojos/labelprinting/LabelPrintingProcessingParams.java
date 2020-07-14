
package com.efficio.pojos.labelprinting;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.InventoryDetails;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class LabelPrintingProcessingParams {

	private Map<Integer, String> labelHeaders;
	private List<Integer> selectedFieldIDs;
	private List<Integer> allFieldIDs;
	private Map<Integer, MeasurementVariable> variableMap;
	private MeasurementRow environmentData;
	private FieldMapTrialInstanceInfo instanceInfo;
	private List<MeasurementRow> instanceMeasurements;
	private Map<Integer, String> userSpecifiedLabels;
	private Map<String, InventoryDetails> inventoryDetailsMap;

	public LabelPrintingProcessingParams() {
	}

	public Map<Integer, String> getLabelHeaders() {
		return this.labelHeaders;
	}

	public void setLabelHeaders(Map<Integer, String> labelHeaders) {
		this.labelHeaders = labelHeaders;
	}

	public List<Integer> getSelectedFieldIDs() {
		return this.selectedFieldIDs;
	}

	public void setSelectedFieldIDs(List<Integer> selectedFieldIDs) {
		this.selectedFieldIDs = selectedFieldIDs;
	}

	public Map<Integer, MeasurementVariable> getVariableMap() {
		return this.variableMap;
	}

	public void setVariableMap(Map<Integer, MeasurementVariable> variableMap) {
		this.variableMap = variableMap;
	}

	public MeasurementRow getEnvironmentData() {
		return this.environmentData;
	}

	public void setEnvironmentData(MeasurementRow environmentData) {
		this.environmentData = environmentData;

	}

	public FieldMapTrialInstanceInfo getInstanceInfo() {
		return this.instanceInfo;
	}

	public void setInstanceInfo(FieldMapTrialInstanceInfo instanceInfo) {
		this.instanceInfo = instanceInfo;
	}

	public List<MeasurementRow> getInstanceMeasurements() {
		return this.instanceMeasurements;
	}

	public void setInstanceMeasurements(List<MeasurementRow> instanceMeasurements) {
		this.instanceMeasurements = instanceMeasurements;
	}

	public Map<Integer, String> getUserSpecifiedLabels() {
		return this.userSpecifiedLabels;
	}

	public void setUserSpecifiedLabels(Map<Integer, String> userSpecifiedLabels) {
		this.userSpecifiedLabels = userSpecifiedLabels;
	}

	public List<Integer> getAllFieldIDs() {
		return this.allFieldIDs;
	}

	public void setAllFieldIDs(List<Integer> allFieldIds) {
		this.allFieldIDs = allFieldIds;
	}

	public Map<String, InventoryDetails> getInventoryDetailsMap() {
		return this.inventoryDetailsMap;
	}

	public void setInventoryDetailsMap(Map<String, InventoryDetails> inventoryDetailsMap) {
		this.inventoryDetailsMap = inventoryDetailsMap;
	}
}
