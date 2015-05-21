package com.efficio.pojos.labelprinting;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.pojos.GermplasmList;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

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
	private GermplasmList stockList;
	private boolean isStockList;
	private Map<String, InventoryDetails> inventoryDetailsMap;

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

	public List<Integer> getAllFieldIDs() {
		return allFieldIDs;
	}
	
	public void setAllFieldIDs(List<Integer> allFieldIds) {
		this.allFieldIDs = allFieldIds;
	}

	public GermplasmList getStockList() {
		return stockList;
	}

	public void setStockList(GermplasmList stockList) {
		this.stockList = stockList;
	}

	public boolean isStockList() {
		return isStockList;
	}

	public void setIsStockList(boolean isStockList) {
		this.isStockList = isStockList;
	}

	public Map<String, InventoryDetails> getInventoryDetailsMap() {
		return inventoryDetailsMap;
	}

	public void setInventoryDetailsMap(
			Map<String, InventoryDetails> inventoryDetailsMap) {
		this.inventoryDetailsMap = inventoryDetailsMap;
	}
}
