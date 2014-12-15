package com.efficio.fieldbook.web.trial.bean.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
	
	public void setParameterValue(String name, String value){
		boolean isFound = false;
		if(parameters != null){
			for(ExpDesignParameter param : parameters){
				if(name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)){
					param.setValue(value);
					isFound = true;
					break;
				}
			}
		}
		if(!isFound){
			if(parameters == null){
				parameters = new ArrayList<ExpDesignParameter>();
			}
			parameters.add(new ExpDesignParameter(name, value));
		}
	}
	
	public String getParameterValue(String name){
		if(parameters != null){
			for(ExpDesignParameter param : parameters){
				if(name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)){
					return param.getValue();
				}
			}
		}
		return "";
	}
	
	public List<ListItem> getParameterList(String name){
		if(parameters != null){
			for(ExpDesignParameter param : parameters){
				if(name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)){
					return param.getListItem();
				}
			}
		}
		return new ArrayList<ListItem>();
	}
}
