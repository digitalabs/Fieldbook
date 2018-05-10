
package com.efficio.fieldbook.web.trial.bean;

public enum WidgetType {

	DROPDOWN, DATE, CTEXT, NTEXT, SLIDER, TEXTAREA;

	public String getType() {
		return this.name();
	}
}
