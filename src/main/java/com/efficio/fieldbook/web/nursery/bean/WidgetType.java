
package com.efficio.fieldbook.web.nursery.bean;

public enum WidgetType {

	DROPDOWN, DATE, CTEXT, NTEXT, SLIDER, TEXTAREA;

	public String getType() {
		return this.name();
	}
}
