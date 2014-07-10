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
    private Map<Integer, String> managementDetailValues;
    private Map<Integer, String> trialDetailValues;

    public Environment() {
        managementDetailValues = new HashMap<Integer, String>();
        trialDetailValues = new HashMap<Integer, String>();
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
}
