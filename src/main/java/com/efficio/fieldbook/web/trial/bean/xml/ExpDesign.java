package com.efficio.fieldbook.web.trial.bean.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="template")
public class ExpDesign  implements Serializable {
	private String name;
		
	private List<ExpDesignParameter> parameters;
		
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

	public List<ExpDesignParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ExpDesignParameter> parameters) {
		this.parameters = parameters;
	}
	
	
}
