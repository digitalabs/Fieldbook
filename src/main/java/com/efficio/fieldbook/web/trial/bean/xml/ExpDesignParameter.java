package com.efficio.fieldbook.web.trial.bean.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Parameter")
public class ExpDesignParameter implements Serializable {
	private String name;
	private String value;		
	
	public ExpDesignParameter(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
