
package com.efficio.fieldbook.web.trial.bean;

import com.efficio.fieldbook.web.common.bean.SettingVariable;

public class ExpDesignDataDetail {

	private String label;
	private SettingVariable variable;

	public ExpDesignDataDetail(String label, SettingVariable variable) {
		super();
		this.label = label;
		this.variable = variable;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public SettingVariable getVariable() {
		return this.variable;
	}

	public void setVariable(SettingVariable variable) {
		this.variable = variable;
	}

}
