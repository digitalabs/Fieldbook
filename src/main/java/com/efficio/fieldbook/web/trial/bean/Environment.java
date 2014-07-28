package com.efficio.fieldbook.web.trial.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 7/8/2014
 * Time: 5:08 PM
 */
public class Environment{
    private long stockId;
    private long locationId;
    private long experimentId;

    private Map<String, String> managementDetailValues;
    private Map<String, String> trialDetailValues;
    private Map<String, Integer> phenotypeIDMap;

    public Environment() {
        managementDetailValues = new HashMap<String, String>();
        trialDetailValues = new HashMap<String, String>();
        phenotypeIDMap = new HashMap<String, Integer>();
    }

    public Map<String, String> getManagementDetailValues() {
        return managementDetailValues;
    }

    public void setManagementDetailValues(Map<String, String> managementDetailValues) {
        this.managementDetailValues = managementDetailValues;
    }

    public Map<String, String> getTrialDetailValues() {
        return trialDetailValues;
    }

    public void setTrialDetailValues(Map<String, String> trialDetailValues) {
        this.trialDetailValues = trialDetailValues;
    }

    public long getStockId() {
        return stockId;
    }

    public void setStockId(long stockId) {
        this.stockId = stockId;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    public long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(long experimentId) {
        this.experimentId = experimentId;
    }

    public Map<String, Integer> getPhenotypeIDMap() {
        return phenotypeIDMap;
    }

    public void setPhenotypeIDMap(Map<String, Integer> phenotypeIDMap) {
        this.phenotypeIDMap = phenotypeIDMap;
    }
}
