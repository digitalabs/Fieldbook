package com.efficio.fieldbook.web.trial.bean.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

public class ListItem implements Serializable {
	private String value;
	
	public ListItem(){
		super();
	}

	public ListItem(String value) {
		super();
		this.value = value;
	}

	@XmlAttribute
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
