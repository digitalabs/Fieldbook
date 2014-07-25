package com.efficio.fieldbook.web.trial.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 *
 * This is a class used to represent treatment factor data being submitted from the front end
 */
public class TreatmentFactorData {
    private int levels;
    private List<String> labels;
    private Integer variableId;
    /*private Map<String, Integer> pairVariable;*/

    public static final String PAIR_VARIABLE_ID_KEY = "variableId";

    public TreatmentFactorData() {
        /*pairVariable = new HashMap<String, Integer>();*/
        labels = new ArrayList<String>();
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Integer getVariableId() {
        return variableId;
    }

    public void setVariableId(Integer variableId) {
        this.variableId = variableId;
    }

    /*public Map<String, Integer> getPairVariable() {
        return pairVariable;
    }

    public void setPairVariable(Map<String, Integer> pairVariable) {
        this.pairVariable = pairVariable;
    }*/
}
