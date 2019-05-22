
package com.efficio.fieldbook.web.trial.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/22/2014 Time: 6:04 PM
 */
public class TreatmentFactorTabBean implements TabInfoBean {

	public Map<String, TreatmentFactorData> currentData;

	public TreatmentFactorTabBean() {
		this.currentData = new HashMap<>();
	}

	public Map<String, TreatmentFactorData> getCurrentData() {
		return this.currentData;
	}

	public void setCurrentData(Map<String, TreatmentFactorData> currentData) {
		this.currentData = currentData;
	}
}
