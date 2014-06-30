package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;


public class PropertyTree {
	
	private TraitPojo traitClass;		
	
	private PropertyReference propertyReference;
	
	private List<StandardVariable> standardVariables = new ArrayList<StandardVariable>();
	
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
	
	public List<StandardVariable> getStandardVariables() {
		return standardVariables;
	}

	
	public void setStandardVariables(List<StandardVariable> standardVariables) {
		this.standardVariables = standardVariables;
	}
	
	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	final class TraitPojo {
		
		@SuppressWarnings("unused")
		private Integer traitClassId;
		
		@SuppressWarnings("unused")
		private String traitClassName;

		
		public void setTraitClassId(Integer traitClassId) {
			this.traitClassId = traitClassId;
		}

		
		public void setTraitClassName(String traitClassName) {
			this.traitClassName = traitClassName;
		}
		
	}
	
	
	
}
