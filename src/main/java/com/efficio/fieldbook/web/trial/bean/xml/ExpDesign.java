package com.efficio.fieldbook.web.trial.bean.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ExpDesign  implements Serializable {
	
	private String name;		
	private List<ExpDesignParameter> parameters;
	
	public ExpDesign(){
		super();
	}
	
	public ExpDesign(String name, List<ExpDesignParameter> parameters) {
		super();
		this.name = name;
		this.parameters = parameters;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

	@XmlElement(name="Parameter")
	public List<ExpDesignParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ExpDesignParameter> parameters) {
		this.parameters = parameters;
	}
	
	
}
