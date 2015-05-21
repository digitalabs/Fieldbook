package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;


public class PropertyTree {
	
	private TraitPojo traitClass;		
	
	private PropertyReference propertyReference;
	
	private List<StandardVariableSummary> standardVariables = new ArrayList<StandardVariableSummary>();
	
	public PropertyTree(PropertyReference propertyReference) {
		this.propertyReference = propertyReference;
	}
	
	public Integer getPropertyId(){
		return propertyReference.getId();
	}
	
	public String getName(){
		return propertyReference.getName();
	}
	
	public String getDescription(){
		return propertyReference.getDescription();
	}
	
	public TraitPojo getTraitClass() {
		return traitClass;
	}
	
	public void setTraitClass(TraitClassReference traitClassReference) {
		this.traitClass = new TraitPojo();
		traitClass.setTraitClassId(traitClassReference.getId());
		traitClass.setTraitClassName(traitClassReference.getName());
	}
	
	
	public List<StandardVariableSummary> getStandardVariables() {
		return standardVariables;
	}

	
	public void setStandardVariables(List<StandardVariableSummary> standardVariables) {
		this.standardVariables = standardVariables;
	}
}
