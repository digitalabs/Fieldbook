package com.efficio.fieldbook.web.trial.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 7/22/2014
 * Time: 6:04 PM
 */
public class TreatmentFactorTabBean implements TabInfoBean{
    public Map<Integer, TreatmentFactorData> currentData;

    public TreatmentFactorTabBean() {
        currentData = new HashMap<Integer, TreatmentFactorData>();
    }

    public Map<Integer, TreatmentFactorData> getCurrentData() {
        return currentData;
    }

    public void setCurrentData(Map<Integer, TreatmentFactorData> currentData) {
        this.currentData = currentData;
    }
}
