package com.efficio.fieldbook.web.common.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.inventory.InventoryDetails;

import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;

public class PaginationListSelection implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2448596622077650635L;
	
	private Map<String, List<InventoryDetails>> finalAdvancedList = new HashMap<String, List<InventoryDetails>>();
	private Map<String, AdvancingNurseryForm> advanceMap = new HashMap<String, AdvancingNurseryForm>();
	
	public void addFinalAdvancedList(String id, List<InventoryDetails> inveList) {
		this.finalAdvancedList.put(id, inveList);
	}
	public List<InventoryDetails> getFinalAdvancedList(String id){
		return finalAdvancedList.get(id);
	}
	public void addAdvanceDetails(String id, AdvancingNurseryForm form) {
		this.advanceMap.put(id, form);
	}
	
	public AdvancingNurseryForm getAdvanceDetails(String id) {
		return this.advanceMap.get(id);
	}
}
