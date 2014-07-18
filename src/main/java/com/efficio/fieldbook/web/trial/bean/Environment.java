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

    private Map<Integer, String> managementDetailValues;
    private Map<Integer, String> trialDetailValues;
    private Map<Integer, Integer> phenotypeIDMap;

    public Environment() {
        managementDetailValues = new HashMap<Integer, String>();
        trialDetailValues = new HashMap<Integer, String>();
        phenotypeIDMap = new HashMap<Integer, Integer>();
    }

    public Map<Integer, String> getManagementDetailValues() {
        return managementDetailValues;
    }

    public void setManagementDetailValues(Map<Integer, String> managementDetailValues) {
        this.managementDetailValues = managementDetailValues;
    }

    public Map<Integer, String> getTrialDetailValues() {
        return trialDetailValues;
    }

    public void setTrialDetailValues(Map<Integer, String> trialDetailValues) {
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

    public Map<Integer, Integer> getPhenotypeIDMap() {
        return phenotypeIDMap;
    }

    public void setPhenotypeIDMap(Map<Integer, Integer> phenotypeIDMap) {
        this.phenotypeIDMap = phenotypeIDMap;
    }
}
