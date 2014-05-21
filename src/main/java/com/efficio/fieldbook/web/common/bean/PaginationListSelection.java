package com.efficio.fieldbook.web.common.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.inventory.InventoryDetails;

public class PaginationListSelection implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2448596622077650635L;
	
	private Map<String, List<InventoryDetails>> finalAdvancedList = new HashMap<String, List<InventoryDetails>>();
	private Map<String, List<MeasurementRow>> reviewDetailsList = new HashMap<String, List<MeasurementRow>>();
	private Map<String, List<MeasurementVariable>> reviewVariableList = new HashMap<String, List<MeasurementVariable>>();
	
	public void addFinalAdvancedList(String id, List<InventoryDetails> inveList) {
		this.finalAdvancedList.put(id, inveList);
	}
	public List<InventoryDetails> getFinalAdvancedList(String id){
		return finalAdvancedList.get(id);
	}
	
	public void addReviewDetailsList(String datasetId, List<MeasurementRow> rows) {
		this.reviewDetailsList.put(datasetId, rows);
	}
	public List<MeasurementRow> getReviewDetailsList(String datasetId) {
		return this.reviewDetailsList.get(datasetId);
	}
	
	public void addReviewVariableList(String datasetId, List<MeasurementVariable> variables) {
		this.reviewVariableList.put(datasetId, variables);
	}
	public List<MeasurementVariable> getReviewVariableList(String datasetId) {
		return this.reviewVariableList.get(datasetId);
	}
}
