
package com.efficio.fieldbook.web.trial.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/8/2014 Time: 5:08 PM
 */
public class Environment {

	private long stockId;
	private long locationId;
	private long experimentId;

	private Map<String, String> managementDetailValues;
	private Map<String, String> trialDetailValues;
	private Map<String, Integer> phenotypeIDMap;

	public Environment() {
		this.managementDetailValues = new HashMap<String, String>();
		this.trialDetailValues = new HashMap<String, String>();
		this.phenotypeIDMap = new HashMap<String, Integer>();
	}

	public Map<String, String> getManagementDetailValues() {
		return this.managementDetailValues;
	}

	public void setManagementDetailValues(Map<String, String> managementDetailValues) {
		this.managementDetailValues = managementDetailValues;
	}

	public Map<String, String> getTrialDetailValues() {
		return this.trialDetailValues;
	}

	public void setTrialDetailValues(Map<String, String> trialDetailValues) {
		this.trialDetailValues = trialDetailValues;
	}

	public long getStockId() {
		return this.stockId;
	}

	public void setStockId(long stockId) {
		this.stockId = stockId;
	}

	public long getLocationId() {
		return this.locationId;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public long getExperimentId() {
		return this.experimentId;
	}

	public void setExperimentId(long experimentId) {
		this.experimentId = experimentId;
	}

	public Map<String, Integer> getPhenotypeIDMap() {
		return this.phenotypeIDMap;
	}

	public void setPhenotypeIDMap(Map<String, Integer> phenotypeIDMap) {
		this.phenotypeIDMap = phenotypeIDMap;
	}
}
